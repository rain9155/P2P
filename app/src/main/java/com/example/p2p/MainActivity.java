package com.example.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baseadapter.BaseAdapter;
import com.example.loading.Loading;
import com.example.loading.StatusView;
import com.example.p2p.adapter.RvMainAdapter;
import com.example.p2p.base.BaseActivity;
import com.example.p2p.base.BaseDialogFragment;
import com.example.p2p.callback.IDialogCallback;
import com.example.p2p.callback.IScanCallback;
import com.example.p2p.manager.ScanManager;
import com.example.p2p.utils.WifiUtils;
import com.example.p2p.widget.dialog.GotoWifiSettingsDialog;
import com.example.utils.CommonUtil;
import com.example.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.rv_main)
    RecyclerView rvMain;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.iv_add)
    ImageView ivAdd;

    private String TAG = MainActivity.class.getSimpleName();
    private final int REQUEST_WIFI_CODE = 0X000;
    private RvMainAdapter mRvMainAdapter;
    private StatusView mStatusView;
    private BaseDialogFragment mGotoWifiSettingsDialog;
    private List<String> mPingSuccessList;
    private NetWorkConnectionReceiver mNetWorkConnectionReceiver;

    @Override
    protected void onStart() {
        mNetWorkConnectionReceiver = new NetWorkConnectionReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetWorkConnectionReceiver, intentFilter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mNetWorkConnectionReceiver);
        super.onStop();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    @Override
    protected void initView() {
        setSupportActionBar(toolBar);
        ivBack.setVisibility(View.GONE);
        tvTitle.setText(getString(R.string.main_tlTitle));
        mStatusView = Loading.beginBuildStatusView(this)
                .warp(findViewById(R.id.rv_main))
                .addLoadingView(R.layout.main_scanning)
                .addEmptyView(R.layout.main_scan_empty)
                .create();
        mPingSuccessList = new ArrayList<>();
        rvMain.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mRvMainAdapter = new RvMainAdapter(mPingSuccessList, R.layout.item_main);
        rvMain.setAdapter(mRvMainAdapter);
        mGotoWifiSettingsDialog = new GotoWifiSettingsDialog();
    }

    @Override
    protected void initCallback() {
        mStatusView.showLoading();
        mGotoWifiSettingsDialog.setDialogCallback(new IDialogCallback() {
            @Override
            public void onAgree() {
                WifiUtils.gotoWifiSettings(MainActivity.this, REQUEST_WIFI_CODE);
            }

            @Override
            public void onDismiss() {
                ToastUtil.showToast(MainActivity.this, getString(R.string.toast_wifi_settings));
                mStatusView.showEmpty();
            }
        });

        ScanManager.getInstance().setScanCallback(new IScanCallback() {
            @Override
            public void onScanSuccess(List<String> pingSuccessList) {
                if (!CommonUtil.isEmptyList(mPingSuccessList)) mPingSuccessList.clear();
                mPingSuccessList.addAll(pingSuccessList);
                mRvMainAdapter.notifyDataSetChanged();
                mStatusView.showSuccess();
            }

            @Override
            public void onScanEmpty() {
                mStatusView.showEmpty();
            }

            @Override
            public void onScanError() {
                mGotoWifiSettingsDialog.show(getSupportFragmentManager(), GotoWifiSettingsDialog.class.getName());
            }
        });
        ScanManager.getInstance().startScan(this);
        mRvMainAdapter.setOnItemChildClickListener((adapter, view, position) -> {

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_WIFI_CODE) {
            if (WifiUtils.isWifiConnected(this)) {
                ScanManager.getInstance().startScan(MainActivity.this);
            } else {
                ToastUtil.showToast(MainActivity.this, getString(R.string.toast_wifi_settings));
                mStatusView.showEmpty();
            }
        }
    }

    @OnClick({R.id.iv_add})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_add:
                if(!ScanManager.getInstance().isScanning()){
                    mStatusView.showLoading();
                    ScanManager.getInstance().startScan(this);
                }
                break;
            default:
                break;
        }
    }


    /**
     * 监听Wifi网络变化广播
     */
    public class NetWorkConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (!WifiUtils.isWifiConnected(context) && ScanManager.getInstance().isScanning()) {
                    mGotoWifiSettingsDialog.show(getSupportFragmentManager(), GotoWifiSettingsDialog.class.getName());
                }
            }
        }
    }

}
