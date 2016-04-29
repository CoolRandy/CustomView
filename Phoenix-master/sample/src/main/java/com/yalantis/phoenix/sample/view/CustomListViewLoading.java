package com.yalantis.phoenix.sample.view;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yalantis.phoenix.sample.R;

//TODO 有两个小问题需要解决：1、当刚好将下拉头拉出显示在屏幕上，这时立即松手，界面会停留在那里不动  2、下拉正在刷新的过程页面会有闪动的情况
/**
 * Created by admin on 2016/4/27.
 *
 */
public class CustomListViewLoading extends LinearLayout implements View.OnTouchListener{

    //首先定义几个状态
    private static final int PULL_TO_REFRESH_STATUS = 0;//下拉刷新状态

    private static final int RELEASE_TO_REFRESH_STATUS = 1;//释放立即刷新

    private static final int REFRESHING_STATUS = 2;//正在刷新

    private static final int REFRESH_FINISHED_STATUS = 3;//刷新结束状态

    //标记当前状态
    private int currStatus = REFRESH_FINISHED_STATUS;
    //标记上次状态
    private int lastStatus = currStatus;

    //当手指下拉一定距离，向下的箭头逆时针旋转变为向上的箭头，这里采用动画处理

    //下拉刷新的过程在实际应用中即从网络加载数据的过程，一般采用异步线程发起网络请求，请求成功后通过回调的方式返回数据
    //所以这里需要定义一个刷新操作的回调监听.这里刷新操作可能成功，也可能失败
    private PullToRefreshListener listener;

    //ImageView 显示加载的icon  TextView显示加载文字  ProgressBar加载进度圈
    private ImageView iconImage;
    private TextView loadText;
    private ProgressBar loadProgress;
    //下拉头的View
    private View headerView;

    //标志是否已经加载过一次layout，避免重复加载
    private boolean loadOnce;

    //headerView的高度
    private int headerHeight;
    //headerView的布局参数
    private MarginLayoutParams headerLayoutParams;
    //ListView
    private ListView listView;
    //标志能否下拉
    private boolean ableToPull;
    //上次点击的y坐标
    private float lastY;
    //当前点击的y坐标
    private float currY;
    //touchSlop 系统能够识别滑动的最小值
    private int touchSlop;

    public CustomListViewLoading(Context context) {
        this(context, null);
    }

    public CustomListViewLoading(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

//    @TargetApi(11)
//    public CustomListViewLoading(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        initView(context);
//    }

    /**
     * 初始化view
     */
    private void initView(Context context){

        headerView = LayoutInflater.from(context).inflate(R.layout.down_loading_headerview, null, true);
        iconImage = (ImageView)headerView.findViewById(R.id.arrow_icon);
        loadText = (TextView)headerView.findViewById(R.id.loading_text);
        loadProgress = (ProgressBar)headerView.findViewById(R.id.loading_progress);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setOrientation(VERTICAL);//设置布局为垂直
        addView(headerView, 0);//将headerView添加到布局顶部
    }

    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 下拉头headerView在刷新结束后向上偏移隐藏，需要重新布局
     * @param changed 用于指示view的布局是否发生改变
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !loadOnce){
            //这里采用一种比较巧妙的方式来隐藏headerView，利用marginTop设置为负值即跑到屏幕显示的上方了
            headerHeight = -headerView.getHeight();
            Log.e("TAG", "header height: " + headerHeight);
            headerLayoutParams = (MarginLayoutParams)headerView.getLayoutParams();
            headerLayoutParams.topMargin = headerHeight;

            //获取listView实例
            listView = (ListView)getChildAt(1);
            //给listView设置触摸监听
            listView.setOnTouchListener(this);
            loadOnce = true;
        }
    }

    /**
     * 监听触摸事件
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //首先需要判断能否下拉，也就是说listView是否上滑到顶部，listView的第一个item是否可见
        ableToPull = !canChildScrollUp();
//        setIsAbleToPull(event);
        Log.e("TAG", "able to pull: " + ableToPull);
        if (ableToPull) {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    Log.e("TAG", "action down");
                    lastY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.e("TAG", "action move");
                    currY = event.getRawY();
                    int distanceY = (int)(currY - lastY);
                    if (distanceY <= 0 && headerLayoutParams.topMargin <= headerHeight){
                        //如果是向上滑动或没有滑动，或者headerView距离top的距离大于headerView的高度（即headerView完全隐藏）
                        // （注意这里的headerHeight是负值，所以一旦下拉头部分显示，topMargin值就会大于headerHeight）
                        return false;
                    }
                    if (distanceY < touchSlop){//判断没有滑动
                        return false;
                    }

                    //这里定义headerView刚好完全下拉显示时为临界点，继续下拉则由“下拉刷新”切换为“释放立即刷新”状态
                    if (currStatus != REFRESHING_STATUS){
                        if (headerLayoutParams.topMargin > 0){
                            //headerView没有完全显示时该值始终为负数
                            currStatus = RELEASE_TO_REFRESH_STATUS;
                        }else {
                            currStatus = PULL_TO_REFRESH_STATUS;
                        }
                        headerLayoutParams.topMargin = (distanceY / 2) + headerHeight;
                        Log.e("TAG", "before action up topMargin: " + headerLayoutParams.topMargin);
                        headerView.setLayoutParams(headerLayoutParams);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                default:
                    Log.e("TAG", "action up");
                    //一旦松手，即释放开始刷新；
                    if (currStatus == RELEASE_TO_REFRESH_STATUS){
                        //执行刷新任务，也即执行网络请求相关任务  这里采用AsyncTask结合sleep模拟请求
                        Log.e("TAG", "释放刷新");
                        new RefreshTask().execute();

                    }else if (currStatus == PULL_TO_REFRESH_STATUS){
                        //直接反弹隐藏下拉头
                        Log.e("TAG", "释放恢复");
                        new HideHeaderTask().execute();
                    }
                    break;
            }
            //在下拉的一些操作需要根据不同状态更新headerView控件显示
            if (currStatus == RELEASE_TO_REFRESH_STATUS || currStatus == PULL_TO_REFRESH_STATUS){
                //更新下拉头信息
                updateHeaderView();
                listView.setFocusable(false);
                listView.setPressed(false);
                listView.setFocusableInTouchMode(false);
                lastStatus = currStatus;
                return true;
            }
        }
        return false;
    }

    /**
     * 更新下拉头信息
     */
    public void updateHeaderView(){

        if (lastStatus != currStatus){
            if (currStatus == PULL_TO_REFRESH_STATUS){

                loadText.setText("下拉刷新");
                iconImage.setVisibility(VISIBLE);
                loadProgress.setVisibility(GONE);
                //旋转箭头，采用动画
                rotateArrow(180f, 360f);
            }else if (currStatus == RELEASE_TO_REFRESH_STATUS){

                loadText.setText("释放立即刷新");
                iconImage.setVisibility(VISIBLE);
                loadProgress.setVisibility(GONE);
                rotateArrow(0f, 180f);
            }else if (currStatus == REFRESHING_STATUS){
                loadText.setText("正在刷新...");
                loadProgress.setVisibility(VISIBLE);
                iconImage.clearAnimation();
                iconImage.setVisibility(GONE);
            }
        }
    }

    private void rotateArrow(float start, float end){
        float pivotX = iconImage.getWidth()/2f;
        float pivotY = iconImage.getHeight()/2f;
        Animation rotateAnimation = new RotateAnimation(start, end, pivotX, pivotY);//以基准点（pivotX, pivotY）进行旋转
        rotateAnimation.setDuration(80);
        rotateAnimation.setFillAfter(true);
        iconImage.startAnimation(rotateAnimation);
    }

    /**
     * 判断listView是否能上滑
     * @return
     */
    public boolean canChildScrollUp(){
        if (listView.getChildCount() > 0) {
            View firstChild = listView.getChildAt(0);//获取listView第一个item
            if (listView.getFirstVisiblePosition() > 0 || firstChild.getTop() < listView.getPaddingTop()){
                if (headerLayoutParams.topMargin != headerHeight){
                    headerLayoutParams.topMargin = headerHeight;
                    headerView.setLayoutParams(headerLayoutParams);
                }
                return true;
            }else {

                return false;
            }
        }else {
            //listView没有子view 即不能上滑，但是仍然可以下拉，所以返回false
            return false;
        }

    }

    class RefreshTask extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            int topMargins = headerLayoutParams.topMargin;
            Log.e("TAG", "top margin: " + topMargins);
            while (true){

                topMargins = topMargins - 20;//这里的处理其实不太好？？
                if (topMargins < 0){
                    topMargins = 0;
                    break;
                }
                publishProgress(topMargins);
                try {
                    Thread.sleep(5);
                }catch (Exception e){
                    e.printStackTrace();
                }
                currStatus = REFRESHING_STATUS;
                publishProgress(0);
                if (listener != null){//实际应用中可以根据具体的网络请求返回值进行不同方法的回调
                    listener.OnRefreshSuccessed();//异步执行
                }
            }
            publishProgress(topMargins);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            updateHeaderView();
            headerLayoutParams.topMargin = values[0];
            headerView.setLayoutParams(headerLayoutParams);
        }
    }

    /**
     * 隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏。
     *
     * @author guolin
     */
    class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            int topMargin = headerLayoutParams.topMargin;
            while (true) {
                topMargin = topMargin - 20;
                if (topMargin <= headerHeight) {
                    topMargin = headerHeight;
                    break;
                }
                publishProgress(topMargin);
                try {
                    Thread.sleep(5);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return topMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... topMargin) {
            headerLayoutParams.topMargin = topMargin[0];
            headerView.setLayoutParams(headerLayoutParams);
        }

        @Override
        protected void onPostExecute(Integer topMargin) {
            headerLayoutParams.topMargin = topMargin;
            headerView.setLayoutParams(headerLayoutParams);
            currStatus = REFRESH_FINISHED_STATUS;
        }
    }

    //设置监听方法
    public void setPullToRefreshListener(PullToRefreshListener listener){

        this.listener = listener;
    }

    public interface PullToRefreshListener{

        void OnRefreshSuccessed();

//        void OnRefreshFailed();
    }

    //开放一个接口用于停止刷新状态
    public void finishRefreshing(){

        currStatus = REFRESH_FINISHED_STATUS;
        new HideHeaderTask().execute();
    }
}
