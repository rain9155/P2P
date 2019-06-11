package com.example.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loading.Loading;
import com.example.loading.StatusView;
import com.example.p2p.adapter.RvMainAdapter;
import com.example.p2p.base.BaseActivity;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IBroadcastCallback;
import com.example.p2p.callback.IConnectCallback;
import com.example.p2p.callback.IDialogCallback;
import com.example.p2p.callback.IScanCallback;
import com.example.p2p.config.Constant;
import com.example.p2p.core.BroadcastManager;
import com.example.p2p.core.PingManager;
import com.example.p2p.core.ConnectManager;
import com.example.p2p.utils.LogUtils;
import com.example.p2p.utils.WifiUtils;
import com.example.p2p.widget.dialog.ConnectingDialog;
import com.example.p2p.widget.dialog.GotoWifiSettingsDialog;
import com.example.utils.CommonUtil;
import com.example.utils.FileUtil;
import com.example.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
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
    @BindView(R.id.iv_scan)
    ImageView ivAdd;

    private String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_SOCKET_STATE = 0x000;

    private RvMainAdapter mRvMainAdapter;
    private StatusView mStatusView;
    private GotoWifiSettingsDialog mGotoWifiSettingsDialog;
    private ConnectingDialog mConnectingDialog;
    private List<User> mOnlineUsers;
    private WifiConnectionReceiver mNetWorkConnectionReceiver;
    private int mPosition;

    @Override
    protected void onStart() {
        super.onStart();
        mNetWorkConnectionReceiver = new WifiConnectionReceiver();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
       // intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetWorkConnectionReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mNetWorkConnectionReceiver);
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
                .addLoadingView(R.layout.loading_view)
                .addEmptyView(R.layout.empty_view)
                .create();
        mOnlineUsers = new ArrayList<>();
        rvMain.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mRvMainAdapter = new RvMainAdapter(mOnlineUsers, R.layout.item_main);
        rvMain.setAdapter(mRvMainAdapter);
        mGotoWifiSettingsDialog = new GotoWifiSettingsDialog();
        mConnectingDialog = new ConnectingDialog();
        mStatusView.showLoading();
    }

    @Override
    protected void initCallback() {
        mRvMainAdapter.setOnItemClickListener((adapter, view, position) -> {
            if(mOnlineUsers.isEmpty()) return;
            mPosition = position;
            mConnectingDialog.show(getSupportFragmentManager());
            ConnectManager.getInstance().connect(mOnlineUsers.get(position).getIp());
        });
        mGotoWifiSettingsDialog.setDialogCallback(new IDialogCallback() {
            @Override
            public void onAgree() {
                WifiUtils.gotoWifiSettings(MainActivity.this);
            }

            @Override
            public void onDismiss() {
                ToastUtil.showToast(MainActivity.this, getString(R.string.toast_wifi_settings));
                if(mOnlineUsers.isEmpty()){
                    mStatusView.showEmpty();
                    return;
                }
                mStatusView.showSuccess();
            }
        });
        //扫描回调监听
//        PingManager.getInstance().setScanCallback(new IScanCallback() {
//            @Override
//            public void onScanSuccess(List<String> pingSuccessList) {
//                if (!CommonUtil.isEmptyList(mOnlineUsers)) mOnlineUsers.clear();
//                mOnlineUsers.addAll(mRvMainAdapter.wrap(pingSuccessList));
//                mRvMainAdapter.notifyDataSetChanged();
//                mStatusView.showSuccess();
//            }
//
//            @Override
//            public void onScanEmpty() {
//                mStatusView.showEmpty();
//            }
//
//            @Override
//            public void onScanError() {
//                mGotoWifiSettingsDialog.show(getSupportFragmentManager());
//            }
//        });
        //PingManager.getInstance().startScan();
        //连接回调监听
        ConnectManager.getInstance().setConnectCallback(new IConnectCallback() {
            @Override
            public void onConnectSuccess(String targetIp) {
                mConnectingDialog.dismiss();
                ToastUtil.showToast(MainActivity.this, getString(R.string.main_connecting_success));
            }

            @Override
            public void onConnectFail(String targetIp) {
                mConnectingDialog.dismiss();
                ToastUtil.showToast(MainActivity.this, getString(R.string.main_connecting_fail));
            }
        });
        BroadcastManager.getInstance().setBroadcastCallback(new IBroadcastCallback() {
            @Override
            public void onJoin(String ip){
            }

            @Override
            public void onExit(String ip) {

            }
        });
        
    }

    @Override
    protected void loadData() {
        new Handler().postDelayed(() -> {
            List<String> tempList =BroadcastManager.getInstance().getOnlineUsers();
            if(tempList.isEmpty()){
                mStatusView.showEmpty();
            }else {
                mOnlineUsers.addAll(mRvMainAdapter.wrap(tempList));
                mRvMainAdapter.notifyDataSetChanged();
                mStatusView.showSuccess();
            }
        }, 2000);
    }

    @OnClick({R.id.iv_scan})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_scan:
//                if(!PingManager.getInstance().isScanning()){
//                    mStatusView.showLoading();
//                    PingManager.getInstance().startScan();
//                }
                mStatusView.showLoading();
                new Handler().postDelayed(() -> {
                    List<String> tempList = BroadcastManager.getInstance().getOnlineUsers();
                    if(tempList.isEmpty()){
                        mStatusView.showEmpty();
                    }else {
                        mOnlineUsers.addAll(mRvMainAdapter.wrap(tempList));
                        mRvMainAdapter.notifyDataSetChanged();
                        mStatusView.showSuccess();
                    }
                }, 2000);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        BroadcastManager.getInstance().exit();
        super.onBackPressed();
    }

    /**
     * 监听Wifi网络变化广播
     */
    public class WifiConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                    NetworkInfo.State state = networkInfo.getState();
                    switch (state){
                        case CONNECTED:
                            LogUtils.d(TAG, "wifi已经连接");
                            mStatusView.showLoading();
                            PingManager.getInstance().startScan();
                            if(mGotoWifiSettingsDialog.isAdded()) mGotoWifiSettingsDialog.dismiss();
                            break;
                        case DISCONNECTED:
                            LogUtils.d(TAG, "wifi已经断开");
                            if(mConnectingDialog.isAdded()) mConnectingDialog.dismiss();
                            mGotoWifiSettingsDialog.show(getSupportFragmentManager());
                            break;
                        default:
                            LogUtils.d(TAG, "wifi已其他状态 = " + state);
                            break;
                    }
                }
            }
            if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                switch (state){
                    case WifiManager.WIFI_STATE_ENABLED:
                        LogUtils.d(TAG, "wifi已经打开");
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        LogUtils.d(TAG, "wifi已经关闭");
                        break;
                    default:
                        LogUtils.d(TAG, "wifi的其他状态 = " + state);
                        break;
                }
            }
        }
    }

}
