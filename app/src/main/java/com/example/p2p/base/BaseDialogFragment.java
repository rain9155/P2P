package com.example.p2p.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.lang.reflect.Field;

/**
 * dialog基类
 * Created by 陈健宇 at 2019/6/7
 */
public abstract class BaseDialogFragment extends DialogFragment {


    protected abstract int getDialogViewId();
    protected abstract void initView(View view);
    protected abstract void loadData();
    private Dialog mDialog;
    private View mView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(getDialogViewId(), null);
        mDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setCancelable(false)
                .create();
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.getWindow().setGravity(Gravity.CENTER);
        mView = view;
        initView(view);
        return mDialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        loadData();
    }

    /**
     * 根据id获取View
     */
    protected <T> T getView(int id){
        return (T) mView.findViewById(id);
    }

    /**
     * 禁止按返回键取消dialog
     * 设置点击屏幕Dialog不消失
     */
    protected void cancelBackDismiss() {
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnKeyListener((dialog1, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0);
    }


    @Override
    public void show(FragmentManager manager, String tag) {
        if(this.isAdded())
            this.dismiss();
        super.show(manager, tag);
    }
}
