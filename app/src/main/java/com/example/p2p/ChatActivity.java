package com.example.p2p;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.p2p.base.BaseActivity;
import com.example.p2p.widget.SendButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.rv_chat)
    RecyclerView rvChat;
    @BindView(R.id.srl_chat)
    SwipeRefreshLayout srlChat;
    @BindView(R.id.iv_audio)
    ImageView ivAudio;
    @BindView(R.id.ed_edit)
    EditText edEdit;
    @BindView(R.id.rl_edit)
    RelativeLayout rlEdit;
    @BindView(R.id.iv_emoji)
    ImageView ivEmoji;
    @BindView(R.id.iv_add)
    ImageView ivAdd;
    @BindView(R.id.iv_album)
    ImageView ivAlbum;
    @BindView(R.id.rl_album)
    RelativeLayout rlAlbum;
    @BindView(R.id.iv_camera)
    ImageView ivCamera;
    @BindView(R.id.rl_camera)
    RelativeLayout rlCamera;
    @BindView(R.id.iv_location)
    ImageView ivLocation;
    @BindView(R.id.rl_location)
    RelativeLayout rlLocation;
    @BindView(R.id.iv_file)
    ImageView ivFile;
    @BindView(R.id.rl_file)
    RelativeLayout rlFile;
    @BindView(R.id.btn_send)
    SendButton btnSend;

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initView() {

        tvTitle.setText(getString(R.string.chat_tlTitle));

        edEdit.addTextChangedListener(this);
        btnSend.setVisibility(View.GONE);
    }

    @Override
    protected void loadData() {

    }

    @OnClick()
    public void onViewClick(View view) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.d(TAG, "beforeTextChanged(): " + "s: " + s.toString() + " start: " + start + " count: " + count + "afer: " + after);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d(TAG, "onTextChanged(): " + "s: " + s.toString() + " start: " + start + " count: " + count);

    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d(TAG, "afterTextChanged(): " + "s: " + s.toString());
        if(!"".equals(s.toString().trim()))
            btnSend.setVisibility(View.VISIBLE);
        else
            btnSend.setVisibility(View.GONE);
    }

}
