package com.example.p2p.widget.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.p2p.R;
import com.example.p2p.base.fragment.BaseDialogFragment;
import com.example.p2p.callback.IDialogCallback;

/**
 * 询问是否去打开wifi的dialog
 * Created by 陈健宇 at 2019/6/7
 */
public class GotoWifiSettingsDialog extends BaseDialogFragment {

    private IDialogCallback mDialogCallback;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.dialog_wifi_settings))
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
}
