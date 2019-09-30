package com.example.p2p.widget.customView;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;



/**
 * 可自由缩放的图片控件
 * Create by 陈健宇 at 2018/8/14
 */
public class ZoomImageView extends AppCompatImageView
        implements
        ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener {

    private final String TAG = ZoomImageView.class.getSimpleName();
    private static final float SCALE_MAX = 3.5f;//最大缩放比例
    private static final float SCALE_MAX_BORDER = 3.0f;//最大缩放边界
    private float mInitScale = 1.0f;//初始化缩放比例
    private static final float SCALE_MIN_BORDER = 0.5f;//最小缩放边界
    private final float[] mMatrixValues = new float[9];//用于存放矩阵的9个值
    private final Matrix mScaleMatrix = new Matrix();//缩放矩阵
    private ScaleGestureDetector mScaleGestureDetector = null;//缩放的手势检测
    private GestureDetector mGestureDetector = null;//双击手势检测
    private int mLastX, mLastY;//上次触摸点坐标平均值
    private int mLastPointerConut;//上次触摸点个数
    private int mIntrinsicWidth, mIntrinsicHeight;
    private boolean isCanDrag;//是否在拖动图片
    private boolean isCheckLeftAndRight, isCheckTopAndBottom;//是否检查了边界
    private boolean isAutoScale;//是否在双击缩放图片
    private boolean isFirst = true;

    public ZoomImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), this);
        /* 双击 */
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isAutoScale) {
                    return true;
                }
                Log.d(TAG, "onDoubleTap，scale：" + getScale() + " initScale: " + mInitScale);
                if (getScale() < SCALE_MAX ) {//放大
                    isAutoScale = true;
                    ZoomImageView.this.postDelayed(new AutoRunnableScale(SCALE_MAX, e.getX(), e.getY()), 15);
                }else{//缩小
                    isAutoScale = true;
                    ZoomImageView.this.postDelayed(new AutoRunnableScale(mInitScale, e.getX(), e.getY()), 15);
                }
                return true;
            }
        });
        this.setOnTouchListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }

    /**
     * 这里进行图片的初始化
     */
    @Override
    public void onGlobalLayout() {
        /* onLayout后回调 */
        /* 进行图片初始化 */
        if (isFirst) {
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }
            isFirst = false;
            Log.d(TAG, "onGlobalLayout: " + drawable.getIntrinsicWidth() + " " + drawable.getIntrinsicHeight());
            Log.d(TAG, "onGlobalLayout: " + getWidth() + " " + getHeight());
            int width = getWidth();
            int height = getWidth();
            int dw = drawable.getIntrinsicWidth();
            int dh = drawable.getIntrinsicHeight();
            float scale = 1.0f;
            /* 如果图片的宽或者高大于屏幕，则缩放至屏幕的宽或者高 */
            if (dw > width && dh <= height)
            {
                scale = width * 1.0f / dw;
            }
            if (dh > height && dw <= width)
            {
                scale = height * 1.0f / dh;
            }
            /* 如果宽和高都大于屏幕，则让其按按比例适应屏幕大小 */
            if (dw > width && dh > height)
            {
                scale = Math.min(dw * 1.0f / width, dh * 1.0f / height);
            }
            mInitScale = scale;
            Log.d(TAG, "onGlobalLayout，mInitScale：" + mInitScale);
            /* 图片移动至屏幕中心 */
            mScaleMatrix.postTranslate((width - dw) / 2, (height - dh) / 2);
            mScaleMatrix.postScale(mInitScale, mInitScale, getWidth() / 2, getHeight() / 2);
            setImageMatrix(mScaleMatrix);
        }
    }

    /**
     * 这里进行图片的缩放
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (getDrawable() == null) {
            return true;
        }
        float scale = getScale();
        float scaleFactor = detector.getScaleFactor();
        Log.d(TAG, "onScale, scale: " + scale + " scaleFactor: " + scaleFactor);
        /* 缩放的范围控制 */
        if ((scale < SCALE_MAX && scaleFactor > 1.0f) || (scale > mInitScale && scaleFactor < 1.0f))
        {
            /* 最大值最小值判断 */
            if (scaleFactor * scale < mInitScale)
            {
                scaleFactor = mInitScale / scale;
            }
            if (scaleFactor * scale > SCALE_MAX)
            {
                scaleFactor = SCALE_MAX / scale;
            }
            /* 设置缩放比例 */
            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    /**
     * 这里进行多点触摸判断
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /* 把事件分发给给GestureDetector处理 */
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        /* 把事件交给ScaleGestureDetector处理 */
        mScaleGestureDetector.onTouchEvent(event);

        int x = 0;
        int y = 0;
        /* 拿到触摸点个数 */
        final int pointerCount = event.getPointerCount();
        /* 计算出每个触摸点x，y坐标的平均值 */
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x = x / pointerCount;
        y = y / pointerCount;
        /* 每当触摸点发生变化时，重置mLasX , mLastY */
        if (mLastPointerConut != pointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }
        mLastPointerConut = pointerCount;
        /* 进行平移图片 */
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouch: ACTION_MOVE");
                float deltaX = x - mLastX;
                float deltaY = y - mLastY;
                Log.d(TAG, "onTouch，deltaX：" + deltaX + " deltaY：" + deltaY);
                if (!isCanDrag) {
                    isCanDrag = Math.sqrt(deltaX * deltaX + deltaY * deltaY) >= ViewConfiguration.getTouchSlop();//判断用户是否在拖动图片
                }
                if (isCanDrag) {//如果在拖动图片
                    RectF rect = getMatrixRectF();
                    Log.d(TAG, "getMatrixRectF: rect.top " + rect.top + " rect.left：" + rect.left);
                    if (getDrawable() != null) {
                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        if (rect.width() < getWidth()) {//如果图片宽度小于屏幕宽度，禁止左右移动
                            deltaX = 0;
                            isCheckLeftAndRight = false;
                        }
                        if (rect.height() < getHeight()) {//如果图片高度小于屏幕高度度，禁止上下移动
                            deltaY = 0;
                            isCheckTopAndBottom = false;
                        }
                        mScaleMatrix.postTranslate(deltaX, deltaY);
                        checkMatrixBounds();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouch: ACTION_UP");
                mLastPointerConut = 0;
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * 拖动图片时进行边界检查, 判断移动或缩放后，图片显示是否超出屏幕边界
     */
    private void checkMatrixBounds() {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;
        if (rect.top > 0 && isCheckTopAndBottom) {//顶部有空隙，上移
            deltaY = -rect.top;
        }
        if (rect.bottom < getHeight() && isCheckTopAndBottom) {//底部有空隙，下移
            deltaY = getHeight() - rect.bottom;
        }
        if (rect.left > 0 && isCheckLeftAndRight) {//左边有空隙，左移
            deltaX = -rect.left;
        }
        if (rect.right < getWidth() && isCheckLeftAndRight) {//右边有空隙，右移
            deltaX = getWidth() - rect.right;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 在缩放时，进行图片显示范围的控制
     */
    private void checkBorderAndCenterWhenScale() {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;
        int width = getWidth();
        int height = getHeight();
        /* 如果宽或高大于屏幕，则控制范围 */
        if (rect.width() >= width) {
            if (rect.left > 0)//如果左边有空隙，左移
            {
                deltaX = -rect.left;
            }
            if (rect.right < width)//如果右边有空隙，右移
            {
                deltaX = width - rect.right;
            }
        }
        if (rect.height() >= height) {
            if (rect.top > 0)//如果顶部有空隙，上移
            {
                deltaY = -rect.top;
            }
            if (rect.bottom < height)//如果底部有空隙，下移
            {
                deltaY = height - rect.bottom;
            }
        }
        /* 如果宽或高小于屏幕，则让其居中 */
        if (rect.width() < width) {
            deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
        }
        if (rect.height() < height) {
            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
        }
        Log.d(TAG, "deltaX = " + deltaX + " , deltaY = " + deltaY);
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);//Matrix 的值映射到RecF中
        }
        return rect;
    }

    /**
     * 获得图形的缩放比例
     *
     * @return
     */
    private final float getScale() {
        mScaleMatrix.getValues(mMatrixValues);
        return mMatrixValues[Matrix.MSCALE_X];
    }

    /**
     * 双击时处理图片的缩放
     */
    class AutoRunnableScale implements Runnable {

        static final float BIGGER = 1.07f;
        static final float SMALLER = 0.93f;
        private float mTargetScale;
        private float tmpScale;
        private float x;
        private float y;

        /**
         * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
         */
        public AutoRunnableScale(float targetScale, float x, float y) {
            this.mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            if (getScale() < mTargetScale) {
                tmpScale = BIGGER;
            } else {
                tmpScale = SMALLER;
            }
        }

        @Override
        public void run()
        {
            // 进行缩放
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
            final float currentScale = getScale();
            //如果值在合法范围内，继续缩放
            if (((tmpScale > 1f) && (currentScale < mTargetScale))
                    || ((tmpScale < 1f) && (mTargetScale < currentScale)))
            {
                ZoomImageView.this.postDelayed(this, 15);
            } else//设置为目标的缩放比例
            {
                final float deltaScale = mTargetScale / currentScale;
                mScaleMatrix.postScale(deltaScale, deltaScale, x, y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }
        }
    }

}
