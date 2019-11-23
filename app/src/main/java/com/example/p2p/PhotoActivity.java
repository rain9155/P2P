package com.example.p2p;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p2p.adapter.RvFolderAdapter;
import com.example.p2p.adapter.RvPhotoAdapter;
import com.example.p2p.app.App;
import com.example.p2p.base.activity.BaseActivity;
import com.example.p2p.bean.Folder;
import com.example.p2p.bean.Photo;
import com.example.p2p.config.Constant;
import com.example.p2p.decoration.GridLayoutItemDivider;
import com.example.p2p.utils.LoadPhotoUtil;
import com.example.p2p.utils.TimeUtil;
import com.example.p2p.widget.dialog.ShowFoldersPopup;
import com.example.p2p.widget.helper.ChangeArrowHelper;
import com.example.p2p.widget.helper.ChangeTimeHelper;
import com.example.permission.PermissionHelper;
import com.example.permission.bean.Permission;
import com.example.permission.callback.IPermissionCallback;
import com.example.utils.CommonUtils;
import com.example.utils.DisplayUtils;
import com.example.utils.StatusBarUtils;
import com.example.utils.ToastUtils;
import com.lxj.xpopup.XPopup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;

public class PhotoActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.rv_photos)
    RecyclerView rvPhotos;
    @BindView(R.id.tv_photo_data)
    TextView tvPhotoData;
    @BindView(R.id.ib_is_raw)
    ImageButton ibIsRaw;
    @BindView(R.id.tv_raw_photo)
    TextView tvRawPhoto;
    @BindView(R.id.tv_pre_view)
    TextView tvPreviewPhoto;
    @BindView(R.id.helper_change_time)
    ChangeTimeHelper helperChangeTime;
    @BindView(R.id.cl_photo_bottom)
    ConstraintLayout clBottomLayout;
    @BindView(R.id.helper_change_arrow)
    ChangeArrowHelper helperChangeArrow;
    @BindView(R.id.iv_arrow)
    ImageButton ivArrow;

    private static final int REQUEST_UPDATE_SELECT_PHOTOS = 0x000;
    private static final int SPAN_COUNT = 4;
    private boolean isRawPhoto;
    private List<Photo> mPhotos;
    private List<Folder> mFolders;
    private RvPhotoAdapter mPhotoAdapter;
    private RvFolderAdapter mFolderAdapter;
    private GridLayoutManager mPhotoLayoutManager;
    private ShowFoldersPopup mShowFoldersPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.immersive(getWindow(), ContextCompat.getColor(this, R.color.colorPhotoBg));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_photo;
    }

    @Override
    protected void initView() {
        updateSelectCount(0);
        ibIsRaw.setSelected(isRawPhoto);

        //照片墙
        mPhotos = new ArrayList<>();
        mPhotoAdapter = new RvPhotoAdapter(mPhotos, R.layout.item_photo_photos);
        mPhotoLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        rvPhotos.setAdapter(mPhotoAdapter);
        rvPhotos.setLayoutManager(mPhotoLayoutManager);
        rvPhotos.addItemDecoration(new GridLayoutItemDivider(this));

        //文件夹列表
        int height = (int) (DisplayUtils.getScreenHeight(this) / 1.2);
        mShowFoldersPopup = (ShowFoldersPopup) new XPopup.Builder(this)
                .atView(toolBar)
                .maxHeight(height)
                .customAnimator(new ShowFoldersPopup.ScrollFromTopAnim())
                .asCustom(new ShowFoldersPopup(this, helperChangeArrow));
        RecyclerView rvFolders = mShowFoldersPopup.findViewById(R.id.rv_show_folders);
        mFolders = new ArrayList<>();
        mFolderAdapter = new RvFolderAdapter(mFolders, R.layout.item_photo_folders);
        rvFolders.setAdapter(mFolderAdapter);
        rvFolders.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void initCallback() {
        //加载图片
        PermissionHelper.getInstance().with(this).requestPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                new IPermissionCallback() {
                    @Override
                    public void onAccepted(Permission permission) {
                        LoadPhotoUtil.loadPhotosFromExternal(App.getContext(), (folders, allPhotos) -> runOnUiThread(() -> {
                            mPhotoAdapter.setNewPhotos(allPhotos);
                            mFolderAdapter.setNewFolders(folders);
                        }));
                    }

                    @Override
                    public void onDenied(Permission permission) {
                        ToastUtils.showToast(App.getContext(), getString(R.string.toast_permission_rejected));
                        finish();
                    }
                });

        //照片墙
        rvPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) helperChangeTime.hidePhotoTime();
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                changePhotoTime(recyclerView.getScrollState());
            }
        });
        mPhotoAdapter.setOnItemClickListener((adapter, view, position) -> {
            PreViewActivity.startActivityForResult(
                    this,
                    mPhotos,
                    mPhotoAdapter.getSelectPhotos(),
                    position,
                    false,
                    isRawPhoto,
                    REQUEST_UPDATE_SELECT_PHOTOS
            );
        });
        mPhotoAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (CommonUtils.isEmptyList(mPhotos)) return;
            checkAndSelectPhoto(position);
            updateSelectCount(mPhotoAdapter.getSelectPhotoCount());
        });

        //文件夹列表
        mFolderAdapter.setOnItemClickListener((adapter, view, position) -> {
            int prePos = mFolderAdapter.getPrePosition();
            if (prePos == position) return;
            mFolderAdapter.updateFolderByPos(!mFolders.get(position).isSelect, position);
            mPhotoAdapter.setNewPhotos(mFolders.get(position).photos);
            mShowFoldersPopup.dismiss();
            tvTitle.setText(mFolders.get(position).name);
        });

    }

    @OnClick({R.id.tv_pre_view, R.id.iv_back, R.id.tv_title, R.id.iv_arrow, R.id.btn_send, R.id.ib_is_raw, R.id.tv_raw_photo})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_send:
                startChatActivity();
                break;
            case R.id.tv_title:
            case R.id.iv_arrow:
                if (mShowFoldersPopup.isShow()) {
                    mShowFoldersPopup.dismiss();
                } else {
                    mShowFoldersPopup.show();
                }
                break;
            case R.id.tv_pre_view:
                PreViewActivity.startActivityForResult(
                        this,
                        new LinkedList<>(mPhotoAdapter.getSelectPhotos()),
                        mPhotoAdapter.getSelectPhotos(),
                        0,
                        true,
                        isRawPhoto,
                        REQUEST_UPDATE_SELECT_PHOTOS);
                break;
            case R.id.ib_is_raw:
            case R.id.tv_raw_photo:
                updateRaw(!isRawPhoto);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_UPDATE_SELECT_PHOTOS) {
                int[] rang = data.getIntArrayExtra(Constant.KEY_MIN_MAX_UPDATE_POS);
                updateRaw(data.getBooleanExtra(Constant.KEY_IS_RAW_PHOTO, isRawPhoto));
                if (rang == null) {
                    mPhotoAdapter.notifyDataSetChanged();
                } else {
                    mPhotoAdapter.notifyItemRangeChanged(rang[0], rang[1] - rang[0] + 1);
                }
                updateSelectCount(mPhotoAdapter.getSelectPhotoCount());
            }
        }
    }


    @Override
    public void finish() {
        if(mShowFoldersPopup.isShow()){
            mShowFoldersPopup.dismiss();
        }else {
            super.finish();
        }
    }

    /**
     * 发送选择的照片路径给聊天界面
     */
    private void startChatActivity() {
        List<Photo> selectedPhotos = mPhotoAdapter.getSelectPhotos();
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
     * 更新时间条时间
     */
    private void changePhotoTime(int scrollState) {
        if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            helperChangeTime.hidePhotoTime();
        } else {
            helperChangeTime.showPhotoTime();
            int firstVisibleItem = mPhotoLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem != RecyclerView.NO_POSITION) {
                Photo photo = mPhotos.get(firstVisibleItem);
                tvPhotoData.setText(TimeUtil.getTime(photo.time * 1000));
            }
        }
    }

    /**
     * 选择或取消选择照片
     */
    private void checkAndSelectPhoto(int position) {
        int selectCount = mPhotoAdapter.getSelectPhotoCount();
        Photo photo = mPhotos.get(position);
        boolean isSelected = !photo.isSelect;
        if (selectCount < Constant.MAX_SELECTED_PHOTO) {
            mPhotoAdapter.setSelectPhotoByPos(isSelected, position, photo);
        } else if (!isSelected) {
            mPhotoAdapter.setSelectPhotoByPos(false, position, photo);
        } else {
            ToastUtils.showToast(App.getContext(), getString(R.string.photo_max_btnSend, Constant.MAX_SELECTED_PHOTO));
        }
    }

    /**
     * 更新控价的照片选择数量
     */
    private void updateSelectCount(int selectCount) {
        if (selectCount == 0) {
            btnSend.setEnabled(false);
            tvPreviewPhoto.setEnabled(false);
            btnSend.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryText));
            tvPreviewPhoto.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryText));
            btnSend.setText(getString(R.string.chat_btnSend));
            tvPreviewPhoto.setText(getString(R.string.photo_tvPreviewPhoto));
        } else {
            btnSend.setEnabled(true);
            tvPreviewPhoto.setEnabled(true);
            btnSend.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            tvPreviewPhoto.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            btnSend.setText(getString(R.string.photo_btnSend, selectCount, Constant.MAX_SELECTED_PHOTO));
            tvPreviewPhoto.setText(getString(R.string.photo_tvPreviewPhoto2, selectCount));
        }
    }

    /**
     * 更新是否原图按钮
     */
    private void updateRaw(boolean b) {
        isRawPhoto = b;
        ibIsRaw.setSelected(isRawPhoto);
    }



}
