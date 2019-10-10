package com.example.p2p;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p2p.adapter.RvFolderAdapter;
import com.example.p2p.adapter.RvPhotoAdapter;
import com.example.p2p.app.App;
import com.example.p2p.base.activity.BaseActivity;
import com.example.p2p.bean.Folder;
import com.example.p2p.config.Constant;
import com.example.p2p.decoration.GridLayoutItemDivider;
import com.example.p2p.utils.PhotoUtil;
import com.example.p2p.utils.TimeUtil;
import com.example.p2p.widget.helper.PhotoActivityHelper;
import com.example.permission.PermissionHelper;
import com.example.permission.bean.Permission;
import com.example.permission.callback.IPermissionCallback;
import com.example.utils.CommonUtil;
import com.example.utils.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
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
    @BindView(R.id.tv_folder_name)
    TextView tvFolderName;
    @BindView(R.id.iv_photo)
    ImageView ivPhoto;
    @BindView(R.id.ib_select_raw)
    ImageButton ibSelectRaw;
    @BindView(R.id.tv_raw_photo)
    TextView tvRawPhoto;
    @BindView(R.id.tv_is_select)
    TextView tvPreviewPhoto;
    @BindView(R.id.helper)
    PhotoActivityHelper helper;

    private List<Photo> mPhotos;
    private List<Folder> mFolders;
    private BottomSheetDialog mShowFoldersDialog;
    private RvPhotoAdapter mPhotoAdapter;
    private RvFolderAdapter mFolderAdapter;
    private GridLayoutManager mPhotoLayoutManager;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_photo;
    }

    @Override
    protected void initView() {
        btnSend.setEnabled(false);
        tvPreviewPhoto.setEnabled(false);

        //照片墙
        mPhotos = new ArrayList<>();
        mPhotoAdapter = new RvPhotoAdapter(mPhotos, R.layout.item_photo_photos);
        mPhotoLayoutManager = new GridLayoutManager(this, 4);
        rvPhotos.setAdapter(mPhotoAdapter);
        rvPhotos.setLayoutManager(mPhotoLayoutManager);
        rvPhotos.addItemDecoration(new GridLayoutItemDivider(this));

        //底部Dialog
        mShowFoldersDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_show_folders, null);
        mShowFoldersDialog.setContentView(view);

        //文件夹列表
        RecyclerView rvFolders = view.findViewById(R.id.rv_show_folders);
        mFolders = new ArrayList<>();
        mFolderAdapter = new RvFolderAdapter(mFolders, R.layout.item_photo_folders);
        rvFolders.setAdapter(mFolderAdapter);
        rvFolders.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void initCallback() {
        PermissionHelper.getInstance().with(this).requestPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                new IPermissionCallback() {
                    @Override
                    public void onAccepted(Permission permission) {
                        PhotoUtil.loadPhotosFromExternal(App.getContext(), (folders, allPhotos) -> runOnUiThread(() -> {
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

        mPhotoAdapter.setOnItemClickListener((adapter, view, position) -> {
            PreViewActivity.startActivity(
                    this,
                    mPhotos,
                    mPhotoAdapter.getSelectPhotos(),
                    position,
                    false
            );
        });
        mPhotoAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (CommonUtil.isEmptyList(mPhotos)) return;
            checkAndSelectPhoto(position);
            updateSelectCount(mPhotoAdapter.getSelectPhotoCount());
        });

        mFolderAdapter.setOnItemClickListener((adapter, view, position) -> {
            int prePos = mFolderAdapter.getPrePosition();
            if (prePos == position) return;
            mFolderAdapter.updateFolderByPos(!mFolders.get(position).isSelect, position);
            mPhotoAdapter.setNewPhotos(mFolders.get(position).photos);
            mShowFoldersDialog.dismiss();
            tvFolderName.setText(mFolders.get(position).name);
        });

        rvPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE) helper.hidePhotoTime();
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                changePhotoTime(recyclerView.getScrollState());
            }
        });


    }

    @OnClick({R.id.tv_folder_name, R.id.tv_is_select, R.id.iv_back})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.tv_folder_name:
                if (mShowFoldersDialog.isShowing()) {
                    mShowFoldersDialog.dismiss();
                } else {
                    mShowFoldersDialog.show();
                }
                break;
            case R.id.tv_is_select:
                PreViewActivity.startActivity(
                        this,
                        new LinkedList<>(mPhotoAdapter.getSelectPhotos()),
                        mPhotoAdapter.getSelectPhotos(),
                        0,
                        true);
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == Constant.REQUEST_UPDATE_SELECT_PHOTOS){
                int[] rang = data.getIntArrayExtra(Constant.KEY_MIN_MAX_UPDATE_POS);
                if(rang == null){
                    mPhotoAdapter.notifyDataSetChanged();
                }else {
                    mPhotoAdapter.notifyItemRangeChanged(rang[0], rang[1] - rang[0] + 1);
                }
                updateSelectCount(mPhotoAdapter.getSelectPhotoCount());
            }
        }
    }

    /**
     * 更新时间条时间
     */
    private void changePhotoTime(int scrollState) {
        if(scrollState == RecyclerView.SCROLL_STATE_IDLE){
            helper.hidePhotoTime();
        }else {
            helper.showPhotoTime();
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
        if(selectCount == 0){
            btnSend.setEnabled(false);
            tvPreviewPhoto.setEnabled(false);
            btnSend.setText(getString(R.string.chat_btnSend));
            tvPreviewPhoto.setText(getString(R.string.photo_tvPreviewPhoto));
        }else {
            btnSend.setEnabled(true);
            tvPreviewPhoto.setEnabled(true);
            btnSend.setText(getString(R.string.photo_btnSend, selectCount, Constant.MAX_SELECTED_PHOTO));
            tvPreviewPhoto.setText(getString(R.string.photo_tvPreviewPhoto2, selectCount));
        }
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, PhotoActivity.class));
    }

}
