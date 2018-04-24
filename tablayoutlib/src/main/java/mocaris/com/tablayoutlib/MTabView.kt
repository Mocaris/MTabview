package mocaris.com.tablayoutlib

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.lang.IllegalStateException
import java.lang.reflect.Method
import java.util.regex.Pattern

/**
<!--图片宽高比 默认 图片原比例-->
<attr name="tab_icon_ratio" format="string" />
<!--图片引用-->
<attr name="tab_icon_drawable" format="reference" />
<!--tab 文字-->
<attr name="tab_txt" format="string" />
<!--tab 文字color-->
<attr name="tab_txt_color" format="color" />
<!--图片与文字中间间距-->
<attr name="tab_txt_icon_interval" format="dimension" />
<!--角标半径 默认左边-->
<attr name="tab_angle_radius" format="dimension" />
<!--角标数字-->
<attr name="tab_angle_digital" format="integer" />
 */
class MTabView : View {
    /**
     * 左上角
     */
    val TOP_LEFT = 3
    /**
     * 右上角
     */
    val TOP_RIGHT = 0

    //宽度比
    var iconRatio: Float = 1f
        set(value) {
            field = value
            invalidate()
        }
    //图标大小
    var iconSize: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    //图标
    var iconDrawable: StateListDrawable? = null
        set(value) {
            field = value
            invalidate()
        }
    //tab 文字
    var tabTxt: String = ""
        set(value) {
            field = value
            invalidate()
        }
    //文字大小
    var tabTxtSize: Float = 0f
        set(value) {
            field = value
            meaMeasureTxt()
            invalidate()
        }
    //文字颜色
    var tabTxtColor: ColorStateList? = null
        set(value) {
            field = value
            invalidate()
        }
    //文字 图标间距
    var txtIconInterval: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    //角标位置
    var angleLocation = TOP_RIGHT
        set(value) {
            field = value
            invalidate()
        }
    //角标颜色
    var angleColor: Int = Color.RED
        set(value) {
            field = value
            invalidate()
        }
    //角标半径大小
    var angleRadius: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    //角标数字
    var angleDigitals = ""
        set(value) {
            field = value
            invalidate()
        }
    //角标数字颜色  默认白色
    var angleDigitalsColor = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }
    //是否显示角标
    var showAngle: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    //是否选中
    var tabSelected = false
        set(value) {
            field = value
            invalidate()
        }
    //图标集合
    private var tabIcon: Array<BitmapDrawable?>? = null

    private val iconPaint = Paint()
    private val anglePaint = Paint()
    private val txtPaint = Paint()
    //tab txt 大小
    private val txtBounds = Rect()
    //图标大小
    private val iconRect = Rect()
    //内边距
    private var paddingBorder = 0

    private fun getDp(size: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, this@MTabView.context.applicationContext.resources.displayMetrics)
    }

    private fun getSp(size: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, this@MTabView.context.applicationContext.resources.displayMetrics)
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MTabView, defStyleAttr, 0)
        //宽高比
        val ratio = typedArray.getString(R.styleable.MTabView_tab_icon_ratio)
        if (null != ratio) {
            val pattern = "^\\d+(\\.\\d{1,2})?(:)\\d+(\\.\\d{1,2})?$"
            val matches = Pattern.compile(pattern).matcher(ratio).matches()
            if (!matches) {
                throw IllegalArgumentException("tab_icon_ratio $ratio  is error!! Example  1:1")
            }
            val ratio = ratio.split(":", ignoreCase = true)
            iconRatio = ratio[0].toFloat() / ratio[1].toFloat()
        } else {
            iconRatio = 1f
        }
        //图标
        val drawable = typedArray.getDrawable(R.styleable.MTabView_tab_icon_drawable)
        if (drawable != null && drawable is StateListDrawable) {
            iconDrawable = drawable
        }
        //图标大小
        iconSize = typedArray.getDimension(R.styleable.MTabView_tab_icon_size, 0f).toInt()
        ///文字
        val txt = typedArray.getString(R.styleable.MTabView_tab_txt)
        tabTxt = if (txt == null) "" else txt
        ///文字大小f
        tabTxtSize = typedArray.getDimension(R.styleable.MTabView_tab_txt_size, getSp(10f))
        ///文字颜色
        val txtColor = typedArray.getColorStateList(R.styleable.MTabView_tab_txt_color)
        if (null != txtColor) {
            tabTxtColor = txtColor
        }
        ///图标文字间距
        txtIconInterval = typedArray.getDimension(R.styleable.MTabView_tab_txt_icon_interval, getDp(3f)).toInt();
        ///角标半径
        angleRadius = typedArray.getDimension(R.styleable.MTabView_tab_angle_radius, getDp(6f))
        //角标颜色
        angleColor = typedArray.getColor(R.styleable.MTabView_tab_angle_color, Color.RED)
        ///角标数字
        val integer = typedArray.getInteger(R.styleable.MTabView_tab_angle_digital, 0)
        angleDigitals = if (integer == 0) "" else integer.toString()
        //角标位置
        angleLocation = typedArray.getInt(R.styleable.MTabView_tab_angle_location, TOP_RIGHT)
        //是否显示角标
        showAngle = typedArray.getBoolean(R.styleable.MTabView_tab_show_angle, false)
        //是否选中
        tabSelected = typedArray.getBoolean(R.styleable.MTabView_tab_selected, false)
        typedArray.recycle()
        initDef()
    }

    //初始化一些默认值
    private fun initDef() {
        anglePaint.isAntiAlias = true
        txtPaint.isAntiAlias = true
        iconPaint.isAntiAlias = true
        paddingBorder = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()
        meaMeasureTxt()
        if (null != iconDrawable) {
            tabIcon = arrayOf(getBitMapDrawable(android.R.attr.state_checked, iconDrawable!!)
                    , getBitMapDrawable(-android.R.attr.state_checked, iconDrawable!!))
        }
//        getDrawableFromStateDrwableList()
    }

    //测量文字 大小
    private fun meaMeasureTxt() {
        txtPaint.textSize = tabTxtSize
        txtPaint.getTextBounds(tabTxt, 0, tabTxt.length, txtBounds)
    }

    override fun onDraw(canvas: Canvas) {
        //角标
        drawAngle(canvas)
        //图标
        drawIcon(canvas)
        //文字
        drawTxt(canvas)

    }

    //角标
    private fun drawAngle(canvas: Canvas) {
        if (showAngle) {
            anglePaint.color = angleColor
            var cx = 0f
            var cy = 0f
            when (angleLocation) {
                TOP_RIGHT -> {
                    cx = measuredWidth - paddingRight - angleRadius
                    cy = paddingTop + angleRadius
                }
                TOP_LEFT -> {
                    cx = paddingLeft + angleRadius
                    cy = paddingTop + angleRadius
                }
            }
            canvas.drawCircle(cx, cy, angleRadius, anglePaint)
            canvas.save()
            //draw angleTxt
            if (angleDigitals.isNotEmpty()) {
                anglePaint.color = angleDigitalsColor
                //根据角标半径大小确定  文字大小  保证自适应
                anglePaint.textSize = angleRadius * 2
                anglePaint.textAlign = Paint.Align.LEFT//从左开始绘制
                val bounds = Rect()
                anglePaint.getTextBounds(angleDigitals, 0, angleDigitals.length, bounds)
                val fontMetrics = anglePaint.fontMetrics
                val x = cx - bounds.width() / 2
                val y = cy + bounds.height() / 2 - (fontMetrics.bottom - fontMetrics.descent) * 2
                canvas.drawText(angleDigitals, x, y, anglePaint)
            }
        }
    }

    //图标
    private fun drawIcon(canvas: Canvas) {
        if (null == iconDrawable) {
            return
        }
        val bitmapDrawable = when (tabSelected) {
            true -> {
                tabIcon?.get(0) ?: tabIcon?.get(1)
            }
            false -> {
                tabIcon?.get(1)
            }
        }
        //可用宽度
        val useWidth = measuredWidth - paddingLeft - paddingRight - paddingBorder * 2
        //可用高度
        val useHeight = measuredHeight - paddingTop - paddingBottom - paddingBorder * 2
        //确定图标宽高 为View 的一半  按比例算
        if (null != bitmapDrawable) {
            val min = if (0 == iconSize) {
                //自适应 宽高  View可用
                Math.min(useWidth, useHeight - txtBounds.height() - txtIconInterval)
            } else {
                //指定宽高
                Math.min(iconSize, useWidth)
            }
            iconRect.left = paddingLeft + paddingBorder + (useWidth - min) / 2
            iconRect.top = paddingTop + paddingBorder + (useHeight - min - txtIconInterval - txtBounds.height()) / 2
            iconRect.right = iconRect.left + min
            iconRect.bottom = (iconRect.top + min / iconRatio).toInt()
            canvas.drawBitmap(bitmapDrawable.bitmap, null, iconRect, iconPaint)
        }
    }

    //画文字
    private fun drawTxt(canvas: Canvas) {
        val fontMetrics = txtPaint.fontMetrics
        val x: Float = paddingLeft + paddingBorder + (measuredWidth - paddingLeft - paddingRight - paddingBorder * 2 - txtBounds.width()) / 2f
        val y: Float = (iconRect.bottom + txtIconInterval + txtBounds.height()).toFloat() - fontMetrics.descent
        txtPaint.textAlign = Paint.Align.LEFT
        txtPaint.textSize = tabTxtSize
        when (tabSelected) {
            true -> {
                txtPaint.color = tabTxtColor?.getColorForState(intArrayOf(android.R.attr.state_checked), Color.GRAY) ?: Color.GRAY
            }
            false -> {
                txtPaint.color = tabTxtColor?.getColorForState(intArrayOf(-android.R.attr.state_checked), Color.RED) ?: Color.GRAY
            }
        }
        canvas.drawText(tabTxt, 0, tabTxt.length, x, y, txtPaint)
    }

    /*
    需要初始化
    获取  需要绘制的bitmap
     */
    private fun getDrawableFromStateDrwableList() {
        if (iconDrawable == null) {
            return
        }
        var checkedDrawable: BitmapDrawable? = null
        var unCheckedDrawable: BitmapDrawable? = null

        val clazz = StateListDrawable::class.java
        val getStateCount: Method = clazz.getDeclaredMethod("getStateCount")//获取drawable中所有状态数量
        val getStateSet: Method = clazz.getDeclaredMethod("getStateSet", Int::class.java) //获取索引处drawable的状态集合
        val getStateDrawable: Method = clazz.getDeclaredMethod("getStateDrawable", Int::class.java)//获取索引处drawable
        val sateCount = getStateCount.invoke(iconDrawable) as Int
        for (index in 0 until sateCount) {
            val stateSet = getStateSet.invoke(iconDrawable, index) as IntArray
            if (stateSet.isEmpty()) {//未选中
                val drawable = getStateDrawable.invoke(iconDrawable, index)
                if (drawable is BitmapDrawable) {
                } else {
                    throw IllegalStateException("tab_icon_drawable is not a bitmap !!!")
                }
                unCheckedDrawable = drawable
                println("drawable::::::::::::::::::::::::::::::::$drawable")
            } else {
                for ((i, state) in stateSet.withIndex()) {
                    //选中状态的drawable
                    if (state == android.R.attr.state_checked) {
                        val drawable = getStateDrawable.invoke(iconDrawable, index) as Drawable
                        println("drawable::::::::::::::::::::::::::::::::$drawable")
                        if (drawable is BitmapDrawable) {
                        } else {
                            throw IllegalStateException("tab_icon_drawable is not a bitmap !!!")
                        }
                        checkedDrawable = drawable
                        break
                    } else if (state == -android.R.attr.state_checked) {//未选中
                        val drawable = getStateDrawable.invoke(iconDrawable, index) as Drawable
                        println("drawable::::::::::::::::::::::::::::::::$drawable")
                        if (drawable is BitmapDrawable) {
                        } else {
                            throw IllegalStateException("tab_icon_drawable is not a bitmap !!!")
                        }
                        unCheckedDrawable = drawable
                        break
                    }
                }
            }
        }
        tabIcon = arrayOf(checkedDrawable, unCheckedDrawable)
    }

}

//扩展方法
fun View.getBitMapDrawable(state: Int, drawable: StateListDrawable): BitmapDrawable? {
    val clazz = StateListDrawable::class.java
    val getStateCount: Method = clazz.getDeclaredMethod("getStateCount")//获取drawable中所有状态数量
    val getStateSet: Method = clazz.getDeclaredMethod("getStateSet", Int::class.java) //获取索引处drawable的状态集合
    val getStateDrawable: Method = clazz.getDeclaredMethod("getStateDrawable", Int::class.java)//获取索引处drawable
    val sateCount = getStateCount.invoke(drawable) as Int
    for (index in 0 until sateCount) {
        val stateSet = getStateSet.invoke(drawable, index) as IntArray
        if (stateSet.isEmpty()) {//未选中
            val drawable = getStateDrawable.invoke(drawable, index)
            if (drawable is BitmapDrawable) {
                return drawable
            } else {
                throw IllegalStateException("tab_icon_drawable is not a bitmap !!!")
            }
        } else {
            for ((i, state) in stateSet.withIndex()) {
                //选中状态的drawable
                if (state == android.R.attr.state_checked) {
                    val drawable = getStateDrawable.invoke(drawable, index) as Drawable
                    println("drawable::::::::::::::::::::::::::::::::$drawable")
                    if (drawable is BitmapDrawable) {
                        return drawable
                    } else {
                        throw IllegalStateException("tab_icon_drawable is not a bitmap !!!")
                    }
                } else if (state == -android.R.attr.state_checked) {//未选中
                    val drawable = getStateDrawable.invoke(drawable, index) as Drawable
                    println("drawable::::::::::::::::::::::::::::::::$drawable")
                    if (drawable is BitmapDrawable) {
                        return drawable
                    } else {
                        throw IllegalStateException("tab_icon_drawable is not a bitmap !!!")
                    }
                }
            }
        }
    }
    return null
}

