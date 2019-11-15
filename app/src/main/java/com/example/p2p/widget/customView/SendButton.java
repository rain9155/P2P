package com.example.p2p.widget.customView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatButton;
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeTransform;
import androidx.transition.Scene;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.example.p2p.R;
import com.example.utils.listener.AnimatorListener;

/**
 * 发送消息按钮
 * Created by 陈健宇 at 2019/5/28
 */
public class SendButton extends AppCompatButton {

    private final static int ANIM_TIME = 150;
    private TransitionSet mTransitionSet;

    public SendButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTransitionSet = new TransitionSet();
        mTransitionSet.setDuration(ANIM_TIME);
        mTransitionSet.addTransition(new ChangeTransform())
                .addTransition(new ChangeBounds());
    }


    @Override
    public void setVisibility(int visibility) {
        TransitionManager.beginDelayedTransition((ViewGroup) this.getParent(), mTransitionSet);
        if(visibility == getVisibility()){
            return;
        }
        if(visibility == View.VISIBLE){
            super.setVisibility(View.VISIBLE);
            this.animate().alpha(1).scaleX(1)
                    .setDuration(ANIM_TIME)
                    .start();
        }else if(visibility == View.GONE){
            this.animate().alpha(0).scaleX(0)
                    .setDuration(ANIM_TIME)
                    .start();
            postDelayed(() -> setVisibility(INVISIBLE), ANIM_TIME);
        }else {
            super.setVisibility(View.GONE);
        }
    }

}
