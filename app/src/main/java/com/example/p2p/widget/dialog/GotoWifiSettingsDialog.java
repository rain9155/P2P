package com.example.p2p.widget.dialog;

import android.view.View;
import android.widget.TextView;

import com.example.p2p.R;
import com.example.p2p.base.BaseDialogFragment;
import com.example.p2p.callback.IScanCallback;

/**
 * 扫描局域网Ip地址的等待dialog
 * Created by 陈健宇 at 2019/6/7
 */
public class GotoWifiSettingsDialog extends BaseDialogFragment {

    private IScanCallback mScanCallback;

    @Override
    protected int getDialogViewId() {
        return R.layout.main_scanning;
    }

    @Override
    protected void initView(View view) {
        cancelBackDismiss();
        TextView textView = getView(R.id.tv_loading);
        textView.setText(getString(R.string.main_scaning_tvScanning));
    }

    @Override
    protected void loadData() {
    }

    public void setScanCallback(IScanCallback scanCallback){
        this.mScanCallback = scanCallback;
    }

}
