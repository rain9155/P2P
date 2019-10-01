package com.example.p2p.widget.helper;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintHelper;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.p2p.R;
import com.example.p2p.utils.CommonUtil;
import com.example.utils.DisplayUtil;

/**
 * PreActivity界面用来控制显示和隐藏toolBar和bottom的ConstraintHelper
 * Created by 陈健宇 at 2019/9/30
 */
public class PreActivityHelper extends ConstraintHelper {

    private final static int ANIM_TIME = 300;
    private final static int DELAY = 100;
    private final static String ANIM_PROPERTY_TRANSLATION = "translationY";
    private final static String ANIM_PROPERTY_ALPHA = "alpha";
    private ObjectAnimator mTopAnim;
    private int mTopViewHeight, mStatusBarHeight;
    private View[] mViews;
    private Activity mActivity;
    private boolean isShow = true;

    public PreActivityHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTopAnim = ObjectAnimator.ofFloat(null, ANIM_PROPERTY_TRANSLATION, 0);
        mStatusBarHeight = DisplayUtil.getStatusBarHeight(context);
    }


    @Override
    public void updatePostLayout(ConstraintLayout container) {
        super.updatePostLayout(container);
        mTopViewHeight = container.findViewById(R.id.tool_bar).getHeight();
        mViews = getViews(container);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTopAnim.cancel();
    }

    public void hideTopBottom(Activity activity){
        if(mTopAnim.isRunning()) return;
        mActivity = activity;
        for(View view : mViews){
            if(view.getId() == R.id.tool_bar){
                startTopAnim(view, 0, -mTopViewHeight);
            }else {
                startBottomAnim(view, 1f, 0);
            }
        }
        //等toolBar完全隐藏后再隐藏statusBar
        postDelayed(
                () -> setStatusBarVisibility(false),
                ANIM_TIME
        );
        isShow = false;
    }

    public void showTopBottom(Activity activity){
        if(mTopAnim.isRunning()) return;
        mActivity = activity;
        setStatusBarVisibility(true);
        for(View view : mViews){
            if(view.getId() == R.id.tool_bar){
                //等statusBar完全显示后再显示toolBar
                postDelayed(
                        () -> startTopAnim(view, -mTopViewHeight, mStatusBarHeight),
                        DELAY
                );
            }else {
                startBottomAnim(view, 0, 1f);
            }
        }
        isShow = true;
    }

    public boolean isShow(){
        return isShow;
    }


    private void startTopAnim(View target, float from, float to){
        mTopAnim.setTarget(target);
        mTopAnim.setFloatValues(from, to);
        mTopAnim.setStartDelay(DELAY);
        mTopAnim.start();
    }

    private void startBottomAnim(View target, float from, float to){
        ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(target, ANIM_PROPERTY_ALPHA, from, to);
        bottomAnim.setDuration(ANIM_TIME);
        bottomAnim.setStartDelay(DELAY);
        bottomAnim.start();
    }

    private void setStatusBarVisibility(boolean isShow) {
        if (isShow) {
            mActivity.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            CommonUtil.darkMode(mActivity, true);
        } else {
            mActivity.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

}
