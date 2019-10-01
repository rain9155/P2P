package com.example.p2p.widget.helper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintHelper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

/**
 * PhotoActivity界面控制时间条显示和隐藏的ConstraintHelper
 * Created by 陈健宇 at 2019/10/1
 */
public class PhotoActivityHelper extends ConstraintHelper {

    private final static String ANIM_PROPERTY_ALPHA = "alpha";
    private ObjectAnimator mHideTimeAnimator;
    private ObjectAnimator mShowTimeAnimator;
    private View mTimeView;

    public PhotoActivityHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void updatePostLayout(ConstraintLayout container) {
        super.updatePostLayout(container);
        mTimeView = getViews(container)[0];
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mHideTimeAnimator != null){
            mHideTimeAnimator.cancel();
            mHideTimeAnimator.removeAllListeners();
        }
        if(mShowTimeAnimator != null){
            mShowTimeAnimator.cancel();
        }
    }

    /**
     * 显示时间条
     */
    public void showPhotoTime(){
        if(mShowTimeAnimator == null){
            mShowTimeAnimator = ObjectAnimator.ofFloat(mTimeView, ANIM_PROPERTY_ALPHA, 0, 1f);
            mShowTimeAnimator.setDuration(500);
            mShowTimeAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        }else {
            mShowTimeAnimator.setFloatValues(0, 1f);
        }
        if(mShowTimeAnimator.isRunning()) return;
        if(mHideTimeAnimator != null && mHideTimeAnimator.isRunning()) mHideTimeAnimator.cancel();
        if(mTimeView.getVisibility() == View.INVISIBLE){
            mTimeView.setVisibility(View.VISIBLE);
            mShowTimeAnimator.start();
        }
    }

    /**
     * 隐藏时间条
     */
    public void hidePhotoTime(){
        if(mHideTimeAnimator == null){
            mHideTimeAnimator = ObjectAnimator.ofFloat(mTimeView, ANIM_PROPERTY_ALPHA, 1f, 0f);
            mHideTimeAnimator.setDuration(500);
            mHideTimeAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        }else {
            mHideTimeAnimator.setFloatValues(1f, 0);
        }
        if(mHideTimeAnimator.isRunning()) return;
        if(mShowTimeAnimator != null && mShowTimeAnimator.isRunning()) mShowTimeAnimator.cancel();
        if(mTimeView.getVisibility() == View.VISIBLE){
            mHideTimeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mTimeView.setVisibility(View.INVISIBLE);
                }
            });
            mHideTimeAnimator.start();
        }
    }
}
