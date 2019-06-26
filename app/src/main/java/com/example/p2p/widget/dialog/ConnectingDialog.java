package com.example.p2p.widget.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.p2p.R;
import com.example.p2p.base.fragment.BaseDialogFragment;

/**
 * 等待连接dialog
 * Created by 陈健宇 at 2019/6/9
 */
public class ConnectingDialog extends BaseDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loading_view, container, false);
        TextView textView = view.findViewById(R.id.tv_loading);
        textView.setText(R.string.dialog_connecting);
        view.setPadding(0, 50, 0, 50);
        return view;
    }
}
