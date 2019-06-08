package com.example.p2p.base;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.p2p.R;
import com.example.p2p.callback.IDialogCallback;

import java.lang.reflect.Field;

/**
 * dialog基类
 * Created by 陈健宇 at 2019/6/7
 */
public abstract class BaseDialogFragment extends DialogFragment {

    private IDialogCallback mDialogCallback;
    protected abstract int getMessage();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage(getString(getMessage()))
                .setTitle(getString(R.string.dialog_toast))
                .setPositiveButton(getString(R.string.dialog_positive), (dialog, which) -> {
                    if(mDialogCallback != null){
                        mDialogCallback.onAgree();
                    }
                    this.dismiss();
                })
                .setNegativeButton(getString(R.string.dialog_negative), (dialog, which) -> {
                    if(mDialogCallback != null){
                        mDialogCallback.onDismiss();
                    }
                    this.dismiss();
                })
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnKeyListener((dialog, keyCode, event) -> event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0);
        return alertDialog;
    }

    public void setDialogCallback(IDialogCallback callback){
        this.mDialogCallback = callback;
    }

    public void show(FragmentManager manager){
        show(manager, this.getClass().getName());
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if(this.isAdded())
            this.dismiss();
        super.show(manager, tag);
    }
}
