package com.example.p2p.widget.helper;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintHelper;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * PhotoActivity界面控制箭头旋转的ConstraintHelper
 * Created by 陈健宇 at 2019/11/10
 */
public class ChangeArrowHelper extends ConstraintHelper {

    private View mArrowView;
    private ObjectAnimator mRotateAnim;
    private boolean isDown = true;

    public ChangeArrowHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void updatePostLayout(ConstraintLayout container) {
        super.updatePostLayout(container);
        mArrowView = getViews(container)[0];
        mRotateAnim = ObjectAnimator.ofFloat(mArrowView, "rotation", 0, 180);
        mRotateAnim.setDuration(300);
        mRotateAnim.setInterpolator(new LinearInterpolator());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mRotateAnim != null){
            mRotateAnim.cancel();
        }
    }

    public void arrowNav(){
        if(mArrowView != null && !mRotateAnim.isRunning()){
            if(isDown){
                mRotateAnim.setFloatValues(0f, 180f);
            }else {
                mRotateAnim.setFloatValues(180f, 360f);
            }
            mRotateAnim.start();
            isDown = !isDown;
        }
    }
}
