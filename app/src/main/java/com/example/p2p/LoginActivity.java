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

import com.example.p2p.bean.User;
import com.example.p2p.callback.IDialogCallback;
import com.example.p2p.config.Constant;
import com.example.p2p.core.OnlineUserManager;
import com.example.p2p.utils.CommonUtils;
import com.example.p2p.utils.FileUtils;
import com.example.p2p.utils.ImageUtils;
import com.example.p2p.utils.IpUtils;
import com.example.p2p.utils.LogUtils;
import com.example.p2p.utils.SimpleTextWatchListener;
import com.example.p2p.utils.WifiUtils;
import com.example.p2p.widget.customView.ScrollEditText;
import com.example.p2p.widget.dialog.GotoWifiSettingsDialog;
import com.example.permission.PermissionHelper;
import com.example.permission.bean.Permission;
import com.example.permission.callback.IPermissionCallback;
import com.example.utils.FileUtil;
import com.example.utils.ToastUtil;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        CommonUtils.darkMode(this, true);

        User restoreUser = (User) FileUtil.restoreObject(LoginActivity.this, Constant.FILE_NAME_USER);
        if (restoreUser != null) {
            goMainActivity(restoreUser);
            return;
        }

        GotoWifiSettingsDialog gotoWifiSettingsDialog = new GotoWifiSettingsDialog();
        gotoWifiSettingsDialog.setDialogCallback(new IDialogCallback() {
            @Override
            public void onAgree() {
                gotoWifiSettingsDialog.dismiss();
                WifiUtils.gotoWifiSettings(LoginActivity.this, REQUEST_WIFI_CODE);
            }

            @Override
            public void onDismiss() {
                gotoWifiSettingsDialog.dismiss();
                ToastUtil.showToast(LoginActivity.this, getString(R.string.toast_wifi_noconnect));
            }
        });

        ivIcon.setOnClickListener(v -> CropImage.startPickImageActivity(this));

        edInput.addTextChangedListener(new SimpleTextWatchListener() {
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
            if (!WifiUtils.isWifiConnected(LoginActivity.this)) {
                gotoWifiSettingsDialog.show(getSupportFragmentManager());
                return;
            }
            goMainActivity(saveUserMessage());
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_WIFI_CODE) {
            if (WifiUtils.isWifiConnected(LoginActivity.this)) {
                goMainActivity(saveUserMessage());
            } else {
                ToastUtil.showToast(LoginActivity.this, getString(R.string.toast_wifi_noconnect));
            }
        }
        if(resultCode == Activity.RESULT_OK){
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
                                ToastUtil.showToast(LoginActivity.this, getString(R.string.toast_permission_rejected));
                            }
                        }
                );
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                try (InputStream in = this.getContentResolver().openInputStream(resultUri)){
                   mUserBitmap = ImageUtils.compressBitmap(BitmapFactory.decodeStream(in), 0.5f,  0.5f);
                   FileUtils.saveUserBitmap(mUserBitmap);
                   ivIcon.setImageBitmap(mUserBitmap);
                }catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "获取图片失败， e = " + e.getMessage());
                    ToastUtil.showToast(this, getString(R.string.toast_open_image_fail));
                }
            }
        }
    }

    private User saveUserMessage() {
        String locIp = IpUtils.getLocIpAddress();
        String name = edInput.getText().toString().trim();
        if(mUserBitmap == null){
            mUserBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_user_image);
            FileUtils.saveUserBitmap(mUserBitmap);
        }
        User user = new User(name, locIp, Constant.FILE_USER_IMAGE);
        FileUtil.saveObject(LoginActivity.this, Constant.FILE_NAME_USER, user);
        return user;
    }

    private void goMainActivity(User restoreUser) {
        byte[] imageBytes = FileUtils.getImageBytes(restoreUser.getImagePath());
        restoreUser.setImageBytesLen(imageBytes.length);
        OnlineUserManager.getInstance().login(restoreUser, imageBytes);
        MainActivity.startActivity(this);
        finish();
    }
}
