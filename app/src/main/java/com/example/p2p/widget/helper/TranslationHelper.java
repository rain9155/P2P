package com.example.p2p.widget.helper;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintHelper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.example.p2p.R;

/**
 * ChatActivity中用于帮助表情布局、更多布局和编辑布局进行平移动画
 * Created by 陈健宇 at 2019/11/14
 */
public class TranslationHelper extends ConstraintHelper {

    private ConstraintSet mResetConstraintSet, mMoreConstraintSet, mEmojiConstraintSet;
    private ConstraintLayout mParent;
    private boolean isMoreLayoutShown, isEmojiLayoutShown;

    public TranslationHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
        mResetConstraintSet = new ConstraintSet();
        mMoreConstraintSet = new ConstraintSet();
        mEmojiConstraintSet = new ConstraintSet();
        createMoreLayoutConstraint();
        createEmojiLayoutConstraint();
        createResetConstraint();
    }

    @Override
    public void updatePostLayout(ConstraintLayout container) {
        super.updatePostLayout(container);
        mParent = container;
    }

    private void createMoreLayoutConstraint() {
        mMoreConstraintSet.clear(R.id.cl_edit);
        mMoreConstraintSet.clear(R.id.cl_more);
        mMoreConstraintSet.clear(R.id.ll_emoji);

        mMoreConstraintSet.connect(R.id.cl_edit, ConstraintSet.TOP, R.id.rv_chat, ConstraintSet.BOTTOM);
        mMoreConstraintSet.connect(R.id.cl_edit, ConstraintSet.BOTTOM, R.id.cl_more, ConstraintSet.TOP);
        mMoreConstraintSet.centerHorizontally(R.id.cl_edit, ConstraintSet.PARENT_ID);
        mMoreConstraintSet.constrainHeight(R.id.cl_edit, ConstraintSet.WRAP_CONTENT);
        mMoreConstraintSet.constrainWidth(R.id.cl_edit, ConstraintSet.MATCH_CONSTRAINT);

        mMoreConstraintSet.connect(R.id.cl_more, ConstraintSet.TOP, R.id.cl_edit, ConstraintSet.BOTTOM);
        mMoreConstraintSet.connect(R.id.cl_more, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        mMoreConstraintSet.centerHorizontally(R.id.cl_more, ConstraintSet.PARENT_ID);
        mMoreConstraintSet.constrainHeight(R.id.cl_more, ConstraintSet.WRAP_CONTENT);
        mMoreConstraintSet.constrainWidth(R.id.cl_more, ConstraintSet.MATCH_CONSTRAINT);

        mMoreConstraintSet.connect(R.id.ll_emoji, ConstraintSet.TOP, R.id.cl_more, ConstraintSet.BOTTOM);
        mMoreConstraintSet.centerHorizontally(R.id.ll_emoji, ConstraintSet.PARENT_ID);
        mMoreConstraintSet.constrainWidth(R.id.ll_emoji, ConstraintSet.MATCH_CONSTRAINT);
        mMoreConstraintSet.constrainHeight(R.id.ll_emoji, ConstraintSet.WRAP_CONTENT);
    }

    private void createEmojiLayoutConstraint() {
        mEmojiConstraintSet.clear(R.id.cl_edit);
        mEmojiConstraintSet.clear(R.id.cl_more);
        mEmojiConstraintSet.clear(R.id.ll_emoji);

        mEmojiConstraintSet.connect(R.id.cl_edit, ConstraintSet.TOP, R.id.rv_chat, ConstraintSet.BOTTOM);
        mEmojiConstraintSet.connect(R.id.cl_edit, ConstraintSet.BOTTOM, R.id.ll_emoji, ConstraintSet.TOP);
        mEmojiConstraintSet.centerHorizontally(R.id.cl_edit, ConstraintSet.PARENT_ID);
        mEmojiConstraintSet.constrainHeight(R.id.cl_edit, ConstraintSet.WRAP_CONTENT);
        mEmojiConstraintSet.constrainWidth(R.id.cl_edit, ConstraintSet.MATCH_CONSTRAINT);


        mEmojiConstraintSet.connect(R.id.ll_emoji, ConstraintSet.TOP, R.id.cl_edit, ConstraintSet.BOTTOM);
        mEmojiConstraintSet.connect(R.id.ll_emoji, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        mEmojiConstraintSet.centerHorizontally(R.id.ll_emoji, ConstraintSet.PARENT_ID);
        mEmojiConstraintSet.constrainHeight(R.id.ll_emoji, ConstraintSet.WRAP_CONTENT);
        mEmojiConstraintSet.constrainWidth(R.id.ll_emoji, ConstraintSet.MATCH_CONSTRAINT);

        mEmojiConstraintSet.connect(R.id.cl_more, ConstraintSet.TOP, R.id.ll_emoji, ConstraintSet.BOTTOM);
        mEmojiConstraintSet.centerHorizontally(R.id.cl_more, ConstraintSet.PARENT_ID);
        mEmojiConstraintSet.constrainWidth(R.id.cl_more, ConstraintSet.MATCH_CONSTRAINT);
        mEmojiConstraintSet.constrainHeight(R.id.cl_more, ConstraintSet.WRAP_CONTENT);
    }

    private void createResetConstraint() {
        mResetConstraintSet.clear(R.id.cl_edit);
        mResetConstraintSet.clear(R.id.cl_more);
        mResetConstraintSet.clear(R.id.ll_emoji);

        mResetConstraintSet.connect(R.id.cl_edit, ConstraintSet.TOP, R.id.rv_chat, ConstraintSet.BOTTOM);
        mResetConstraintSet.connect(R.id.cl_edit, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        mResetConstraintSet.centerHorizontally(R.id.cl_edit, ConstraintSet.PARENT_ID);
        mResetConstraintSet.constrainHeight(R.id.cl_edit, ConstraintSet.WRAP_CONTENT);
        mResetConstraintSet.constrainWidth(R.id.cl_edit, ConstraintSet.MATCH_CONSTRAINT);

        mResetConstraintSet.connect(R.id.cl_more, ConstraintSet.TOP, R.id.cl_edit, ConstraintSet.BOTTOM);
        mResetConstraintSet.centerHorizontally(R.id.cl_more, ConstraintSet.PARENT_ID);
        mResetConstraintSet.constrainHeight(R.id.cl_more, ConstraintSet.WRAP_CONTENT);
        mResetConstraintSet.constrainWidth(R.id.cl_more, ConstraintSet.MATCH_CONSTRAINT);

        mResetConstraintSet.connect(R.id.ll_emoji, ConstraintSet.TOP, R.id.cl_edit, ConstraintSet.BOTTOM);
        mResetConstraintSet.centerHorizontally(R.id.ll_emoji, ConstraintSet.PARENT_ID);
        mResetConstraintSet.constrainHeight(R.id.ll_emoji, ConstraintSet.WRAP_CONTENT);
        mResetConstraintSet.constrainWidth(R.id.ll_emoji, ConstraintSet.MATCH_CONSTRAINT);
    }


    public void showMoreLayout(){
        TransitionManager.beginDelayedTransition(mParent);
        mMoreConstraintSet.applyTo(mParent);
        isMoreLayoutShown = true;
        isEmojiLayoutShown = false;
    }

    public void showEmojiLayout(){
        TransitionManager.beginDelayedTransition(mParent);
        mEmojiConstraintSet.applyTo(mParent);
        isEmojiLayoutShown = true;
        isMoreLayoutShown = false;
    }


    public void hideBottomLayout(){
        TransitionManager.beginDelayedTransition(mParent);
        mResetConstraintSet.applyTo(mParent);
        isEmojiLayoutShown = false;
        isMoreLayoutShown = false;
    }

    /**
     * 底部表情布局或底部更多布局是否显示
     */
    public boolean isButtomLayoutShown() {
        return isEmojiLayoutShown || isMoreLayoutShown;
    }

    /**
     * 底部更多布局是否显示
     */
    public boolean isMoreLayoutShown() {
        return isMoreLayoutShown;
    }

    /**
     * 底部表情布局是否显示
     */
    public boolean isEmojiLayoutShown() {
        return isEmojiLayoutShown;
    }

}
