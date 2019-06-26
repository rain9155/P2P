package com.example.p2p.base.fragment;

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
