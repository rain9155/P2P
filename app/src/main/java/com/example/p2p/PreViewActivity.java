package com.example.p2p;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import java.util.List;

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

    private RvPreBottomAdapter mBottomAdapter;
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
        mBottomAdapter = new RvPreBottomAdapter(mSelectPhotos, R.layout.item_pre_bottom);
        rvBottomPreView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvBottomPreView.setAdapter(mBottomAdapter);
        mBottomAdapter.setSelectPhoto(mPreViewPhotos.get(mPos));

        mPreViewAdapter = new RvPreViewAdapter(mPreViewPhotos, R.layout.item_pre);
        rvPreView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvPreView.setAdapter(mPreViewAdapter);
        new PagerSnapHelper().attachToRecyclerView(rvPreView);
        rvPreView.scrollToPosition(mPos);

        updateBottom();
        updateTitle(mPos);
        updateSend(mSelectPhotos.size());
        updateSelect(mPreViewPhotos.get(mPos).isSelect);
    }

    private void updateBottom() {
        if (CommonUtil.isEmptyList(mSelectPhotos)) {
            rvBottomPreView.setVisibility(View.INVISIBLE);
            divider.setVisibility(View.INVISIBLE);
        }else {
            rvBottomPreView.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        }
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
                    Photo selectPhoto = mPreViewPhotos.get(pos);
                    //与底部列表联动
                    mBottomAdapter.setSelectPhoto(selectPhoto);
                    rvBottomPreView.smoothScrollToPosition(selectPhoto.selectPos);
                    pos = isJumpFromPreBtn ? pos : selectPhoto.position;
                    //更新其他属性
                    updateTitle(pos);
                    updateSelect(mPreViewPhotos.get(pos).isSelect);
                    mPos = pos;
                }
            }
        });

        mBottomAdapter.setOnItemClickListener((adapter, view, position) -> {
            Photo selectPhoto = mSelectPhotos.get(position);
            rvBottomPreView.smoothScrollToPosition(position);
            //与预览列表联动
            mBottomAdapter.setSelectPhoto(selectPhoto);
            int pos = isJumpFromPreBtn ? position : selectPhoto.position;
            rvPreView.scrollToPosition(pos);
            //更新其他属性
            updateTitle(pos);
            updateSelect(true);
            mPos = pos;
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
                Photo photo = mPreViewPhotos.get(mPos);
                boolean isSelect = !photo.isSelect;
                mBottomAdapter.updateSelectPhoto(isSelect, photo);
                updateBottom();
                updateSend(mSelectPhotos.size());
                updateSelect(isSelect);
                break;
            default:
                break;
        }
    }

    @Override
    public void finish() {
        for(Photo photo : mSelectPhotos){
            photo.isSelect = true;
        }
        setResult(RESULT_OK);
        super.finish();
    }

    private void updateSelect(boolean isSelect) {
        ibIsSelect.setSelected(isSelect);
    }


    @SuppressLint("SetTextI18n")
    private void updateTitle(int position) {
        int preViewCount = mPreViewPhotos.size();
        tvTitle.setText((position + 1) + "/" + preViewCount);
    }

    private void updateSend(int count) {
        if(count == 0){
            btnSend.setText(getString(R.string.chat_btnSend));
        }else {
            btnSend.setText(getString(R.string.photo_btnSend, count, Constant.MAX_SELECTED_PHOTO));
        }
    }

    public static void startActivity(Activity activity, List<Photo> preViewPhotos, List<Photo> selectPhotos, int clickPos, boolean mode) {
        mTempPreViewPhotos = preViewPhotos;
        mTempSelectPhotos = selectPhotos;
        Intent intent = new Intent(activity, PreViewActivity.class);
        intent.putExtra(Constant.KEY_CLICK_POSITION, clickPos);
        intent.putExtra(Constant.KEY_MODE, mode);
        activity.startActivityForResult(intent, Constant.REQUEST_UPDATA_SELECT_PHOTOS);
    }

}
