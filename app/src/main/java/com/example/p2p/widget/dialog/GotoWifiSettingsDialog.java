package com.example.p2p.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.p2p.R;
import com.example.p2p.base.BaseDialogFragment;
import com.example.p2p.callback.IDialogCallback;
import com.example.p2p.callback.IScanCallback;

import java.lang.reflect.Field;

import static android.graphics.Color.*;

/**
 * 询问是否去打开wifi的dialog
 * Created by 陈健宇 at 2019/6/7
 */
public class GotoWifiSettingsDialog extends BaseDialogFragment {

    @Override
    protected int getMessage() {
        return R.string.dialog_wifi_settings;
    }
}
