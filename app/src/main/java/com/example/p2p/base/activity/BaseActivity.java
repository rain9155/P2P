package com.example.p2p.base.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.p2p.R;
import com.example.p2p.utils.CommonUtil;
import com.example.p2p.utils.StatusbarUtils;
import com.example.utils.StatusBarUtils;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract int getLayoutId();
    protected abstract void initView();
    protected abstract void initCallback();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        StatusbarUtils.immersive(getWindow(), R.color.colorPrimary);
        //CommonUtil.darkMode(this, true);
        initView();
        initCallback();
    }
}
