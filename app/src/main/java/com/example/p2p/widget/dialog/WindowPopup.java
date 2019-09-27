package com.example.p2p.widget.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.p2p.R;
import com.example.p2p.callback.IRefreshCallback;

/**
 * Created by 陈健宇 at 2019/6/29
 */
public class WindowPopup extends PopupWindow {

    private TextView mTvRefresh;
    private IRefreshCallback mCallback;

    public WindowPopup(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.popup_widow, null);
        mTvRefresh = view.findViewById(R.id.tv_refresh);
        this.setContentView(view);
        this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setTouchable(true);
        this.setFocusable(true);
        mTvRefresh.setOnClickListener(v -> {
            this.dismiss();
            if(mCallback != null) mCallback.onRefresh();
        });
    }

    public void setRefreshCallback(IRefreshCallback callback){
        this.mCallback = callback;
    }

}
