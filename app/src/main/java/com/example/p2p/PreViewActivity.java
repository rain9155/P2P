package com.example.p2p;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p2p.adapter.RvPreBottomAdapter;
import com.example.p2p.adapter.RvPreViewAdapter;
import com.example.p2p.base.activity.BaseActivity;
import com.example.p2p.bean.Photo;
import com.example.p2p.config.Constant;
import com.example.p2p.widget.helper.PreActivityHelper;
import com.example.utils.CommonUtil;
import com.example.utils.StatusBarUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import butterknife.BindView;
import butterknife.OnClick;

public class PreViewActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.rv_bottom_preView)
    RecyclerView rvBottomPreView;
    @BindView(R.id.divider)
    View divider;
    @BindView(R.id.ib_select_raw)
    ImageButton ibSelectRaw;
    @BindView(R.id.tv_raw_photo)
    TextView tvRawPhoto;
    @BindView(R.id.tv_is_select)
    TextView tvIsSelect;
    @BindView(R.id.cl_bottom)
    ConstraintLayout clBottom;
    @BindView(R.id.helper)
    PreActivityHelper helper;
    @BindView(R.id.rv_preView)
    RecyclerView rvPreView;
    @BindView(R.id.ib_is_select)
    ImageButton ibIsSelect;

    private static List<Photo> mTempPreViewPhotos, mTempSelectPhotos;
    private static List<Photo> mPreViewPhotos, mSelectPhotos;

    private RvPreBottomAdapter mPreBottomAdapter;
    private RvPreViewAdapter mPreViewAdapter;
    private int mPos;
    private boolean isJumpFromPreBtn;//从PhotoActivity点击列表跳转还是点击预览跳转

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreViewPhotos = mTempPreViewPhotos;
        mSelectPhotos = mTempSelectPhotos;
        mTempPreViewPhotos = null;
        mTempSelectPhotos = null;
        mPos = getIntent().getIntExtra(Constant.KEY_CLICK_POSITION, 0);
        isJumpFromPreBtn = getIntent().getBooleanExtra(Constant.KEY_MODE, true);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState);
        StatusBarUtils.setHeightAndPadding(this, toolBar);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pre_view;
    }

    @Override
    protected void initView() {
        if (CommonUtil.isEmptyList(mSelectPhotos)) {
            rvBottomPreView.setVisibility(View.INVISIBLE);
            divider.setVisibility(View.INVISIBLE);
        }
        updateTitle(mPos);

        mPreBottomAdapter = new RvPreBottomAdapter(mSelectPhotos, R.layout.item_pre_bottom);
        rvBottomPreView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvBottomPreView.setAdapter(mPreBottomAdapter);

        mPreViewAdapter = new RvPreViewAdapter(mPreViewPhotos, R.layout.item_pre);
        rvPreView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvPreView.setAdapter(mPreViewAdapter);
        new PagerSnapHelper().attachToRecyclerView(rvPreView);
    }

    @Override
    protected void initCallback() {
        mPreViewAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (helper.isShow()) {
                helper.hideTopBottom(this);
            } else {
                helper.showTopBottom(this);
            }
        });

        rvPreView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int pos = layoutManager.findFirstVisibleItemPosition();
                        if (pos == RecyclerView.NO_POSITION) return;
                        rvBottomPreView.smoothScrollToPosition(pos);
                        mPreBottomAdapter.updatePhotoByPos(true, pos);
                        updateTitle(pos);
                }
            }
        });

        mPreBottomAdapter.setOnItemClickListener((adapter, view, position) -> {
            boolean isSelect = mSelectPhotos.get(position).isSelect;
            rvPreView.smoothScrollToPosition(position);
            mPreBottomAdapter.updatePhotoByPos(!isSelect, position);
            updateTitle(position);
        });

    }

    @OnClick({R.id.iv_back, R.id.tv_is_select, R.id.ib_is_select})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_is_select:
            case R.id.ib_is_select:
                break;
            default:
                break;
        }
    }


    @SuppressLint("SetTextI18n")
    private void updateTitle(int position) {
        int selectCount = mSelectPhotos.size();
        int preViewCount = mPreViewPhotos.size();
        if(isJumpFromPreBtn){
            tvTitle.setText((position + 1) + "/" + selectCount);
        }else{
            tvTitle.setText((position + 1) + "/" + preViewCount);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateSend(int count) {
        btnSend.setText("发送(" + count + "/9");
    }

    public static void startActivity(Context context, List<Photo> preViewPhotos, List<Photo> selectPhotos, int clickPos, boolean mode) {
        //清空所有选中照片的选择
        for(Photo photo : selectPhotos) photo.isSelect = false;
        mTempPreViewPhotos = preViewPhotos;
        mTempSelectPhotos = selectPhotos;
        Intent intent = new Intent(context, PreViewActivity.class);
        intent.putExtra(Constant.KEY_CLICK_POSITION, clickPos);
        intent.putExtra(Constant.KEY_MODE, mode);
        context.startActivity(intent);
    }

}
