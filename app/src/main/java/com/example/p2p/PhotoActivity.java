package com.example.p2p;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
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
import com.example.p2p.utils.LogUtil;
import com.example.p2p.utils.PhotoUtil;
import com.example.p2p.utils.TimeUtil;
import com.example.permission.PermissionHelper;
import com.example.permission.bean.Permission;
import com.example.permission.callback.IPermissionCallback;
import com.example.utils.CommonUtil;
import com.example.utils.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
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
    @BindView(R.id.tv_photos)
    TextView tvPhotos;
    @BindView(R.id.iv_photo)
    ImageView ivPhoto;
    @BindView(R.id.ib_select_raw)
    ImageButton ibSelectRaw;
    @BindView(R.id.tv_raw_photo)
    TextView tvRawPhoto;
    @BindView(R.id.tv_preview_photo)
    TextView tvPreviewPhoto;

    private List<Photo> mPhotos;
    private List<Folder> mFolders;
    private BottomSheetDialog mShowFoldersDialog;
    private RvPhotoAdapter mPhotoAdapter;
    private RvFolderAdapter mFolderAdapter;
    private GridLayoutManager mPhotoLayoutManager;
    private RecyclerView rvFolders;
    private ObjectAnimator mHideTimeAnimator;
    private ObjectAnimator mShowTimeAnimator;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_photo;
    }

    @Override
    protected void initView() {
        ivBack.setOnClickListener(v -> finish());
        btnSend.setEnabled(false);

        //照片墙
        mPhotos = new ArrayList<>();
        mPhotoAdapter = new RvPhotoAdapter(mPhotos, R.layout.item_photo_photos);
        mPhotoLayoutManager = new GridLayoutManager(this, 4);
        rvPhotos.setAdapter(mPhotoAdapter);
        rvPhotos.setLayoutManager(mPhotoLayoutManager);

        //底部Dialog
        mShowFoldersDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_show_folders, null);
        mShowFoldersDialog.setContentView(view);

        //文件夹列表
        rvFolders = view.findViewById(R.id.rv_show_folders);
        mFolders = new ArrayList<>();
        mFolderAdapter = new RvFolderAdapter(mFolders, R.layout.item_photo_folders);
        rvFolders.setAdapter(mFolderAdapter);
        rvFolders.setLayoutManager(new LinearLayoutManager(this));

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initCallback() {
        PermissionHelper.getInstance().with(this).requestPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                new IPermissionCallback() {
                    @Override
                    public void onAccepted(Permission permission) {
                        PhotoUtil.loadPhotosFromExternal(App.getContext(), (folders, allPhotos) -> runOnUiThread(() -> {
                            if (!CommonUtil.isEmptyList(folders)) {
                                mPhotoAdapter.setNewPhotos(allPhotos);
                                mFolderAdapter.setNewFolders(folders);
                            }
                        }));
                    }

                    @Override
                    public void onDenied(Permission permission) {
                        ToastUtils.showToast(App.getContext(), getString(R.string.toast_permission_rejected));
                    }
                });

        mPhotoAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (CommonUtil.isEmptyList(mPhotos)) return;
            int selectCount = mPhotoAdapter.getSelectPhotoCount();
            Photo photo = mPhotos.get(position);
            boolean isSelected = !photo.isSelect;
            if (selectCount < Constant.MAX_SELECTED_PHOTO) {
                mPhotoAdapter.updatePhotoByPos(isSelected, position, photo);
            } else if (!isSelected) {
                mPhotoAdapter.updatePhotoByPos(false, position, photo);
            } else {
                ToastUtils.showToast(App.getContext(), "最多只能发送" + Constant.MAX_SELECTED_PHOTO + "张图片");
            }
            //更新之后的选择数量
            selectCount = mPhotoAdapter.getSelectPhotoCount();
            btnSend.setText("发送(" + selectCount + "/" + Constant.MAX_SELECTED_PHOTO + ")");
            if (isSelected) {
                btnSend.setEnabled(true);
            } else if (selectCount == 0) {
                btnSend.setEnabled(false);
                btnSend.setText("发送");
            }
        });

        mFolderAdapter.setOnItemClickListener((adapter, view, position) -> {
            int prePos = mFolderAdapter.getPrePosition();
            if (prePos == position) return;
            mFolderAdapter.updateFolderByPos(!mFolders.get(position).isSelect, position);
            mPhotoAdapter.setNewPhotos(mFolders.get(position).photos);
        });

        rvPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE) hidePhotoTime();
                LogUtil.d("rain", "onScrollStateChanged, state = " + newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LogUtil.d("rain", "onScrolled, state = " + recyclerView.getScrollState() + ", dy = " + dy + ", scrollY = " + recyclerView.getScrollY());
                changePhotoTime(recyclerView.getScrollState());
            }
        });
    }

    @OnClick({R.id.tv_photos})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.tv_photos:
                if (mShowFoldersDialog.isShowing()) {
                    mShowFoldersDialog.dismiss();
                } else {
                    mShowFoldersDialog.show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if(mHideTimeAnimator != null){
            mHideTimeAnimator.cancel();
            mHideTimeAnimator.removeAllListeners();
        }
       if(mShowTimeAnimator != null){
           mShowTimeAnimator.cancel();
       }
        super.onDestroy();
    }

    /**
     * 显示时间条
     */
    private void showPhotoTime(){
        if(mShowTimeAnimator == null){
            mShowTimeAnimator = ObjectAnimator.ofFloat(tvPhotoData, "alpha", 0, 1f);
            mShowTimeAnimator.setDuration(500);
            mShowTimeAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        }else {
            mShowTimeAnimator.setFloatValues(0, 1f);
        }
        if(mShowTimeAnimator.isRunning()) return;
        if(mHideTimeAnimator != null && mHideTimeAnimator.isRunning()) mHideTimeAnimator.cancel();
        if(tvPhotoData.getVisibility() == View.INVISIBLE){
            tvPhotoData.setVisibility(View.VISIBLE);
            mShowTimeAnimator.start();

        }
    }

    /**
     * 隐藏时间条
     */
    private void hidePhotoTime(){
        if(mHideTimeAnimator == null){
            mHideTimeAnimator = ObjectAnimator.ofFloat(tvPhotoData, "alpha", 1f, 0f);
            mHideTimeAnimator.setDuration(500);
            mHideTimeAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        }else {
            mHideTimeAnimator.setFloatValues(1f, 0);
        }
        if(mHideTimeAnimator.isRunning()) return;
        if(mShowTimeAnimator != null && mShowTimeAnimator.isRunning()) mShowTimeAnimator.cancel();
        if(tvPhotoData.getVisibility() == View.VISIBLE){
            mHideTimeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tvPhotoData.setVisibility(View.INVISIBLE);
                }
            });
            mHideTimeAnimator.start();
        }
    }

    /**
     * 更新时间条时间
     */
    private void changePhotoTime(int scrollState) {
        if(scrollState == RecyclerView.SCROLL_STATE_IDLE){
            hidePhotoTime();
        }else {
            showPhotoTime();
            int firstVisibleItem = mPhotoLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem != RecyclerView.NO_POSITION) {
                Photo photo = mPhotos.get(firstVisibleItem);
                tvPhotoData.setText(TimeUtil.getTime(photo.time * 1000));
            }
        }
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, PhotoActivity.class));
    }


}
