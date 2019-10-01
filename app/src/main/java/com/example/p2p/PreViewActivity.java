package com.example.p2p;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.p2p.base.activity.BaseActivity;
import com.example.p2p.widget.helper.PreActivityHelper;

import butterknife.BindView;
import butterknife.OnClick;

public class PreViewActivity extends BaseActivity {

    @BindView(R.id.vp_preView)
    ViewPager vpPreView;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.rv_preView)
    RecyclerView rvPreView;
    @BindView(R.id.divider)
    View divider;
    @BindView(R.id.ib_select_raw)
    ImageButton ibSelectRaw;
    @BindView(R.id.tv_raw_photo)
    TextView tvRawPhoto;
    @BindView(R.id.tv_preview_photo)
    TextView tvPreviewPhoto;
    @BindView(R.id.cl_bottom)
    ConstraintLayout clBottom;
    @BindView(R.id.helper)
    PreActivityHelper helper;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pre_view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initView() {
        vpPreView.setOnTouchListener((v, event) -> {
            if(helper.isShow()){
                helper.hideTopBottom(this);
            }else {
                helper.showTopBottom(this);
            }
            return false;
        });

    }

    @Override
    protected void initCallback() {

    }

    @OnClick({R.id.iv_back})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, PreViewActivity.class));
    }

}
