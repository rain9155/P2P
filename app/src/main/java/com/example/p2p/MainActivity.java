package com.example.p2p;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.p2p.base.BaseActivity;
import com.example.p2p.kernel.Ping;
import com.example.p2p.utils.IpUtils;
import com.example.p2p.utils.LogUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    private String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.iv_back)
    ImageView ivBack;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    @Override
    protected void initView() {
        ivBack.setVisibility(View.GONE);
        tvTitle.setText(getString(R.string.main_tlTitle));
    }

    @Override
    protected void loadData() {
        Ping.getInstance().start();
    }

    public void onTextViewClick(View view) {
        startActivity(new Intent(this, ChatActivity.class));
    }

}
