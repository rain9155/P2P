package com.example.p2p.widget.helper;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.constraintlayout.widget.ConstraintHelper;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.p2p.R;

import butterknife.ButterKnife;

/**
 * Created by 陈健宇 at 2019/9/30
 */
public class CustomHelper extends ConstraintHelper {


    private ObjectAnimator mTopAnim, mRvPreViewAnim, mDividerAnim, mClBottomAnim;
    private AnimatorSet mBottomAnim;
    private int mTopViewHeight;
    private View[] mViews;

    public CustomHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void updatePostLayout(ConstraintLayout container) {
        super.updatePostLayout(container);
        mTopViewHeight = container.findViewById(R.id.tool_bar).getHeight();
        mViews = getViews(container);
    }

    public void hide(){
        initAnim();
        mTopAnim.setStartDelay(500);
        mTopAnim.start();
        mBottomAnim.playTogether(mRvPreViewAnim, mDividerAnim, mClBottomAnim);
        mBottomAnim.start();
    }

    private void initAnim() {

    }

}
