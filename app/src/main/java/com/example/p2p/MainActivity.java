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

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loading.Loading;
import com.example.loading.StatusView;
import com.example.p2p.adapter.RvUsersAdapter;
import com.example.p2p.base.BaseActivity;
import com.example.p2p.bean.Data;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IUserCallback;
import com.example.p2p.callback.IConnectCallback;
import com.example.p2p.callback.IDialogCallback;
import com.example.p2p.core.OnlineUserManager;
import com.example.p2p.core.ConnectManager;
import com.example.p2p.utils.LogUtils;
import com.example.p2p.utils.WifiUtils;
import com.example.p2p.widget.dialog.ConnectingDialog;
import com.example.p2p.widget.dialog.GotoWifiSettingsDialog;
import com.example.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.rv_user)
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
    private static final int REQUEST_WIFI_ENABLE = 0x001;

    private RvUsersAdapter mRvMainAdapter;
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
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetWorkConnectionReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mNetWorkConnectionReceiver);
    }

    @Override
    protected void onDestroy() {
        OnlineUserManager.getInstance().exit();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_WIFI_ENABLE){
            if(WifiUtils.isWifiConnected(MainActivity.this)){
                refreshOnlineUsers();
            }else {
                //等待一下，如果用户返回过快，wifi可能正在连接中，但还连接上
                new Handler().postDelayed(() -> {
                    if(WifiUtils.isWifiConnected(MainActivity.this)){
                        refreshOnlineUsers();
                    }else {
                        if(mOnlineUsers.isEmpty())
                            mStatusView.showEmpty();
                    }
                }, 3000);
            }
        }
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
                .warp(findViewById(R.id.rv_user))
                .addLoadingView(R.layout.loading_view)
                .addEmptyView(R.layout.empty_view)
                .create();
        mOnlineUsers = new ArrayList<>();
        rvMain.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mRvMainAdapter = new RvUsersAdapter(mOnlineUsers, R.layout.item_user);
        rvMain.setAdapter(mRvMainAdapter);
        mGotoWifiSettingsDialog = new GotoWifiSettingsDialog();
        mConnectingDialog = new ConnectingDialog();
        mStatusView.showLoading();
        OnlineUserManager.getInstance().getOnlineUsers();
    }

    @Override
    protected void initCallback() {
        //item监听
        mRvMainAdapter.setOnItemClickListener((adapter, view, position) -> {
            if(mOnlineUsers.isEmpty()) return;
            mPosition = position;
            mConnectingDialog.show(getSupportFragmentManager());
            ConnectManager.getInstance().connect(mOnlineUsers.get(position).getIp());
        });
        mGotoWifiSettingsDialog.setDialogCallback(new IDialogCallback() {
            @Override
            public void onAgree() {
                WifiUtils.gotoWifiSettings(MainActivity.this, REQUEST_WIFI_ENABLE);
            }

            @Override
            public void onDismiss() {
                ToastUtil.showToast(MainActivity.this, getString(R.string.toast_wifi_noconnect));
                if(mOnlineUsers.isEmpty()){
                    mStatusView.showEmpty();
                    return;
                }
                mStatusView.showSuccess();
            }
        });
        //连接回调监听
        ConnectManager.getInstance().setConnectCallback(new IConnectCallback() {
            @Override
            public void onConnectSuccess(String targetIp) {
                mConnectingDialog.dismiss();
                ToastUtil.showToast(MainActivity.this, getString(R.string.main_connecting_success));
                ChatActivity.startActiivty(MainActivity.this, mOnlineUsers.get(mPosition), REQUEST_SOCKET_STATE);
            }

            @Override
            public void onConnectFail(String targetIp) {
                mConnectingDialog.dismiss();
                ToastUtil.showToast(MainActivity.this, getString(R.string.main_connecting_fail));
            }
        });
        //广播回调监听
        OnlineUserManager.getInstance().setUserCallback(new IUserCallback() {
            @Override
            public void onOnlineUsers(List<User> users) {
                mOnlineUsers.clear();
                if(users.isEmpty()){
                    mStatusView.showEmpty();
                }else {
                    mOnlineUsers.addAll(users);
                    mRvMainAdapter.notifyDataSetChanged();
                    mStatusView.showSuccess();
                }
            }

            @Override
            public void onJoin(User user){
                mOnlineUsers.add(user);
                mRvMainAdapter.notifyItemInserted(mOnlineUsers.size());
            }

            @Override
            public void onExit(User user) {
                ConnectManager.getInstance().removeConnect(user.getIp());
                int index = mOnlineUsers.indexOf(user);
                mOnlineUsers.remove(user);
                mRvMainAdapter.notifyItemRemoved(index);
            }
        });
    }


    @OnClick({R.id.iv_scan})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_scan:
                refreshOnlineUsers();
                break;
            default:
                break;
        }
    }

    /**
     * 刷新在线用户
     */
    private void refreshOnlineUsers() {
        mStatusView.showLoading();
        OnlineUserManager.getInstance().getOnlineUsers();
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
                            if(mGotoWifiSettingsDialog.isAdded()) mGotoWifiSettingsDialog.dismiss();
                            break;
                        case DISCONNECTED:
                            LogUtils.d(TAG, "wifi已经断开");
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
                        if(mConnectingDialog.isAdded()) mConnectingDialog.dismiss();
                        mGotoWifiSettingsDialog.show(getSupportFragmentManager());
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        LogUtils.d(TAG, "wifi关闭中...");
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        LogUtils.d(TAG, "wifi打开中...");
                        break;
                    default:
                        LogUtils.d(TAG, "wifi的其他状态 = " + state);
                        break;
                }
            }
        }
    }

    public static void startActivity(Context context){
        context.startActivity(new Intent(context, MainActivity.class));
    }
}
