package com.example.p2p;

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
import com.example.p2p.callback.IScanCallback;
import com.example.p2p.kernel.Scan;
import com.example.p2p.utils.WifiUtils;
import com.example.p2p.widget.dialog.GotoWifiSettingsDialog;

import java.util.List;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.rv_main)
    RecyclerView rvMain;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.iv_back)
    ImageView ivBack;

    private String TAG = MainActivity.class.getSimpleName();
    private GotoWifiSettingsDialog mScanLoadingDialog;
    private RvMainAdapter mRvMainAdapter;
    private StatusView mStatusView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    @Override
    protected void initView() {
        ivBack.setVisibility(View.GONE);
        tvTitle.setText(getString(R.string.main_tlTitle));
        mScanLoadingDialog = new GotoWifiSettingsDialog();
        mStatusView = Loading.beginBuildStatusView(this)
                .warp(findViewById(R.id.rv_main))
                .addLoadingView(R.layout.main_scanning)
                .addEmptyView(R.layout.main_scan_empty)
                .create();
        rvMain.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }

    @Override
    protected void loadData(){
        mStatusView.showLoading();
        Scan.getInstance().start(this);
        Scan.getInstance().setScanCallback(new IScanCallback() {
            @Override
            public void onScanSuccess(List<String> pingSuccessList) {
                mRvMainAdapter = new RvMainAdapter(pingSuccessList, R.layout.item_main);
            }

            @Override
            public void onScanEmpty() {
                mStatusView.showEmpty();
            }

            @Override
            public void onScanError() {
                WifiUtils.gotoWifiSettings(MainActivity.this, 0);
            }
        });
    }

}
