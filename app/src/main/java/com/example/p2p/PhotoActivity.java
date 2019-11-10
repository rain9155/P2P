package com.example.p2p;

import android.Manifest;
import android.content.Context;
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
import com.example.p2p.utils.PhotoUtil;
import com.example.p2p.utils.TimeUtil;
import com.example.p2p.widget.dialog.ShowFoldersPopup;
import com.example.p2p.widget.helper.ChangeArrowHelper;
import com.example.p2p.widget.helper.ChangeTimeHelper;
import com.example.permission.PermissionHelper;
import com.example.permission.bean.Permission;
import com.example.permission.callback.IPermissionCallback;
import com.example.utils.CommonUtil;
import com.example.utils.StatusBarUtils;
import com.example.utils.ToastUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    @BindView(R.id.ib_select_raw)
    ImageButton ibSelectRaw;
    @BindView(R.id.tv_raw_photo)
    TextView tvRawPhoto;
    @BindView(R.id.tv_is_select)
    TextView tvPreviewPhoto;
    @BindView(R.id.helper_change_time)
    ChangeTimeHelper helperChangeTime;
    @BindView(R.id.cl_photo_bottom)
    ConstraintLayout clBottomLayout;
    @BindView(R.id.helper_change_arrow)
    ChangeArrowHelper helperChangeArrow;
    @BindView(R.id.iv_arrow)
    ImageButton ivArrow;

    private List<Photo> mPhotos;
    private List<Folder> mFolders;
    private RvPhotoAdapter mPhotoAdapter;
    private RvFolderAdapter mFolderAdapter;
    private GridLayoutManager mPhotoLayoutManager;
    private ShowFoldersPopup mShowFoldersPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.immersive(getWindow(), R.color.colorPhotoBg);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_photo;
    }

    @Override
    protected void initView() {
        updateSelectCount(0);

        //照片墙
        mPhotos = new ArrayList<>();
        mPhotoAdapter = new RvPhotoAdapter(mPhotos, R.layout.item_photo_photos);
        mPhotoLayoutManager = new GridLayoutManager(this, 4);
        rvPhotos.setAdapter(mPhotoAdapter);
        rvPhotos.setLayoutManager(mPhotoLayoutManager);
        rvPhotos.addItemDecoration(new GridLayoutItemDivider(this));

        //底部Dialog
        View view = getLayoutInflater().inflate(R.layout.popup_show_folders, null);
        mShowFoldersPopup = new ShowFoldersPopup(this, helperChangeArrow);
        mShowFoldersPopup.setContentView(view);

        //文件夹列表
        RecyclerView rvFolders = view.findViewById(R.id.rv_show_folders);
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

    @OnClick({R.id.tv_is_select, R.id.iv_back, R.id.tv_title, R.id.iv_arrow})
    public void onViewClick(View view) {
        switch (view.getId()) {
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
            case R.id.tv_title:
            case R.id.iv_arrow:
                if (mShowFoldersPopup.isShowing()) {
                    mShowFoldersPopup.dismiss();
                } else {
                    mShowFoldersPopup.showAsDropDown(toolBar);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.REQUEST_UPDATE_SELECT_PHOTOS) {
                int[] rang = data.getIntArrayExtra(Constant.KEY_MIN_MAX_UPDATE_POS);
                if (rang == null) {
                    mPhotoAdapter.notifyDataSetChanged();
                } else {
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

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, PhotoActivity.class));
    }

}
