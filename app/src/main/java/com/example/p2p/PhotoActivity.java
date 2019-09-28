package com.example.p2p;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p2p.adapter.RvPhotoAdapter;
import com.example.p2p.app.App;
import com.example.p2p.base.activity.BaseActivity;
import com.example.p2p.bean.Folder;
import com.example.p2p.bean.Photo;
import com.example.p2p.utils.PhotoUtil;
import com.example.permission.PermissionHelper;
import com.example.permission.bean.Permission;
import com.example.permission.callback.IPermissionCallback;
import com.example.utils.CommonUtil;
import com.example.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

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

    private RvPhotoAdapter mPhotoAdapter;
    private List<Photo> mCurPhotos;
    private List<Folder> mFolders;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_photo;
    }

    @Override
    protected void initView() {
        ivBack.setOnClickListener(v -> finish());
        btnSend.setEnabled(false);

        mCurPhotos = new ArrayList<>();
        mFolders = new ArrayList<>();
        mPhotoAdapter = new RvPhotoAdapter(mCurPhotos, R.layout.item_photo_photos);

        rvPhotos.setAdapter(mPhotoAdapter);
        rvPhotos.setLayoutManager(new GridLayoutManager(this, 4));

    }

    @Override
    protected void initCallback() {
        PermissionHelper.getInstance().with(this).requestPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                new IPermissionCallback() {
            @Override
            public void onAccepted(Permission permission) {
                PhotoUtil.loadPhotosFromExternal(App.getContext(), (folders, allPhotos) -> runOnUiThread(() -> {
                    if(!CommonUtil.isEmptyList(folders)){
                        mCurPhotos.clear();
                        mFolders.clear();
                        mCurPhotos.addAll(allPhotos);
                        mFolders.addAll(folders);
                        mPhotoAdapter.notifyDataSetChanged();
                    }
                }));
            }

            @Override
            public void onDenied(Permission permission) {
                ToastUtils.showToast(App.getContext(), getString(R.string.toast_permission_rejected));
            }
        });
    }


    public static void startActivity(Context context){
        context.startActivity(new Intent(context, PhotoActivity.class));
    }

}
