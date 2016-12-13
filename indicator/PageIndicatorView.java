package com.randy.easyweather.easyweather.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.randy.easyweather.easyweather.R;
import com.randy.easyweather.easyweather.utils.Utils;

/**
 * Created by randy on 2016/12/11.
 * custom indicators
 */
public class PageIndicatorView extends View{

    private static final String TAG = "PageIndicator";

    //support page count
    private int pageCount;

    //out circle radius
    private float outRadius;

    //inner circle radius
    private float innerRadius;

    //unselected circle color
    private int unSeleColor;

    //selected circle color
    private int seleColor;

    //padding
    private float padding;

    //out circle edge width
    private float edgeWidth;

    //distance between circle
    private float circleDistance;

    //default value dp
    private static final int DEFAULT_OUT_CIRCLE_RADIUS = 5;

    private static final int DEFAULT_INNER_CIRCLE_RADIUS = 3;

    private static final int DEFAULT_DISTANCE_BETWEEN_CIRCLE = 4;

    private static final String DEFAULT_UNSELE_CIRCLE_COLOR = "#d9d9d9";

    private static final String DEFAULT_SELE_CIRCLE_COLOR = "#f7f7f7";

    //paint
    private Paint mOutCirclePaint;
    private Paint mInnerSelCirclePaint;
    private Paint mInnerUnselCirclePaint;
    //selected page pos
    private int selectedPos;

    //draw position
    private int mDrawPos;

    private float mDrawPosOffset;

    //support lifecircle
    private boolean lifecircle = false;

    //contact to ViewPager
    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (lifecircle) {
                mDrawPos = position - 1;
            }else {
                mDrawPos = position;
            }
            mDrawPosOffset = positionOffset;
            invalidate();
        }

        @Override
        public void onPageSelected(int position) {
            if (lifecircle) {
                setSelectedPos(position - 1);
            }else {
                setSelectedPos(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public PageIndicatorView(Context context) {
        this(context, null);
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttributes(attrs);

        initPaint();
    }

    private void initAttributes(AttributeSet attrs){

        if (null == attrs){

            return;
        }

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PageIndicatorView);
        pageCount = a.getInteger(R.styleable.PageIndicatorView_page_count, 1);
        outRadius = a.getFloat(R.styleable.PageIndicatorView_out_circle_radius, Utils.dpToPx(DEFAULT_OUT_CIRCLE_RADIUS, getContext()));
        innerRadius = a.getFloat(R.styleable.PageIndicatorView_inner_circle_radius, Utils.dpToPx(DEFAULT_INNER_CIRCLE_RADIUS, getContext()));
        seleColor = a.getColor(R.styleable.PageIndicatorView_circle_sel_color, Color.parseColor(DEFAULT_SELE_CIRCLE_COLOR));
        unSeleColor = a.getColor(R.styleable.PageIndicatorView_circle_unsel_color, Color.parseColor(DEFAULT_UNSELE_CIRCLE_COLOR));
        padding = a.getDimension(R.styleable.PageIndicatorView_page_padding, 0f);
        edgeWidth = a.getFloat(R.styleable.PageIndicatorView_circle_edge_width, 2f);
        circleDistance = a.getFloat(R.styleable.PageIndicatorView_distance_between_circle, Utils.dpToPx(DEFAULT_DISTANCE_BETWEEN_CIRCLE, getContext()));
        a.recycle();
    }

    private void initPaint(){

        mOutCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerSelCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerUnselCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //这里只有外圆边框存在宽度，内圆为实心圆，无所谓边框宽度
        mOutCirclePaint.setStyle(Paint.Style.STROKE);
        mInnerSelCirclePaint.setStyle(Paint.Style.FILL);
        mInnerUnselCirclePaint.setStyle(Paint.Style.FILL);

        mOutCirclePaint.setColor(seleColor);
        mInnerSelCirclePaint.setColor(seleColor);
        mInnerUnselCirclePaint.setColor(unSeleColor);

        mOutCirclePaint.setStrokeWidth(2f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width;
        int height;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth;
        if (1 == pageCount){
             desiredWidth = widthSize;
        }else {
            //外围加个圆圈，增加了定位的复杂性
            desiredWidth = (int)((pageCount - 1) * circleDistance + pageCount * 2 * innerRadius +
                    (outRadius - innerRadius) * 2 + 2 * edgeWidth + 2 * padding);
        }

        if (widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        }else if (widthMode == MeasureSpec.AT_MOST){
            width = Math.min(widthSize, desiredWidth);
        }else {
            width = desiredWidth;
        }

        int desiredHeight = (int)outRadius;
        if (heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        }else if (heightMode == MeasureSpec.AT_MOST){
            height = Math.min(heightSize, desiredHeight);
        }else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        for (int i = 0; i < pageCount; i++){
            //绘制内部圆
            canvas.drawCircle(padding + i * circleDistance + (2 * i + 1) * innerRadius,
                    height / 2, innerRadius, (i == selectedPos) ? mInnerSelCirclePaint : mInnerUnselCirclePaint);
        }

        if (0 == mDrawPosOffset){
            canvas.drawCircle(padding + innerRadius * (2 * mDrawPos + 1) + mDrawPos * circleDistance, height / 2, outRadius, mOutCirclePaint);
        }else {
            {
                canvas.drawCircle(padding + (2 * mDrawPos + 1) * innerRadius + mDrawPos * circleDistance + (2 * innerRadius + circleDistance) * mDrawPosOffset,
                        height / 2, outRadius, mOutCirclePaint);
//                Log.e(TAG, "padding= " + padding + ", innerRadius= " + innerRadius + ", radius width= " + (2 * mDrawPos + 1) * innerRadius);
//                Log.e(TAG, "mDrawPos= " + mDrawPos + ", mDrawPosOffset= " + mDrawPosOffset + ", circleDistance= " + circleDistance + ", " +
//                        "offset dis= " + (2 * innerRadius + circleDistance) * mDrawPosOffset);
//                Log.e(TAG, "x= " + (padding + (2 * mDrawPos + 1) * innerRadius + (2 * innerRadius + circleDistance) * mDrawPosOffset));
            }
        }

    }

    /**
     * set page count
     * @param pageCount
     */
    public void setPageCount(int pageCount) {
        if (this.pageCount != pageCount){
            this.pageCount = pageCount;
            requestLayout();
        }
    }

    /**
     * set selected position
     * @param selectedPos
     */
    public void setSelectedPos(int selectedPos) {

        if (this.selectedPos != selectedPos){

            this.selectedPos = selectedPos;
            requestLayout();
        }
    }

    /**
     *
     * @param viewPager
     * @param lifecircle true represent support lifecircle;false represent unsupport lifecircle
     */
    public void attachToViewPager(ViewPager viewPager, boolean lifecircle){

        if (null == viewPager){
            return;
        }
        viewPager.addOnPageChangeListener(mOnPageChangeListener);
        if (viewPager.getAdapter() != null){
            if (lifecircle) {
                this.lifecircle = lifecircle;
                setPageCount(viewPager.getAdapter().getCount() - 2);
            }else {
                setPageCount(viewPager.getAdapter().getCount());
            }
        }
    }
}
