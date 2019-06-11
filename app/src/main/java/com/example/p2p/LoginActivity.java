package com.example.p2p;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.p2p.utils.CommonUtils;
import com.example.p2p.utils.SimpleTextWatchListener;
import com.example.p2p.widget.customView.ScrollEditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity{

    @BindView(R.id.ed_input)
    ScrollEditText edInput;
    @BindView(R.id.btn_login)
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        CommonUtils.darkMode(this, true);
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
        btnLogin.setOnClickListener(() -> {

        });
    }
}
