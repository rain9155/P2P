package com.example.p2p.widget.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;

import androidx.appcompat.widget.Toolbar;

import com.example.p2p.R;
import com.example.p2p.widget.helper.ChangeArrowHelper;
import com.example.utils.DisplayUtil;

/**
 * Created by 陈健宇 at 2019/11/10
 */
public class ShowFoldersPopup extends PopupWindow {

    private ChangeArrowHelper mArrowHelper;

    public ShowFoldersPopup(Context context, ChangeArrowHelper helper) {
        super(context);
        this.mArrowHelper = helper;
        this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setTouchable(true);
        this.setFocusable(true);
        int height = (int) (DisplayUtil.getScreenHeight(context) / 1.2);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(height);
        this.setAnimationStyle(R.style.PopupWindowAnim);
    }


    @Override
    public void dismiss() {
        super.dismiss();
        mArrowHelper.arrowNav();
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        mArrowHelper.arrowNav();
    }

}
