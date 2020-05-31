package com.example.p2p.widget.customView
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.*
import android.widget.OverScroller
import androidx.appcompat.widget.AppCompatImageView
import java.io.File
import java.lang.IllegalArgumentException
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * 可自由缩放的图片控件
 * 使用ScaleGestureDetector监听缩放手势
 * 使用GestureDetector监听双击、单击、fling手势
 * Create by 陈健宇 at 2020/5/31
 */
class ZoomImageView(context: Context?, attrs: AttributeSet?) : AppCompatImageView(context, attrs),
        View.OnLayoutChangeListener,
        View.OnTouchListener {

    companion object {
        private const val EDGE_NONE = 0x000
        private const val EDGE_BOTH = 0x001
        private const val EDGE_RIGHT = 0x002
        private const val EDGE_LEFT = 0x003
        private const val MIN_SCALE = 1.0f
        private const val MAX_SCALE = 3.0f
        private const val AUTO_SCALE_INTERVAL = 10L
        private const val AUTO_FLING_INTERVAL = 15L
        private const val SCALE_BIGGER = 1.07f
        private const val SCALE_SMALLER = 0.93f
    }

    private val TAG = ZoomImageView::class.java.simpleName
    private val mTempMatrixValues = FloatArray(9)
    private val mTempImageMatrix = Matrix()
    private val mTempRect = RectF()
    private val mInitMatrix = Matrix()//图片的初始矩阵
    private val mShapeMatrix = Matrix()//控制图片位置和大小的矩阵
    private var mImageEdge = EDGE_BOTH //用于判断图片是否到达父容器的左右边界
    private var mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    private var mMinFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity.toFloat()
    private val mSupportScaleTypes = arrayOf(ScaleType.CENTER_INSIDE, ScaleType.FIT_CENTER)
    private var mScaleType = ScaleType.FIT_CENTER
    private var mScaleGestureDetector: ScaleGestureDetector
    private var mGestureDetector: GestureDetector
    private var mFlingScroller = OverScroller(context)
    private var mOnClickListener: OnClickListener? = null
    private var isDragging = false
    private var isScaling = false
    private var isFling = false
    private var mLastX = 0
    private var mLastY = 0
    private var mLastPointerCount = 0

    init {
        scaleType = ScaleType.MATRIX

        setOnTouchListener(this)
        addOnLayoutChangeListener(this)

        mScaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {

            /**
             * 这里进行图片的缩放
             */
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (drawable == null) {
                    return true
                }

                val curImageScale = curImageScale
                var scaleFactor = detector.scaleFactor

                if(scaleFactor > 1.0f){//正在放大
                    if(scaleFactor * curImageScale > MAX_SCALE){
                        scaleFactor = MAX_SCALE / curImageScale
                    }
                }
                if(scaleFactor < 1.0f){//正在缩小
                    if(scaleFactor * curImageScale < MIN_SCALE){
                        scaleFactor = MIN_SCALE / curImageScale
                    }
                }

                //设置缩放比例
                mShapeMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                checkAndUpdateImageMatrix()
                
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                isScaling = true
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                isScaling = false
            }
        })
        mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            /**
             * onSingleTapConfirmed和onDoubleTap只会回调一个，即双击的同时不会触发两次单击事件
             */
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                mOnClickListener?.onClick(this@ZoomImageView)
                return isPointInImageDisplayRect(e)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (isScaling) {
                    return true
                }
                if (curImageScale < MAX_SCALE) {//放大
                    isScaling = true
                    postDelayed(
                            AutoScaleRunnable(MAX_SCALE, e.x, e.y), AUTO_SCALE_INTERVAL)
                } else { //缩小
                    isScaling = true
                    postDelayed(
                            AutoScaleRunnable(MIN_SCALE, e.x, e.y), AUTO_SCALE_INTERVAL)
                }
                return true
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                super.onFling(e1, e2, velocityX, velocityY)
                if (abs(velocityX).coerceAtMost(abs(velocityY)) >= mMinFlingVelocity) {
                    isFling = true
                    postDelayed(AutoFlingRunnable(velocityX, velocityY), AUTO_FLING_INTERVAL)
                }
                return false
            }
        })
    }

    /**
     * 这里进行图片的初始化，居中图片，onSizeChanged会在onLayout后回调
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            initImageMatrix(drawable)
        }
    }

    /**
     * 当前控件的边界坐标改变时，会调用，此时要重置图片
     */
    override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        if (left != oldLeft || right != oldRight || bottom != oldBottom || top != oldTop) {
            initImageMatrix(drawable)
        }
    }

    /**
     * 这里进行多点触摸判断
     */
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (drawable == null) {
            return false
        }

        //把事件分发给给GestureDetector处理
        if (mGestureDetector.onTouchEvent(event)) {
            return true
        }
        //把事件交给ScaleGestureDetector处理
        mScaleGestureDetector.onTouchEvent(event)

        //处理图片移动
        return handleMove(event, v)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeOnLayoutChangeListener(this)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        initImageMatrix(drawable)
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        initImageMatrix(drawable)
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        initImageMatrix(drawable)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mOnClickListener = l
    }

    /**
     * 设置ScaleType
     */
    fun setImageScaleType(type: ScaleType) {
        if(!mSupportScaleTypes.contains(type)){
            throw IllegalArgumentException("unSupport")
        }
        if(type != mScaleType){
            mScaleType = type
            initImageMatrix(drawable)
        }
    }

    /**
     * 处理图片的滑动
     */
    private fun handleMove(event: MotionEvent, v: View): Boolean {
        var handle = false
        var touchX = 0
        var touchY = 0

        //拿到触摸点个数
        val pointerCount = event.pointerCount
        //计算出每个触摸点x，y坐标的平均值
        for (i in 0 until pointerCount) {
            touchX += event.getX(i).toInt()
            touchY += event.getY(i).toInt()
        }
        touchX /= pointerCount
        touchY /= pointerCount

        //每当触摸点发生变化时，重置mLasX , mLastY
        if (mLastPointerCount != pointerCount) {
            mLastX = touchX
            mLastY = touchY
        }
        mLastPointerCount = pointerCount

        //进行平移图片
        val parent: ViewParent?
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //首先父容器不能拦截接下来的事件，子控件要消耗ACTION_DOWN事件，这样才能保证接下来子控件能收到事件
                parent = v.parent
                parent?.requestDisallowInterceptTouchEvent(true)
                handle = true
                mFlingScroller.abortAnimation()
                isFling = false
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                //触摸点数置零
                mLastPointerCount = 0
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dX = touchX - mLastX.toFloat()
                val dY = touchY - mLastY.toFloat()
                if (!isDragging) {
                    isDragging = sqrt(dX * dX + dY * dY.toDouble()) >= mScaledTouchSlop //判断用户是否在拖动图片
                }
                if (isDragging && !isScaling && !isFling) {
                    mShapeMatrix.postTranslate(dX, dY)
                    checkAndUpdateImageMatrix()
                    parent = getParent()
                    //判断图片是否拖动到父容器边界，如果是，把下一个事件交给父容器
                    if (mImageEdge == EDGE_BOTH || mImageEdge == EDGE_RIGHT && dX <= -1f
                            || mImageEdge == EDGE_LEFT && dX >= 1f) {
                        parent?.requestDisallowInterceptTouchEvent(false)
                    } else {
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    handle = true
                }
                mLastX = touchX
                mLastY = touchY
            }
            else -> {
            }
        }
        return handle
    }

    /**
     * 判断触摸点是否在图片的显示范围
     */
    private fun isPointInImageDisplayRect(e: MotionEvent): Boolean{
        val displayRect = imageDisplayRect
        return displayRect.contains(e.x, e.y)
    }

    /**
     * 检查边界并设置图片的Matrix
     */
    private fun checkAndUpdateImageMatrix() {
        checkImageBounds()
        updateImageMatrix()
    }

    /**
     * 在缩放和拖动时，进行图片显示范围的控制
     */
    private fun checkImageBounds() {
        val rect = imageDisplayRect
        var deltaX = 0f
        var deltaY = 0f
        val width = viewWidth
        val height = viewHeight
        //如果图片缩放的宽或高小于屏幕，则让其居中
        if (rect.width() < width) {
            mImageEdge = EDGE_BOTH
            deltaX = width * 0.5f - rect.right + 0.5f * rect.width()
        }
        if (rect.height() < height) {
            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height()
        }
        //如果图片缩放的宽或高大于屏幕，则控制范围，哪里有空隙就往哪边移动
        if (rect.width() >= width) {
            mImageEdge = EDGE_NONE
            if (rect.left > 0) { //如果左边有空隙，左移
                mImageEdge = EDGE_LEFT
                deltaX = -rect.left
            }
            if (rect.right < width) { //如果右边有空隙，右移
                mImageEdge = EDGE_RIGHT
                deltaX = width - rect.right
            }
        }
        if (rect.height() >= height) {
            if (rect.top > 0) { //如果顶部有空隙，上移
                deltaY = -rect.top
            }
            if (rect.bottom < height) { //如果底部有空隙，下移
                deltaY = height - rect.bottom
            }
        }
        mShapeMatrix.postTranslate(deltaX, deltaY)
    }

    /**
     * 初始化图片矩阵
     */
    private fun initImageMatrix(drawable: Drawable?) {
        if (drawable == null) {
            return
        }
        mInitMatrix.reset()
        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight
        val widthScale = viewWidth / imageWidth.toFloat()
        val heightScale = viewWidth / imageHeight.toFloat()
        when(mScaleType){
            ScaleType.CENTER_INSIDE -> {
                val scale = 1.0f.coerceAtMost(widthScale.coerceAtMost(heightScale))
                //图片移动到中心
                mInitMatrix.postTranslate(
                        (viewWidth - imageWidth) / 2f,
                        (viewHeight - imageHeight) / 2f)
                //等比例缩放
                mInitMatrix.postScale(
                        scale, scale,
                        viewWidth / 2f, viewHeight/ 2f)
            }
            else -> {
                val mSrcRect = RectF(0f, 0f, imageWidth.toFloat(), imageHeight.toFloat())
                val mDstRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
                mInitMatrix.setRectToRect(mSrcRect, mDstRect, Matrix.ScaleToFit.CENTER)
            }
        }

        resetImageMatrix()
    }

    /**
     * 重置图片矩阵
     */
    private fun resetImageMatrix(){
        mShapeMatrix.reset()
        updateImageMatrix()
    }

    /**
     * 更新图片矩阵
     */
    private fun updateImageMatrix(){
        imageMatrix = getTempImageMatrix()
    }

    /**
     * 获取还未设置前的图片矩阵
     */
    private fun getTempImageMatrix(): Matrix{
        mTempImageMatrix.set(mInitMatrix)
        mTempImageMatrix.postConcat(mShapeMatrix)
        return mTempImageMatrix
    }

    /**
     * 根据当前图片的Matrix获得图片的显示范围
     */
    private val imageDisplayRect: RectF
        get() {
            val d = drawable
            if (null != d) {
                mTempRect.set(0f, 0f,
                        d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat()
                )
                getTempImageMatrix().mapRect(mTempRect)
            }
            return mTempRect
        }

    /**
     * 获得当前图片的缩放比例
     */
    private val curImageScale: Float
        get() {
            mShapeMatrix.getValues(mTempMatrixValues)
            return mTempMatrixValues[Matrix.MSCALE_X]
        }

    /**
     * 视图的宽
     */
    private val viewWidth: Int
        get() = width - paddingLeft - paddingRight

    /**
     * 视图的高
     */
    private val viewHeight: Int
        get() = height - paddingBottom - paddingTop

    /**
     * fling时处理图片移动
     */
    internal inner class AutoFlingRunnable(velocityX: Float, velocityY: Float) : Runnable {

        private var mLastX = 0
        private var mLastY = 0

        init {
            val displayRect = imageDisplayRect
            val imageWidth = displayRect.width().roundToInt()
            val imageHeight = displayRect.height().roundToInt()
            val viewWidth = width
            val viewHeight = height

            val vX = velocityX.roundToInt()
            val vY = velocityY.roundToInt()

            val startX = -displayRect.left.roundToInt()
            val startY = -displayRect.top.roundToInt()
            var minX = startX
            var maxX = startX
            var minY = startY
            var maxY = startY

            if(imageWidth > viewWidth){
                minX = 0
                maxX = imageWidth - viewWidth
            }
            if(imageHeight > viewHeight){
                minY = 0
                maxY = imageHeight - viewHeight
            }

            //当图片高或宽大于视图的显示范围时才能fling
            if (startX != maxX || startY != maxY) {
                mFlingScroller.fling(startX, startY, vX, vY, minX, maxX, minY, maxY)
            }

            mLastX = startX
            mLastY = startY
        }

        override fun run() {
            if (mFlingScroller.isFinished) {
                return
            }
            if (mFlingScroller.computeScrollOffset()) {
                val curX = mFlingScroller.currX
                val curY = mFlingScroller.currY
                val dX = curX - mLastX
                val dY = curY - mLastY
                mShapeMatrix.postTranslate(dX.toFloat(), dY.toFloat())
                checkAndUpdateImageMatrix()
                mLastX = curX
                mLastY = curY
                postDelayed(this, AUTO_FLING_INTERVAL)
            }
        }
    }

    /**
     * 双击时处理图片缩放
     */
    internal inner class AutoScaleRunnable(private val mTargetScale: Float, private val x: Float, private val y: Float) : Runnable {

        private var mTempScale = 0f

        init {
            //传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
            mTempScale = if (curImageScale < mTargetScale) {
                Companion.SCALE_BIGGER
            } else {
                Companion.SCALE_SMALLER
            }
        }

        override fun run() {
            // 进行缩放
            mShapeMatrix.postScale(mTempScale, mTempScale, x, y)
            checkAndUpdateImageMatrix()
            val currentScale = curImageScale
            if (mTempScale > 1f && currentScale < mTargetScale ||
                    mTempScale < 1f && mTargetScale < currentScale) { //如果值在合法范围内，继续缩放
                postDelayed(this, AUTO_SCALE_INTERVAL)
            } else { //设置为目标的缩放比例
                val deltaScale = mTargetScale / currentScale
                mShapeMatrix.postScale(deltaScale, deltaScale, x, y)
                checkAndUpdateImageMatrix()
                isScaling = false
            }
        }
    }
}