package com.example.p2p;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.text.Editable;
import android.view.LayoutInflater;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.p2p.adapter.RvEmojiAdapter;
import com.example.p2p.adapter.VpEmojiAdapter;
import com.example.p2p.base.BaseActivity;
import com.example.p2p.bean.EmojiBean;
import com.example.p2p.db.EmojiDao;
import com.example.p2p.utils.CommonUtils;
import com.example.p2p.utils.SimpleTextWatchListener;
import com.example.p2p.widget.IndicatorView;
import com.example.p2p.widget.SendButton;
import com.example.p2p.widget.WrapViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
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
    @BindView(R.id.ll_emoji)
    LinearLayout llEmoji;

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
            if (event.getAction() == MotionEvent.ACTION_UP && isButtomLayoutShown()) {
                if(clMore.isShown()) clMore.setVisibility(View.GONE);
                if (llEmoji.isShown())llEmoji.setVisibility(View.GONE);
            }
            return false;
        });
        rvChat.setOnTouchListener((view, event) -> {
            edEdit.clearFocus();
            CommonUtils.hideSoftInput(ChatActivity.this, edEdit);
            if(clMore.isShown()) clMore.setVisibility(View.GONE);
            if (llEmoji.isShown()) llEmoji.setVisibility(View.GONE);
            return false;
        });

        //获取表情
        List<EmojiBean> list = EmojiDao.getInstance().getEmojiBeanList();
        //添加删除表情按钮信息
        EmojiBean emojiDelete = new EmojiBean(0, 000);
        int emojiDeleteCount = (int) Math.ceil(list.size() * 1.0 / 21);
        for(int i = 1; i <= emojiDeleteCount; i++){
            if (i == emojiDeleteCount) {
                list.add(list.size(), emojiDelete);
            } else {
                list.add(i * 21 - 1, emojiDelete);
            }
        }
        //为每个Vp添加Rv
        List<View> views = new ArrayList<>();
        RvEmojiAdapter emojiAdapter;
        for(int i = 0; i < emojiDeleteCount; i++){
            RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(this).inflate(R.layout.item_emoji_vp, vpEmoji, false);
            //recyclerView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            recyclerView.setLayoutManager(new GridLayoutManager(this, 7));
            if(i == emojiDeleteCount - 1){
                emojiAdapter = new RvEmojiAdapter(list.subList(i * 21, list.size()), R.layout.item_emoji);
            }else {
                emojiAdapter = new RvEmojiAdapter(list.subList(i * 21, i * 21 + 21), R.layout.item_emoji);
            }
            recyclerView.setAdapter(emojiAdapter);
            views.add(recyclerView);
        }
        VpEmojiAdapter vpEmojiAdapter = new VpEmojiAdapter(views);
        vpEmoji.setAdapter(vpEmojiAdapter);
        idvEmoji.setIndicatorCount(views.size());
        vpEmoji.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                idvEmoji.setCurrentIndicator(position);
            }
        });
    }

    @Override
    protected void loadData() {

    }

    @OnClick({R.id.iv_add, R.id.iv_back, R.id.iv_emoji})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_add:
                changeMoreLayout();
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_emoji:
                changeEmojiLayout();
                break;
            default:
                break;
        }
    }

    /**
     * 改变更多布局显示
     */
    private void changeMoreLayout() {
        edEdit.clearFocus();
        int visibility = clMore.isShown() ? View.GONE : View.VISIBLE;
        if (!clMore.isShown() && !isKeyboardShowing) {//如果键盘没有显示，且更多布局也没有显示，只显示更多布局
            clMore.setVisibility(visibility);
            if(llEmoji.isShown()) llEmoji.setVisibility(View.GONE);
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
    }

    /**
     * 改变表情布局显示
     */
    private void changeEmojiLayout() {
        edEdit.clearFocus();
        int visibility = llEmoji.isShown() ? View.GONE : View.VISIBLE;
        if (!llEmoji.isShown() && !isKeyboardShowing) {
            llEmoji.setVisibility(visibility);
            if(clMore.isShown()) clMore.setVisibility(View.GONE);
        } else if (llEmoji.isShown() && !isKeyboardShowing) {
            llEmoji.setVisibility(visibility);
            CommonUtils.showoftInput(this, edEdit);
        } else if (!llEmoji.isShown() && isKeyboardShowing) {
            lockContentHeight();
            CommonUtils.hideSoftInput(this, edEdit);
            edEdit.postDelayed(() -> {
                unlockContentHeightDelayed();
                llEmoji.setVisibility(visibility);

            }, 200);
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
    private void unlockContentHeightDelayed() {
        ((LinearLayout.LayoutParams) mContentView.getLayoutParams()).weight = 1.0F;
    }

    /**
     * 底部表情布局或底部更多布局是否显示
     */
    private boolean isButtomLayoutShown() {
        return clMore.isShown() || llEmoji.isShown();
    }
}
