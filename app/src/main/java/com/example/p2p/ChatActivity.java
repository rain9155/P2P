package com.example.p2p;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.p2p.base.BaseActivity;
import com.example.p2p.bean.EmojiBean;
import com.example.p2p.db.EmojiDao;
import com.example.p2p.utils.CommonUtils;
import com.example.p2p.utils.SimpleTextWatchListener;
import com.example.p2p.widget.IndicatorView;
import com.example.p2p.widget.SendButton;
import com.example.p2p.widget.WrapViewPager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends BaseActivity {

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
    @BindView(R.id.rl_album)
    RelativeLayout rlAlbum;
    @BindView(R.id.rl_camera)
    RelativeLayout rlCamera;
    @BindView(R.id.rl_location)
    RelativeLayout rlLocation;
    @BindView(R.id.rl_file)
    RelativeLayout rlFile;
    @BindView(R.id.btn_send)
    SendButton btnSend;
    @BindView(R.id.cl_more)
    ConstraintLayout clMore;
    @BindView(R.id.vp_emoji)
    WrapViewPager vpEmoji;
    @BindView(R.id.idv_emoji)
    IndicatorView idvEmoji;

    private final String TAG = this.getClass().getSimpleName();

    private ViewGroup mContentView;
    private boolean isKeyboardShowing;
    private int screenHeight;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initView() {

        tvTitle.setText(getString(R.string.chat_tlTitle));

        screenHeight = CommonUtils.getScreenHeight(ChatActivity.this);
        mContentView = getWindow().getDecorView().findViewById(android.R.id.content);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            //当前窗口可见区域的大小
            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            //与当前可视区域的差值(软键盘的高度)
            int heightDiff = screenHeight - (rect.bottom - rect.top);
            isKeyboardShowing = heightDiff > screenHeight / 3;
        });

        edEdit.addTextChangedListener(new SimpleTextWatchListener() {
            @Override
            public void afterTextChanged(Editable s) {
                int visibility = "".equals(s.toString().trim()) ? View.GONE : View.VISIBLE;
                btnSend.setVisibility(visibility);
            }
        });
        edEdit.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && clMore.isShown()) {
                clMore.setVisibility(View.GONE);
            }
            return false;
        });
        rvChat.setOnTouchListener((view, event) -> {
            edEdit.clearFocus();
            CommonUtils.hideSoftInput(ChatActivity.this, edEdit);
            clMore.setVisibility(View.GONE);
            return false;
        });

        List<EmojiBean> list = EmojiDao.getInstance().getEmojiBeanList();

    }

    @Override
    protected void loadData() {

    }

    @OnClick({R.id.iv_add, R.id.iv_back})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_add:
                edEdit.clearFocus();
                int visibility = clMore.isShown() ? View.GONE : View.VISIBLE;
                if (!clMore.isShown() && !isKeyboardShowing) {//如果键盘没有显示，且更多布局也没有显示，只显示更多布局
                    clMore.setVisibility(visibility);
                } else if (clMore.isShown() && !isKeyboardShowing) {//如果键盘没有显示，但更多布局显示，隐藏更多布局，显示键盘
                    clMore.setVisibility(visibility);
                    CommonUtils.showoftInput(this, edEdit);
                } else if (!clMore.isShown() && isKeyboardShowing) {//如果只有键盘显示，就隐藏键盘，显示更多布局
                    lockContentHeight();
                    CommonUtils.hideSoftInput(this, edEdit);
                    edEdit.postDelayed(() -> {
                        unlockContentHeightDelayed();
                        clMore.setVisibility(visibility);

                    }, 200);
                }
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 锁定内容高度，防止跳闪
     */
    private void lockContentHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
        params.height = mContentView.getHeight();
        params.weight = 0.0F;
    }

    /**
     * 释放被锁定的内容高度
     */
    public void unlockContentHeightDelayed() {
        ((LinearLayout.LayoutParams) mContentView.getLayoutParams()).weight = 1.0F;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
