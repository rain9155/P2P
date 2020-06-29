package com.example.p2p;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p2p.adapter.RvBottomPhotoAdapter;
import com.example.p2p.adapter.RvPreViewAdapter;
import com.example.p2p.app.App;
import com.example.p2p.base.activity.BaseActivity;
import com.example.p2p.bean.Photo;
import com.example.p2p.config.Constant;
import com.example.p2p.widget.helper.ToolbarHelper;
import com.example.utils.CommonUtils;
import com.example.utils.StatusBarUtils;
import com.example.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
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
    @BindView(R.id.ib_is_raw)
    ImageButton ibIsRaw;
    @BindView(R.id.tv_raw_photo)
    TextView tvRawPhoto;
    @BindView(R.id.tv_pre_view)
    TextView tvIsSelect;
    @BindView(R.id.cl_bottom)
    ConstraintLayout clBottom;
    @BindView(R.id.helper_change_time)
    ToolbarHelper helper;
    @BindView(R.id.rv_preView)
    RecyclerView rvPreView;
    @BindView(R.id.ib_is_select)
    ImageButton ibIsSelect;

    private static final String KEY_CLICK_POSITION = "clickPos";
    private static final String KEY_MODE = "mode";
    private static List<Photo> mTempPreViewPhotos, mTempSelectPhotos;
    private List<Photo> mPreViewPhotos, mSelectPhotos;//依次为预览照片列表，底部的已选择照片列表

    private RvBottomPhotoAdapter mBottomAdapter;
    private RvPreViewAdapter mPreViewAdapter;
    private boolean isRawPhoto;
    private int mPos;
    private boolean isJumpFromPreBtn;//从PhotoActivity点击列表跳转还是点击预览跳转

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreViewPhotos = mTempPreViewPhotos;
        mSelectPhotos = mTempSelectPhotos;
        mTempPreViewPhotos = null;
        mTempSelectPhotos = null;
        mPos = getIntent().getIntExtra(KEY_CLICK_POSITION, 0);
        isJumpFromPreBtn = getIntent().getBooleanExtra(KEY_MODE, true);
        isRawPhoto = getIntent().getBooleanExtra(Constant.KEY_IS_RAW_PHOTO, isRawPhoto);
        super.onCreate(savedInstanceState);
        StatusBarUtils.immersive(this, ContextCompat.getColor(this, R.color.colorPhotoBg));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pre_view;
    }

    @Override
    protected void initView() {
        //预览照片列表
        mPreViewAdapter = new RvPreViewAdapter(mPreViewPhotos, R.layout.item_pre_view);
        rvPreView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvPreView.setAdapter(mPreViewAdapter);
        new PagerSnapHelper().attachToRecyclerView(rvPreView);
        rvPreView.scrollToPosition(mPos);

        //底部已选择照片列表
        mBottomAdapter = new RvBottomPhotoAdapter(mSelectPhotos, R.layout.item_pre_bottom);
        rvBottomPreView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvBottomPreView.setAdapter(mBottomAdapter);
        mBottomAdapter.setSelectPhoto(mPreViewPhotos.get(mPos));

        //初始化控件
        setBottomVisibility();
        updateTitle(mPos);
        updateSend(mSelectPhotos.size());
        ibIsSelect.setSelected(mPreViewPhotos.get(mPos).isSelect);
        ibIsRaw.setSelected(isRawPhoto);
    }

    @Override
    protected void initCallback() {
        //预览照片列表
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
                    //更新其他控件
                    updateTitle(pos);
                    ibIsSelect.setSelected(mPreViewPhotos.get(pos).isSelect);
                    mPos = pos;
                }
            }
        });

        //底部已选择照片列表
        mBottomAdapter.setOnItemClickListener((adapter, view, position) -> {
            Photo selectPhoto = mSelectPhotos.get(position);
            rvBottomPreView.smoothScrollToPosition(position);
            //与预览列表联动
            mBottomAdapter.setSelectPhoto(selectPhoto);
            int pos = isJumpFromPreBtn ? position : selectPhoto.position;
            rvPreView.scrollToPosition(pos);
            //更新其他控件
            updateTitle(pos);
            ibIsSelect.setSelected(true);
            mPos = pos;
        });

    }

    @OnClick({R.id.iv_back, R.id.tv_pre_view, R.id.ib_is_select, R.id.btn_send, R.id.ib_is_raw, R.id.tv_raw_photo})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_send:
                startChatActivity();
                break;
            case R.id.tv_pre_view:
            case R.id.ib_is_select:
                checkAndUpdateSelectCount();
                break;
            case R.id.ib_is_raw:
            case R.id.tv_raw_photo:
                updateRaw();
                break;
            default:
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            if(!helper.isShow()){
                helper.setStatusBarVisibility(false);
            }
        }
    }

    @Override
    public void finish() {
        setResult();
        super.finish();
    }

    /**
     * 发送选择的照片路径给聊天界面
     */
    private void startChatActivity() {
        List<Photo> selectedPhotos = mSelectPhotos;
        if(selectedPhotos.isEmpty()){
            selectedPhotos.add(mPreViewPhotos.get(mPos));
        }
        Collections.sort(selectedPhotos, (o1, o2) -> Integer.compare(o1.position, o2.position));
        ArrayList<String> paths = new ArrayList<>(selectedPhotos.size());
        for(Photo photo : selectedPhotos){
            paths.add(photo.path);
        }
        Intent result = new Intent(this, ChatActivity.class);
        result.putStringArrayListExtra(Constant.KEY_CHOOSE_PHOTOS_PATH, paths);
        result.putExtra(Constant.KEY_IS_RAW_PHOTO, isRawPhoto);
        startActivity(result);
    }

    /**
     * 更新标题控件
     */
    @SuppressLint("SetTextI18n")
    private void updateTitle(int position) {
        int preViewCount = mPreViewPhotos.size();
        tvTitle.setText((position + 1) + "/" + preViewCount);
    }

    /**
     * 更新发送按钮控件
     */
    private void updateSend(int count) {
        if(count == 0){
            btnSend.setText(getString(R.string.chat_btnSend));
        }else {
            btnSend.setText(getString(R.string.photo_btnSend, count, Constant.MAX_SELECTED_PHOTO));
        }
    }

    /**
     * Bottom的RecyclerView
     */
    private void setBottomVisibility() {
        if (CommonUtils.isEmptyList(mSelectPhotos)) {
            rvBottomPreView.setVisibility(View.INVISIBLE);
            divider.setVisibility(View.INVISIBLE);
        }else {
            rvBottomPreView.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新照片的选择数量
     */
    private void checkAndUpdateSelectCount() {
        Photo photo = mPreViewPhotos.get(mPos);
        boolean isSelect = !photo.isSelect;
        int selectCount = mSelectPhotos.size();
        if(selectCount < Constant.MAX_SELECTED_PHOTO){
            mBottomAdapter.updateSelectPhoto(isSelect, photo);
        }else if(!isSelect){
            mBottomAdapter.updateSelectPhoto(false, photo);
        }else {
            ToastUtils.showToast(App.getContext(), getString(R.string.photo_max_btnSend, Constant.MAX_SELECTED_PHOTO));
        }
        //更新控件
        setBottomVisibility();
        updateSend(mSelectPhotos.size());
        ibIsSelect.setSelected(isSelect);
    }

    /**
     * 更新是否原图按钮
     */
    private void updateRaw() {
        isRawPhoto = !isRawPhoto;
        ibIsRaw.setSelected(isRawPhoto);
    }

    /**
     * 返回列表的更新范围给上一个活动
     */
    private void setResult() {
        int minPos = Integer.MAX_VALUE;
        int maxPos = Integer.MIN_VALUE;
        for(Photo photo : mSelectPhotos){
            photo.isSelect = true;
            if(photo.position < minPos){
                minPos = photo.position;
            }
            if(photo.position > maxPos){
                maxPos = photo.position;
            }
        }
        for(Photo photo : mBottomAdapter.getUnSelectedPhotos()){
            if(photo.position < minPos){
                minPos = photo.position;
            }
            if(photo.position > maxPos){
                maxPos = photo.position;
            }
        }
        Intent data = new Intent();
        data.putExtra(Constant.KEY_MIN_MAX_UPDATE_POS, new int[]{minPos, maxPos});
        data.putExtra(Constant.KEY_IS_RAW_PHOTO, isRawPhoto);
        setResult(RESULT_OK, data);
    }


    /**
     * @param preViewPhotos 需要预览的照片列表
     * @param selectPhotos 底部展示的照片列表，即已经选择了的的照片列表
     * @param clickPos 点击的照片的位置
     * @param mode 是从照片墙点击，还是从预览点击
     */
    public static void startActivityForResult(Activity activity, List<Photo> preViewPhotos, List<Photo> selectPhotos, int clickPos, boolean mode, boolean isRawPhoto, int requestCode) {
        mTempPreViewPhotos = preViewPhotos;
        mTempSelectPhotos = selectPhotos;
        Intent intent = new Intent(activity, PreViewActivity.class);
        intent.putExtra(KEY_CLICK_POSITION, clickPos);
        intent.putExtra(KEY_MODE, mode);
        intent.putExtra(Constant.KEY_IS_RAW_PHOTO, isRawPhoto);
        activity.startActivityForResult(intent, requestCode);
    }

}
