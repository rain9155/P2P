package com.example.p2p.widget.customView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.p2p.utils.LogUtils;


/**
 * 可自由缩放的图片控件
 * 使用ScaleGestureDetector监听缩放手势
 * 使用GestureDetector监听双击、单击、fling手势
 * Create by 陈健宇 at 2018/8/14
 */
public class ZoomImageView extends AppCompatImageView
        implements
        View.OnLayoutChangeListener,
        View.OnTouchListener {

    private final String TAG = ZoomImageView.class.getSimpleName();
    private final static int EDGE_NONE = 0x000;
    private final static int EDGE_BOTH = 0x001;
    private final static int EDGE_RIGHT = 0x002;
    private final static int EDGE_LEFT = 0x003;
    private static final float MAX_SCALE = 2.0f;
    private static final int AUTO_SCALE_INTERVAL = 10;


    private final float[] mMatrixValues = new float[9];//用于存放图片矩阵的9个值
    private final Matrix mImageMatrix = new Matrix();//控制图片缩放和位置的矩阵
    private boolean isDragging;
    private boolean isScaling;
    private float mInitScale = 1.0f;//初始化缩放比例,等比例缩放
    private int mImageEdge = EDGE_BOTH;//用于判断图片是否到达父容器的左右边界
    private View.OnClickListener mOnClickListener;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;
    private Scroller mFlingScroller;
    private int mLastX, mLastY;
    private int mLastPointerCount;
    private float mScaledTouchSlop;
    private float mMinFlingVelocity;

    public ZoomImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        setScaleType(ScaleType.MATRIX);
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mMinFlingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        mFlingScroller = new Scroller(getContext());

        this.setOnTouchListener(this);
        this.addOnLayoutChangeListener(this);

        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
            /**
             * 这里进行图片的缩放
             */
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (getDrawable() == null) {
                    return true;
                }

                float curImageScale = getCurImageScale();
                float scaleFactor = detector.getScaleFactor();
                LogUtils.d(TAG, "onScale, scaleFactor = " + scaleFactor);
                if ((curImageScale < MAX_SCALE && scaleFactor > 1.0f) //正在放大
                        || (curImageScale > mInitScale && scaleFactor < 1.0f)//正在缩小
                ) {
                    //缩放的范围控制
                    if (scaleFactor * curImageScale < mInitScale) {
                        scaleFactor = mInitScale / curImageScale;
                    }
                    if (scaleFactor * curImageScale > MAX_SCALE){
                        scaleFactor = MAX_SCALE / curImageScale;
                    }
                    //设置缩放比例
                    mImageMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                    checkAndSetImageMatrix();
                }
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                LogUtils.d(TAG, "onScaleBegin");
                isScaling = true;
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                LogUtils.d(TAG, "onScaleEnd");
                isScaling = false;
            }
        });

        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

            //onSingleTapConfirmed和onDoubleTap只会回调一个，即双击的同时不会触发两次单击事件

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                LogUtils.d(TAG, "onSingleTapConfirmed");
                if(mOnClickListener != null){
                    mOnClickListener.onClick(ZoomImageView.this);
                }
                final RectF displayRect = getImageMatrixRect();
                return displayRect.contains(e.getX(), e.getY());
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                LogUtils.d(TAG, "onDoubleTap");
                if (isScaling) {
                    return true;
                }
                if (getCurImageScale() < MAX_SCALE) {//放大
                    isScaling = true;
                    ZoomImageView.this.postDelayed(
                            new AutoScaleRunnable(MAX_SCALE, e.getX(), e.getY()),
                            AUTO_SCALE_INTERVAL);
                }else{//缩小
                    isScaling = true;
                    ZoomImageView.this.postDelayed(
                            new AutoScaleRunnable(mInitScale, e.getX(), e.getY()),
                            AUTO_SCALE_INTERVAL);
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                LogUtils.d(TAG, "onFling,  velocityX = " + velocityX + ", velocityY = " + velocityY);
                if(Math.max(velocityX, velocityY) >= mMinFlingVelocity){
                    ZoomImageView.this.post(new AutoFlingRunnable(e2.getX(), e2.getY(), velocityX, velocityY));
                }
                return false;
            }

        });
    }

    /**
     * 这里进行图片的初始化，居中图片
     * onSizeChanged会在onLayout后回调
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtils.d(TAG, "onSizeChanged，w = " + w + ", ow = " + oldw);
        if(w != oldw || h != oldh){
            resetImageMatrix(getDrawable());
        }
    }

    /**
     * 当前控件的边界坐标改变时，会调用，此时要重置图片
     */
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        LogUtils.d(TAG, "onLayoutChange, left = " + left + ", oLeft = " + oldLeft);
        if(left != oldLeft || right != oldRight || bottom != oldBottom || top != oldTop){
            resetImageMatrix(getDrawable());
        }
    }

    /**
     * 这里进行多点触摸判断
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(getDrawable() == null) return false;

        //把事件分发给给GestureDetector处理
        if (mGestureDetector != null && mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        //把事件交给ScaleGestureDetector处理
        if(mScaleGestureDetector != null){
            mScaleGestureDetector.onTouchEvent(event);
        }

        boolean handle = false;
        int touchX = 0;
        int touchY = 0;

        //拿到触摸点个数
        final int pointerCount = event.getPointerCount();
        //计算出每个触摸点x，y坐标的平均值
        for (int i = 0; i < pointerCount; i++) {
            touchX += event.getX(i);
            touchY += event.getY(i);
        }
        touchX = touchX / pointerCount;
        touchY = touchY / pointerCount;

        //每当触摸点发生变化时，重置mLasX , mLastY
        if (mLastPointerCount != pointerCount) {
            mLastX = touchX;
            mLastY = touchY;
        }
        mLastPointerCount = pointerCount;

        //进行平移图片
        ViewParent parent;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //首先父容器不能拦截接下来的事件，子控件要消耗ACTION_DOWN事件，这样才能保证接下来子控件能收到事件
                 parent = v.getParent();
                if(parent != null){
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                handle = true;
                isDragging = false;
                mFlingScroller.forceFinished(true);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //触摸点数置零
                mLastPointerCount = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                float dX = touchX - mLastX;
                float dY = touchY - mLastY;
                if (!isDragging) {
                    isDragging = Math.sqrt(dX * dX + dY * dY) >= mScaledTouchSlop;//判断用户是否在拖动图片
                }
                if(isDragging && !isScaling) {
                    mImageMatrix.postTranslate(dX, dY);
                    checkAndSetImageMatrix();
                    parent = getParent();
                    //判断图片是否拖动到父容器边界，如果是，把下一个事件交给父容器
                    if(mImageEdge == EDGE_BOTH
                            || (mImageEdge == EDGE_RIGHT && dX <= -1f)
                            || (mImageEdge == EDGE_LEFT && dX >= 1f)){
                        if(parent != null){
                            parent.requestDisallowInterceptTouchEvent(false);
                        }
                    }else {
                        if(parent != null){
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    handle = true;
                }
                mLastX = touchX;
                mLastY = touchY;
                break;
            default:
                break;
        }
        return handle;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeOnLayoutChangeListener(this);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        resetImageMatrix(drawable);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        resetImageMatrix(getDrawable());
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        resetImageMatrix(getDrawable());
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mOnClickListener = l;
    }

    /**
     * 检查边界并设置图片的Matrix
     */
    private void checkAndSetImageMatrix() {
        checkImageBounds();
        setImageMatrix(mImageMatrix);
    }

    /**
     * 在缩放和拖动时，进行图片显示范围的控制
     */
    private void checkImageBounds() {
        RectF rect = getImageMatrixRect();
        float deltaX = 0;
        float deltaY = 0;
        int width = getWidth();
        int height = getHeight();
        //如果图片缩放的宽或高小于屏幕，则让其居中
        if (rect.width() < width) {
            mImageEdge = EDGE_BOTH;
            deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
        }
        if (rect.height() < height) {
            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
        }
        //如果图片缩放的宽或高大于屏幕，则控制范围，哪里有空隙就往哪边移动
        if (rect.width() >= width) {
            mImageEdge = EDGE_NONE;
            if (rect.left > 0) {//如果左边有空隙，左移
                mImageEdge = EDGE_LEFT;
                deltaX = -rect.left;
            }
            if (rect.right < width) {//如果右边有空隙，右移
                mImageEdge = EDGE_RIGHT;
                deltaX = width - rect.right;
            }
        }
        if (rect.height() >= height) {
            if (rect.top > 0) {//如果顶部有空隙，上移
                deltaY = -rect.top;
            }
            if (rect.bottom < height) {//如果底部有空隙，下移
                deltaY = height - rect.bottom;
            }
        }
        mImageMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 重置ImageMatrix
     */
    private void resetImageMatrix(Drawable drawable) {
        if(drawable == null) return;
        mImageMatrix.reset();
        int width = getWidth();
        int height = getHeight();
        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();
        mInitScale = computeInitScale(imageWidth, imageHeight, width, height);
        // 图片移动至屏幕中心
        mImageMatrix.postTranslate((width - imageWidth) / 2f, (height - imageHeight) / 2f);
        //图片等比例缩放
        mImageMatrix.postScale(mInitScale, mInitScale, width / 2f, height / 2f);
        setImageMatrix(mImageMatrix);
    }


    /**
     * 计算初始化缩放比例，等比例缩放
     */
    private float computeInitScale(int imageWidth, int imageHeight, int width, int height) {
        float scale = 1.0f;
        if (imageWidth > width && imageHeight > height) { // 如果图片宽和高都大于屏幕，则让其按比例适应屏幕大小

            scale = Math.min(width * 1.0f / imageWidth, height * 1.0f / imageHeight);

        }else if(imageWidth > width){//如果图片只是宽大于屏幕

            scale = width * 1.0f / imageWidth;

        }else if(imageHeight > height){//如果图片只是高大于屏幕

            scale = height * 1.0f / imageHeight;

        }
        return scale;
    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     */
    private RectF getImageMatrixRect() {
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            mImageMatrix.mapRect(rect);//Matrix 的值映射到RecF中
        }
        return rect;
    }

    /**
     * 获得当前图形的缩放比例
     */
    private float getCurImageScale() {
        mImageMatrix.getValues(mMatrixValues);
        return mMatrixValues[Matrix.MSCALE_X];
    }

    /**
     * fling时处理图片移动
     */
    class AutoFlingRunnable implements Runnable{

        private int mLastX;
        private int mLastY;

        public AutoFlingRunnable(float x, float y, float velocityX, float velocityY){
            fling(x, y, velocityX, velocityY);
        }

        private void fling(float x, float y, float velocityX, float velocityY){

            RectF displayRect = getImageMatrixRect();
            float imageWidth = displayRect.width();
            float imageHeight = displayRect.height();
            float width = getWidth();
            float height = getHeight();

            int startX = Math.round(x);
            int startY = Math.round(y);
            int vX = Math.round(velocityX);
            int vY = Math.round(velocityY);
            int maxX, maxY;

            if(imageWidth <= width){
                maxX = startX;
            }else {
                maxX = Math.round(imageWidth - width);
            }

            if(imageHeight <= height){
                maxY = startY;
            }else {
                maxY = Math.round(imageHeight - height);
            }

            if(startX != maxX || startY != maxY){
                mFlingScroller.fling(startX, startY, vX, vY, 0, maxX, 0, maxY);
            }
            LogUtils.d(TAG,
                    "mFlingScroller, startX = " + mFlingScroller.getStartX() + ", startY = " + mFlingScroller.getStartY()
                            + ", finalX = " + mFlingScroller.getFinalX() + ", finalY = " + mFlingScroller.getFinalY());
            mLastX = startX;
            mLastY = startY;
        }

        @Override
        public void run() {
            if(mFlingScroller.isFinished()) return;
            if(mFlingScroller.computeScrollOffset()){
                int curX = mFlingScroller.getCurrX();
                int curY = mFlingScroller.getCurrY();
                int dX = curX - mLastX;
                int dY = curY - mLastY;
                mImageMatrix.postTranslate(dX, dY);
                checkAndSetImageMatrix();
                mLastX = curX;
                mLastY = curY;
                ZoomImageView.this.post(this);
            }
        }
    }

    /**
     * 双击时处理图片缩放
     */
    class AutoScaleRunnable implements Runnable {

        static final float BIGGER = 1.07f;
        static final float SMALLER = 0.93f;
        private float mTargetScale;
        private float mTempScale;
        private float x;
        private float y;

        /**
         * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
         */
        public AutoScaleRunnable(float targetScale, float x, float y) {
            this.mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            if (getCurImageScale() < mTargetScale) {
                mTempScale = BIGGER;
            } else {
                mTempScale = SMALLER;
            }
        }

        @Override
        public void run()
        {
            // 进行缩放
            mImageMatrix.postScale(mTempScale, mTempScale, x, y);
            checkAndSetImageMatrix();
            final float currentScale = getCurImageScale();
            if (
                    ((mTempScale > 1f) && (currentScale < mTargetScale)) ||
                    ((mTempScale < 1f) && (mTargetScale < currentScale))
            ) { //如果值在合法范围内，继续缩放
                ZoomImageView.this.postDelayed(this, AUTO_SCALE_INTERVAL);
            } else{//设置为目标的缩放比例
                final float deltaScale = mTargetScale / currentScale;
                mImageMatrix.postScale(deltaScale, deltaScale, x, y);
                checkAndSetImageMatrix();
                isScaling = false;
            }
        }
    }

}
