package com.example.p2p;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.p2p.app.App;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IDialogCallback;
import com.example.p2p.config.Constant;
import com.example.p2p.utils.CommonUtil;
import com.example.p2p.utils.FileUtil;
import com.example.p2p.utils.IpUtil;
import com.example.p2p.utils.WifiUtil;
import com.example.p2p.widget.customView.ScrollEditText;
import com.example.p2p.widget.dialog.GotoWifiSettingsDialog;
import com.example.permission.PermissionHelper;
import com.example.permission.bean.Permission;
import com.example.permission.callback.IPermissionCallback;
import com.example.utils.FileUtils;
import com.example.utils.ImageUtils;
import com.example.utils.LogUtils;
import com.example.utils.ToastUtils;
import com.example.utils.listener.TextWatchListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.ed_input)
    ScrollEditText edInput;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.iv_icon)
    ImageView ivIcon;

    private static final int REQUEST_WIFI_CODE = 0X000;
    private Bitmap mUserBitmap;
    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        CommonUtil.darkMode(this, true);

        User restoreUser = (User) FileUtils.restoreObject(LoginActivity.this, Constant.FILE_NAME_USER);
        if (restoreUser != null) {
            mUserBitmap = BitmapFactory.decodeFile(restoreUser.getImagePath());
            mImagePath = restoreUser.getImagePath();
            ivIcon.setImageBitmap(mUserBitmap);
            edInput.setText(restoreUser.getName());
            btnLogin.setEnabled(true);
        }

        GotoWifiSettingsDialog gotoWifiSettingsDialog = new GotoWifiSettingsDialog();
        gotoWifiSettingsDialog.setDialogCallback(new IDialogCallback() {
            @Override
            public void onAgree() {
                gotoWifiSettingsDialog.dismiss();
                WifiUtil.gotoWifiSettings(LoginActivity.this, REQUEST_WIFI_CODE);
            }

            @Override
            public void onDismiss() {
                gotoWifiSettingsDialog.dismiss();
                ToastUtils.showToast(App.getContext(), getString(R.string.toast_wifi_noconnect));
            }
        });

        ivIcon.setOnClickListener(v -> CropImage.startPickImageActivity(this));

        edInput.addTextChangedListener(new TextWatchListener() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    btnLogin.setEnabled(false);
                } else {
                    btnLogin.setEnabled(true);
                }
            }
        });

        btnLogin.setOnClickListener(v -> {
            if (!WifiUtil.isWifiConnected(LoginActivity.this)) {
                gotoWifiSettingsDialog.show(getSupportFragmentManager());
                return;
            }
            goMainActivity(saveUserMessage());
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WIFI_CODE) {
            if (WifiUtil.isWifiConnected(LoginActivity.this)) {
                goMainActivity(saveUserMessage());
            } else {
                ToastUtils.showToast(App.getContext(), getString(R.string.toast_wifi_noconnect));
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
                Uri imageUri = CropImage.getPickImageResultUri(this, data);
                PermissionHelper.getInstance().with(this).requestPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        new IPermissionCallback() {
                            @Override
                            public void onAccepted(Permission permission) {
                                CropImage.activity(imageUri)
                                        .start(LoginActivity.this);
                            }

                            @Override
                            public void onDenied(Permission permission) {
                                ToastUtils.showToast(App.getContext(), getString(R.string.toast_permission_rejected));
                            }
                        }
                );
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                try (InputStream in = this.getContentResolver().openInputStream(resultUri)) {
                    mUserBitmap = ImageUtils.compressBitmap(BitmapFactory.decodeStream(in), 0.3f, 0.3f);
                    mImagePath = FileUtil.saveUserBitmap(mUserBitmap);
                    ivIcon.setImageBitmap(mUserBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "获取图片失败， e = " + e.getMessage());
                    ToastUtils.showToast(App.getContext(), getString(R.string.toast_open_image_fail));
                }
            }
        }
    }

    private User saveUserMessage() {
        String locIp = IpUtil.getLocIpAddress();
        String name = edInput.getText().toString().trim();
        if(mUserBitmap == null){
            mUserBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_user);
            mUserBitmap = ImageUtils.compressBitmap(mUserBitmap, 0.3f, 0.3f);
            mImagePath = FileUtil.saveUserBitmap(mUserBitmap);
        }
        User user = new User(name, locIp, mImagePath);
        FileUtils.saveObject(LoginActivity.this, Constant.FILE_NAME_USER, user);
        return user;
    }

    private void goMainActivity(User restoreUser) {
        MainActivity.startActivity(this);
        finish();
    }
}
