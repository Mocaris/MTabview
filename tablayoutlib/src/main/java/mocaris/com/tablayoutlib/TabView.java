package mocaris.com.tablayoutlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by mocaris on 2017/11/17.
 * tabview 可以自定义图标和字体大小
 */

@SuppressLint("AppCompatCustomView")
public class TabView extends View implements View.OnClickListener {
    //提示文字颜色
    private int tipTxtColor;
    //提示点大小
    private float tipRadius;
    //提示点文字大小
    private float tipTxtSize = 10;
    //提示点文字
    private String tipTxt;
    //是否显示提示点
    private boolean showTip = false;
    //提示点颜色
    private int tipColor = Color.RED;
    //默认图标
    private Bitmap mIcon;
    //选中图标
    private Bitmap mSelectIcon;

    private Bitmap mCurrentIcon = mIcon;

    private boolean isChecked = false;
    //图片标宽度
    private int mIconWidth;
    //图标高度
    private int mIconHeight;

    private String mText = "";
    //文字大小
    private int mTextSize;
    //文字颜色
    private int mTextColor;
    private int mTextSelectColor;
    //间距
    private int mInterval;

    private Paint mTextPaint;
    private Rect mTextBounds;
    private Rect mIconRect;
    private OnCheckedChangeListener mListener;
    private Paint tipPaint;


    public TabView(Context context) {
        this(context, null);
    }

    public TabView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TabView);

        //图标大小
        mIconWidth = (int) typedArray.getDimension(R.styleable.TabView_icon_width, getDp(15));
        mIconHeight = (int) typedArray.getDimension(R.styleable.TabView_icon_height, getDp(15));
        //图标和文字之间的间距
        mInterval = (int) typedArray.getDimension(R.styleable.TabView_icon_txt_interval, 0);
        //图标
        mIcon = ((BitmapDrawable) typedArray.getDrawable(R.styleable.TabView_tab_icon)).getBitmap();
//        Drawable drawable = typedArray.getDrawable(R.styleable.TabView_tab_icon_);
        if (null == mIcon) {
            throw new NullPointerException("app:tab_icon not found !");
        }
        //图标
        mSelectIcon = ((BitmapDrawable) typedArray.getDrawable(R.styleable.TabView_tab_select_icon)).getBitmap();
        if (null == mSelectIcon) {
            throw new NullPointerException("app:tab_select_icon not found !");
        }
        //文字
        mText = typedArray.getString(R.styleable.TabView_tab_text);
        //文字颜色
        mTextColor = typedArray.getColor(R.styleable.TabView_tab_text_color, Color.GRAY);
        //文字颜色
        mTextSelectColor = typedArray.getColor(R.styleable.TabView_tab_text_select_color, Color.RED);
        //文字大小
        mTextSize = (int) typedArray.getDimension(R.styleable.TabView_tab_text_size, 12);
        //选中状态
        isChecked = typedArray.getBoolean(R.styleable.TabView_tab_checked, false);
        //提示点的颜色
        tipColor = typedArray.getColor(R.styleable.TabView_tip_circle_color, Color.RED);
        //提示文字颜色
        tipTxtColor = typedArray.getColor(R.styleable.TabView_tip_txt_color, Color.WHITE);
        //是否显示提示点
        showTip = typedArray.getBoolean(R.styleable.TabView_show_tip_circle, false);
        //提示点数字
        tipTxt = typedArray.getString(R.styleable.TabView_tip_txt);
        //提示文字大小
        tipTxtSize = typedArray.getDimension(R.styleable.TabView_tip_txt_size, 10);
        //提示点大小
        tipRadius = typedArray.getDimension(R.styleable.TabView_tip_radius, getDp(8));
        typedArray.recycle();
        tipPaint = new Paint();
        tipPaint.setAntiAlias(true);
        mTextPaint = new Paint();
        //文字大小
        mTextPaint.setTextSize(mTextSize);
        //文字颜色
        mTextPaint.setColor(isChecked ? mTextSelectColor : mTextColor);
        //抗锯齿
        mTextPaint.setAntiAlias(true);
        mTextBounds = new Rect();
        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
        setOnClickListener(this);
    }


    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.checked = this.isChecked;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (state instanceof SavedState) {
            this.isChecked = ((SavedState) state).checked;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        int min = Math.min(getMeasuredWidth() - getPaddingLeft() - getPaddingRight()
//                , getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - mInterval - mTextBounds.height());
//        //图标大小
//        mIconSize = Math.min(mIconSize, min);

        int left = (getMeasuredWidth()) / 2
                - mIconWidth / 2 + getPaddingLeft() - getPaddingRight();
        int top = getMeasuredHeight() / 2
                - (mIconHeight + mInterval + mTextBounds.height()) / 2
                + getPaddingTop()
                - getPaddingBottom();
        //图标位置
        mIconRect = new Rect(left, top, left + mIconWidth, top + mIconHeight);
//        Logger.i("mIconSize :" + mIconSize);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCurrentIcon = isChecked ? mSelectIcon : mIcon;
        mTextPaint.setColor(isChecked ? mTextSelectColor : mTextColor);
        drawIcon(canvas);
        drawText(canvas);
        drawTip(canvas);
    }

    //画提示点
    private void drawTip(Canvas canvas) {
        if (showTip) {
            //画圆点
            int x = (int) (getMeasuredWidth() - tipRadius) - getPaddingRight();
            int y = (int) tipRadius + getPaddingTop();
            tipPaint.setColor(tipColor);
            canvas.drawCircle(x, y, tipRadius, tipPaint);
            // 画圆点上的字
            if (!TextUtils.isEmpty(tipTxt)) {
                Rect txtBound = new Rect();
                tipPaint.setTextSize(tipTxtSize);
                tipPaint.getTextBounds(tipTxt, 0, tipTxt.length(), txtBound);
                tipPaint.setColor(tipTxtColor);
                Paint.FontMetrics fontMetrics = tipPaint.getFontMetrics();
                int tx = (int) (getMeasuredWidth() - getPaddingRight() - txtBound.width() / 2 - tipRadius);
                int ty = (int) (getPaddingTop() + tipRadius + (Math.abs(fontMetrics.ascent) - Math.abs(fontMetrics.descent)) / 2);
                canvas.drawText(tipTxt, tx, ty, tipPaint);
            }
        }
    }

    //画图标
    private void drawIcon(Canvas canvas) {
        canvas.drawBitmap(mCurrentIcon, null, mIconRect, null);
    }

    //画文字
    private void drawText(Canvas canvas) {
        float x = getMeasuredWidth() / 2 - mTextBounds.width() / 2 + getPaddingLeft() - getPaddingRight();
//        float y = getPaddingTop() + mIconRect.height() + mInterval + mTextBounds.height();
        float y = mIconRect.bottom + mInterval + mTextBounds.height();
        canvas.drawText(mText, x, y, mTextPaint);
    }

    /**
     * 重绘
     */
    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mListener = listener;
    }

    /**
     * 是否选中
     *
     * @param isChecked
     */
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        if (null != mListener) {
            mListener.onChecked(this, isChecked);
        }
        invalidateView();
    }

    @Override
    public void onClick(View v) {
        if (!isChecked) {
            setChecked(true);
        }
    }

    /**
     * tab文字
     *
     * @param txt
     */
    public void setTabTxt(String txt) {
        this.mText = txt;
        postInvalidate();
    }

    /**
     * 是否显示提示点
     *
     * @param showTip
     */
    public void showTip(boolean showTip) {
        this.showTip = showTip;
        postInvalidate();
    }

    /**
     * 提示点文字颜色
     *
     * @param color
     */
    public void setTipTxtColor(int color) {
        this.tipTxtColor = color;
        postInvalidate();
    }

    /**
     * 提示点颜色
     *
     * @param color
     */
    public void setTipColor(int color) {
        this.tipColor = color;
        postInvalidate();
    }

    /**
     * 提示文字
     *
     * @param tip
     */
    public void setTipTxt(String tip) {
        this.tipTxt = tip;
        postInvalidate();
    }

    /**
     * 提示点大小
     *
     * @param tipRadius
     */
    public void setTipRadius(int tipRadius) {
        this.tipRadius = tipRadius;
        postInvalidate();
    }

    /**
     * 提示点文字大小
     *
     * @param tipTxtSize
     */
    public void setTipTxtSize(int tipTxtSize) {
        this.tipTxtSize = tipTxtSize;
        postInvalidate();
    }


    public interface OnCheckedChangeListener {
        void onChecked(TabView tab, boolean isChecked);
    }


    private float getDp(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }


    public static class SavedState extends BaseSavedState {
        boolean checked;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            this.checked = source.readInt() == 1 ? true : false;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(checked ? 1 : 0);
        }

        @Override
        public int describeContents() {
            return super.describeContents();
        }

        public static final Creator CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
