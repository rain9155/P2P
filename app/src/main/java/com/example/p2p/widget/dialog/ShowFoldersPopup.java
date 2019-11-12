package com.example.p2p.widget.dialog;

import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.example.p2p.R;
import com.example.p2p.widget.helper.ChangeArrowHelper;
import com.example.utils.DisplayUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.animator.PopupAnimator;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupAnimation;
import com.lxj.xpopup.impl.PartShadowPopupView;

/**
 * Created by 陈健宇 at 2019/11/10
 */
public class ShowFoldersPopup extends PartShadowPopupView {

    private ChangeArrowHelper mArrowHelper;

    public ShowFoldersPopup(@NonNull Context context, ChangeArrowHelper arrowHelper) {
        super(context);
        mArrowHelper = arrowHelper;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_show_folders;
    }


    @Override
    public BasePopupView show() {
        mArrowHelper.arrowNav();
        return super.show();
    }


    @Override
    public void dismiss() {
        mArrowHelper.arrowNav();
        super.dismiss();
    }

    @Override
    protected void onShow() {
        super.onShow();
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();
    }

    public static class ScrollFromTopAnim extends PopupAnimator {

        private IntEvaluator intEvaluator = new IntEvaluator();
        private int startScrollX, startScrollY;

        @Override
        public void initAnimator() {
            targetView.post(() -> {
                // 设置参考点
                startScrollY =  targetView.getMeasuredHeight();
                startScrollX = 0;
                targetView.scrollTo(startScrollX, startScrollY);

            });
        }

        @Override
        public void animateShow() {
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.addUpdateListener(animation -> {
                float fraction = animation.getAnimatedFraction();
                targetView.scrollTo(
                        intEvaluator.evaluate(fraction, startScrollX, 0),
                        intEvaluator.evaluate(fraction, startScrollY, 0));
            });
            animator.setDuration(XPopup.getAnimationDuration()).setInterpolator(new FastOutSlowInInterpolator());
            animator.start();
        }

        @Override
        public void animateDismiss() {
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.addUpdateListener(animation -> {
                float fraction = animation.getAnimatedFraction();
                targetView.scrollTo(
                        intEvaluator.evaluate(fraction, 0, startScrollX),
                        intEvaluator.evaluate(fraction, 0, startScrollY));
            });
            animator.setDuration(XPopup.getAnimationDuration())
                    .setInterpolator(new FastOutSlowInInterpolator());
            animator.start();
        }

    }

}
