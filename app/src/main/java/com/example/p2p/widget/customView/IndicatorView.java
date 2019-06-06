package com.example.p2p.widget.customView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.p2p.R;

/**
 * ViewPager指示器
 * Created by 陈健宇 at 2019/5/29
 */
public class IndicatorView extends View {

    private int mIndicatorColor;
    private int mIndicatorColorSelected;
    private int mIndicatorRadius;
    private int mIndicatorRadiusSelected;
    private int mIndicatorCount;
    private int mIndicatorOffset;

    private int mWidth;
    private int mHeight;
    private Paint mPaint;
    private int mCurrentIndicator = 0;


    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wrapWidth =  (mIndicatorCount - 1) * mIndicatorRadiusSelected * 2 + mIndicatorRadius * 2 + mIndicatorOffset * (mIndicatorCount - 1) + getPaddingEnd() + getPaddingStart();
        int wrapHeight = Math.max(mIndicatorRadius, mIndicatorRadiusSelected) * 2 + getPaddingBottom() + getPaddingTop();
        setMeasuredDimension(
                MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(widthMeasureSpec) : wrapWidth,
                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY ?  MeasureSpec.getSize(heightMeasureSpec) : wrapHeight
        );
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.IndicatorView);
        mIndicatorColor = typedArray.getColor(R.styleable.IndicatorView_indicatorColor, Color.GRAY);
        mIndicatorColorSelected = typedArray.getColor(R.styleable.IndicatorView_indicatorColorSelected, Color.BLACK);
        mIndicatorRadius = typedArray.getDimensionPixelSize(R.styleable.IndicatorView_indicatorRadius, 12);
        mIndicatorRadiusSelected = typedArray.getDimensionPixelSize(R.styleable.IndicatorView_indicatorRadiusSelected, 8);
        mIndicatorOffset = typedArray.getDimensionPixelSize(R.styleable.IndicatorView_indicatorOffset, 10);
        mIndicatorCount = typedArray.getInt(R.styleable.IndicatorView_indicatorCount, 3);
        typedArray.recycle();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int y = mHeight / 2;
        int x = mIndicatorRadius + (mWidth - ((mIndicatorCount - 1) * mIndicatorRadiusSelected * 2 + mIndicatorRadius * 2 + mIndicatorOffset * (mIndicatorCount - 1))) / 2;//居中
        for(int i = 0; i < mIndicatorCount; i++){
            if(i == mCurrentIndicator){
                mPaint.setColor(mIndicatorColorSelected);
                if(i != 0){
                    canvas.drawCircle(x + mIndicatorRadius / 4, y, mIndicatorRadius, mPaint);
                }else {
                    canvas.drawCircle(x, y, mIndicatorRadius, mPaint);
                }
                x += mIndicatorRadius * 2 + mIndicatorOffset;
            }else {
                mPaint.setColor(mIndicatorColor);
                canvas.drawCircle(x, y, mIndicatorRadiusSelected, mPaint);
                x += mIndicatorRadiusSelected * 2 + mIndicatorOffset;
            }
        }
    }

    public void setIndicatorCount(int indicatorCount) {
        if(indicatorCount < 0) return;
        mIndicatorCount = indicatorCount;
        invalidate();
    }


    public void setCurrentIndicator(int currentIndicator) {
        if(currentIndicator < 0 || currentIndicator > mIndicatorCount) return;
        mCurrentIndicator = currentIndicator;
        invalidate();
    }
}
