package com.example.p2p;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.p2p.bean.Data;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IDialogCallback;
import com.example.p2p.config.Constant;
import com.example.p2p.core.BroadcastManager;
import com.example.p2p.utils.CommonUtils;
import com.example.p2p.utils.IpUtils;
import com.example.p2p.utils.SimpleTextWatchListener;
import com.example.p2p.utils.WifiUtils;
import com.example.p2p.widget.customView.ScrollEditText;
import com.example.p2p.widget.dialog.GotoWifiSettingsDialog;
import com.example.utils.FileUtil;
import com.example.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity{

    @BindView(R.id.ed_input)
    ScrollEditText edInput;
    @BindView(R.id.btn_login)
    Button btnLogin;

    private static final int REQUEST_WIFI_CODE = 0X000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        CommonUtils.darkMode(this, true);

        User restoreUser = (User) FileUtil.restoreObject(LoginActivity.this, Constant.FILE_NAME);
        if(restoreUser != null){
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
        edInput.addTextChangedListener(new SimpleTextWatchListener(){
            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().trim().isEmpty()){
                    btnLogin.setEnabled(false);
                }else {
                    btnLogin.setEnabled(true);
                }
            }
        });
        btnLogin.setOnClickListener(v -> {
           if(!WifiUtils.isWifiConnected(LoginActivity.this)){
               gotoWifiSettingsDialog.show(getSupportFragmentManager());
               return;
           }
            goMainActivity(saveUserMessage());
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_WIFI_CODE){
            if(WifiUtils.isWifiConnected(LoginActivity.this)){
                goMainActivity(saveUserMessage());
            }else {
                ToastUtil.showToast(LoginActivity.this, getString(R.string.toast_wifi_noconnect));
            }
        }
    }

    private User saveUserMessage() {
        String locIp = IpUtils.getLocIpAddress();
        String name = edInput.getText().toString().trim();
        User user = new User(name, locIp);
        FileUtil.saveObject(LoginActivity.this, Constant.FILE_NAME, user);
        return user;
    }

    private void goMainActivity(User restoreUser) {
        Data data = new Data(0, restoreUser);
        BroadcastManager.getInstance().broadcast(data);
        MainActivity.startActivity(this);
        finish();
    }
}
