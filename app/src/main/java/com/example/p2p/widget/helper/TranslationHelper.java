package com.example.p2p.widget.helper;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintHelper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.transition.TransitionManager;

import com.example.p2p.R;

/**
 * Created by 陈健宇 at 2019/11/14
 */
public class TranslationHelper extends ConstraintHelper {

    private View mBottomLayout, mMoreLayout, mEmojiLayout, mEditLayout;
    private ObjectAnimator mBottomAnim, mEditAnim;
    private AnimatorSet mAnimatorSet;
    private ConstraintSet mApplyConstraintSet, mResetConstraintSet;
    private ConstraintLayout mParent;
    private boolean isBottomLayoutShown;

    public TranslationHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
        mApplyConstraintSet = new ConstraintSet();
        mResetConstraintSet = new ConstraintSet();
    }

    @Override
    public void updatePostLayout(ConstraintLayout container) {
        super.updatePostLayout(container);
        mParent = container;
        mApplyConstraintSet.clone(container);
        mResetConstraintSet.clone(container);
        mMoreLayout = container.findViewById(R.id.cl_more);
        mEmojiLayout = container.findViewById(R.id.ll_emoji);

    }

    public void showMoreLayout(){
        mMoreLayout.setVisibility(VISIBLE);
        mEmojiLayout.setVisibility(GONE);
        TransitionManager.beginDelayedTransition(mParent);
        mApplyConstraintSet.clear(R.id.cl_edit);
        mApplyConstraintSet.clear(R.id.fl_bottom);
        mApplyConstraintSet.connect(R.id.cl_edit, ConstraintSet.TOP, R.id.srl_chat, ConstraintSet.BOTTOM);
        mApplyConstraintSet.connect(R.id.cl_edit, ConstraintSet.BOTTOM, R.id.fl_bottom, ConstraintSet.TOP);
        mApplyConstraintSet.connect(R.id.fl_bottom, ConstraintSet.TOP, R.id.cl_edit, ConstraintSet.BOTTOM);
        mApplyConstraintSet.connect(R.id.fl_bottom, ConstraintSet.BOTTOM, R.id.cl_main, ConstraintSet.BOTTOM);
        mApplyConstraintSet.applyTo(mParent);
    }

    public void hideMoreLayout(){
        TransitionManager.beginDelayedTransition(mParent);
        mResetConstraintSet.applyTo(mParent);
        mMoreLayout.setVisibility(GONE);

    }

    public void showEmojiLayout(){
        mEmojiLayout.setVisibility(VISIBLE);
        mMoreLayout.setVisibility(GONE);
        TransitionManager.beginDelayedTransition(mParent);
        mApplyConstraintSet.clear(R.id.cl_edit);
        mApplyConstraintSet.clear(R.id.fl_bottom);
        mApplyConstraintSet.connect(R.id.cl_edit, ConstraintSet.TOP, R.id.srl_chat, ConstraintSet.BOTTOM);
        mApplyConstraintSet.connect(R.id.cl_edit, ConstraintSet.BOTTOM, R.id.fl_bottom, ConstraintSet.TOP);
        mApplyConstraintSet.connect(R.id.fl_bottom, ConstraintSet.TOP, R.id.cl_edit, ConstraintSet.BOTTOM);
        mApplyConstraintSet.connect(R.id.fl_bottom, ConstraintSet.BOTTOM, R.id.cl_main, ConstraintSet.BOTTOM);
        mApplyConstraintSet.applyTo(mParent);
    }

    public void hideEmojiLayout(){

        TransitionManager.beginDelayedTransition(mParent);
        mResetConstraintSet.applyTo(mParent);
        mEmojiLayout.setVisibility(GONE);

    }

    public void hideBottomLayout(){
        TransitionManager.beginDelayedTransition(mParent);
        mResetConstraintSet.applyTo(mParent);
    }

    /**
     * 底部表情布局或底部更多布局是否显示
     */
    public boolean isButtomLayoutShown() {
        return isBottomLayoutShown;
    }

    /**
     * 底部更多布局是否显示
     */
    public boolean isMoreLayoutShown() {
        return mMoreLayout.isShown();
    }

    /**
     * 底部表情布局是否显示
     */
    public boolean isEmojiLayoutShown() {
        return mEmojiLayout.isShown();
    }

}
