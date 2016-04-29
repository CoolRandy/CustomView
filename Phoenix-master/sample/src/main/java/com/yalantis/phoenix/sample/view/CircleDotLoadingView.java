package com.yalantis.phoenix.sample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.yalantis.phoenix.sample.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by admin on 2016/4/29.
 *
 */
public class CircleDotLoadingView extends View {

    private float circleStrokeWidth;
    private float circleRadius;
    private int circleColor;
    private float offset;

    //default
    private static float DEFAULT_CIRCLE_STROKE_WIDTH;
    private static float DEFAULT_CIRCLE_RADIUS;
    private static int DEFAULT_CIRCLE_COLOR;

    private Paint paint;
    private Paint dotPaint;
    //circle radius
    private float cx;
    private float cy;
    private float radius;
    private float dx;
    private float dy;
    private float dRadius;
    private int status = INIT_STATUS;
    private static final int INIT_STATUS = 0;
    private static final int RUNNING_STATUS = 1;

    //关于小圆点在圆环上旋转，可以借助插值器
    private Interpolator interpolator = new AccelerateDecelerateInterpolator();
    private int angleValue = 0;//角度步长

    private float MIN_SIZE;
    private boolean timerIsCanceled = false;

    //定时刷新，可以采用handler或者timer
    //方式1：timer
    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

            updateDotPosition();
        }
    };

    public CircleDotLoadingView(Context context) {
        this(context, null);
    }

    public CircleDotLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleDotLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(context, attrs);

        initView();

    }

    public void initAttrs(Context context, AttributeSet attrs){

        DEFAULT_CIRCLE_STROKE_WIDTH = dp2px(4.0f);
        DEFAULT_CIRCLE_RADIUS = dp2px(50.0f);
        DEFAULT_CIRCLE_COLOR = Color.BLUE;
        offset = dp2px(6.0f);
        dRadius = dp2px(6.0f);

        MIN_SIZE = dp2px(72); //这里的dip2px方法
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleDotLoadingView, 0, 0);
        circleStrokeWidth = a.getDimension(R.styleable.CircleDotLoadingView_circle_stroke_width, DEFAULT_CIRCLE_STROKE_WIDTH);
        circleRadius = a.getDimension(R.styleable.CircleDotLoadingView_circle_radius, DEFAULT_CIRCLE_RADIUS);
        circleColor = a.getColor(R.styleable.CircleDotLoadingView_circle_color, DEFAULT_CIRCLE_COLOR);

        a.recycle();
    }

    public void initView(){

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(circleStrokeWidth);
        paint.setAntiAlias(true);
        paint.setColor(circleColor);

        dotPaint = new Paint();
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(Color.RED);
        dotPaint.setAntiAlias(true);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension((int)MIN_SIZE, (int)MIN_SIZE);
//        } else if (widthMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension((int)MIN_SIZE, heightSize);
//        } else if (heightMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(widthSize, (int)MIN_SIZE);
//        }
//
//    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //获取圆心坐标
//        Log.e("TAG", "padding left=" + getPaddingLeft() + ", padding right=" + getPaddingRight() + ", padding top=" + getPaddingTop() + ", padding bottom=" + getPaddingBottom());
//        Log.e("TAG", "width=" + getWidth() + ", height=" + getHeight());
        cx = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
        cy = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2 + getPaddingTop();
//        Log.e("TAG", "cx=" + cx + ", cy=" + cy);
        //获取圆半径 注意这里减去了一个偏移量，这是因为画笔是有宽度的，圆的半径和外边界刚好相切，如果不减去一个偏移量，会导致上下左右出现不平滑的现象，可以去掉offset试一下效果
        float x_r = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 - DEFAULT_CIRCLE_STROKE_WIDTH / 2 - offset;
        float y_r = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2 - DEFAULT_CIRCLE_STROKE_WIDTH / 2 - offset;
        radius = Math.min(x_r, y_r);
//        Log.e("TAG", "x_r=" + x_r + ", y_r=" + y_r + ", default width=" + DEFAULT_CIRCLE_STROKE_WIDTH + ", offset=" + offset);
        dx = cx;
        //其实这里与理论分析有点不符，按理说应该是getPaddingTop() + DEFAULT_CIRCLE_STROKE_WIDTH/2 + offset
        dy = getPaddingTop() + DEFAULT_CIRCLE_STROKE_WIDTH + offset;
//        Log.e("TAG", "dx=" + dx + ", dy= " + dy);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(cx, cy, radius, paint);
        if (status == RUNNING_STATUS) {
//            Log.e("TAG", "dx=" + dx + ", dy=" + dy + ", dot radius=" + dRadius);
            canvas.drawCircle(dx, dy, dRadius, dotPaint);
        }
    }

    public float dp2px(float dp){
        final float scale = getResources().getDisplayMetrics().density;
        return scale * dp + 0.5f;
    }

    /**
     * 更新小圆点的位置，这里的思路采取借助插值器，并不断重绘的方式更新，当然也可以全部采用属性动画的方式不断移动小圆点的位置
     */
    private void updateDotPosition(){

        //首先将角度换算为弧度
        //input为当前的角度值
        float input = angleValue % 360.0f;
        //采用加减速插值器获取相应的interpolated fraction，位于0~1之间
        float interpolatorValue = interpolator.getInterpolation(input / 360.0f);
        //然后将角度换算为弧度
        double realAngle = interpolatorValue * 2 * Math.PI;
        //获取小圆点的坐标
        dx = cx + (float)(radius * Math.sin(realAngle));
        dy = cy - (float)(radius * Math.cos(realAngle));
        //重绘
        postInvalidate();
        //递增角度
        angleValue = angleValue + 1;
    }

    public void executeTask(){
        if (timerIsCanceled){
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {

                    updateDotPosition();
                }
            };
        }
        timer.schedule(timerTask, 10, 3);//0.01s延迟之后，每个3ms执行一次
        status = RUNNING_STATUS;
    }

    public void stopTask(){
        if (timer != null) {
            timer.cancel();
            timer = null;
            timerIsCanceled = true;
        }
        if (timerTask != null){
            timerTask.cancel();
            timerTask = null;
            timerIsCanceled = true;
        }

    }

}
