package com.example.p2p.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewPropertyAnimator;

import androidx.appcompat.widget.AppCompatButton;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.example.p2p.R;
import com.example.p2p.utils.SimpleAnimatorListener;

/**
 * 发送消息按钮
 * Created by 陈健宇 at 2019/5/28
 */
public class SendButton extends AppCompatButton {

    private  ObjectAnimator mAnimator;

    public SendButton(Context context) {
        super(context, null);
    }


    public SendButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.buttonStyle);
    }

    public SendButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
       // mAnimator.removeAllListeners();
        mAnimator = null;
        super.onDetachedFromWindow();
    }

    private void init() {
        mAnimator = ObjectAnimator.ofFloat(this, "scale");
        mAnimator.setDuration(200);
        mAnimator.setInterpolator(new LinearOutSlowInInterpolator());
    }

    @Override
    public void setVisibility(int visibility) {
        if(visibility == getVisibility()) return;
        if(mAnimator == null) mAnimator = ObjectAnimator.ofFloat(this, "scale", 0, 1.0f);
        if(visibility == View.VISIBLE){
            super.setVisibility(visibility);
            mAnimator.setFloatValues(0, 1.0f);
        }else {
            mAnimator.setFloatValues(1.0f, 0);
        }
        mAnimator.start();
    }

   private void setScale(float scale){
        this.setScaleX(scale);
        this.setScaleY(scale);
   }
}
