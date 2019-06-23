package com.example.p2p;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.p2p.adapter.RvChatAdapter;
import com.example.p2p.adapter.RvEmojiAdapter;
import com.example.p2p.adapter.VpEmojiAdapter;
import com.example.p2p.base.BaseActivity;
import com.example.p2p.bean.Audio;
import com.example.p2p.bean.Emoji;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.MesType;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IReceiveMessageCallback;
import com.example.p2p.callback.IRecordedCallback;
import com.example.p2p.callback.ISendMessgeCallback;
import com.example.p2p.config.Constant;
import com.example.p2p.core.ConnectManager;
import com.example.p2p.core.MediaPlayerManager;
import com.example.p2p.db.EmojiDao;
import com.example.p2p.utils.FileUtils;
import com.example.p2p.utils.LogUtils;
import com.example.p2p.utils.SimpleTextWatchListener;
import com.example.p2p.widget.customView.AudioTextView;
import com.example.p2p.widget.customView.IndicatorView;
import com.example.p2p.widget.customView.SendButton;
import com.example.p2p.widget.customView.WrapViewPager;
import com.example.permission.PermissionHelper;
import com.example.permission.bean.Permission;
import com.example.permission.callback.IPermissionsCallback;
import com.example.utils.DisplayUtil;
import com.example.utils.FileUtil;
import com.example.utils.KeyBoardUtil;
import com.example.utils.ToastUtil;

import java.io.File;
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
    @BindView(R.id.iv_scan)
    ImageView ivScan;
    @BindView(R.id.iv_keyborad)
    ImageView ivKeyborad;
    @BindView(R.id.tv_audio)
    AudioTextView tvAudio;

    private final String TAG = this.getClass().getSimpleName();
    private ViewGroup mContentView;
    private boolean isKeyboardShowing;
    private int screenHeight;
    private List<RvEmojiAdapter> mEmojiAdapters;
    private List<Emoji> mEmojiBeans;
    private User mTargetUser;
    private User mUser;
    private RvChatAdapter mRvChatAdapter;
    private List<Mes> mMessageList;
    private int mLastPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mTargetUser = (User) getIntent().getSerializableExtra(Constant.EXTRA_TARGET_USER);
        mUser = (User) FileUtil.restoreObject(this, Constant.FILE_NAME_USER);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (edEdit.hasFocus()) edEdit.clearFocus();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        KeyBoardUtil.closeKeyBoard(this, edEdit);
        if (clMore.isShown()) clMore.setVisibility(View.GONE);
        if (llEmoji.isShown()) llEmoji.setVisibility(View.GONE);
        super.onBackPressed();
    }
    
    @Override
    protected void onDestroy() {
        mMessageList.clear();
        FileUtil.deleteDir(new File(FileUtils.getAudioPath(mTargetUser.getIp(), Constant.TYPE_ITEM_RECEIVE_AUDIO)));
        FileUtil.deleteDir(new File(FileUtils.getAudioPath(mUser.getIp(), Constant.TYPE_ITEM_SEND_AUDIO)));
        super.onDestroy();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initView() {
        tvTitle.setText(mTargetUser.getName());
        ivScan.setVisibility(View.GONE);
        PermissionHelper.getInstance().with(this).requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new IPermissionsCallback() {
                    @Override
                    public void onAccepted(List<Permission> permissions) {

                    }

                    @Override
                    public void onDenied(List<Permission> permissions) {
                        ToastUtil.showToast(ChatActivity.this, getString(R.string.toast_permission_rejected));
                        finish();
                    }
                }
        );
        screenHeight = DisplayUtil.getScreenHeight(ChatActivity.this);
        mContentView = getWindow().getDecorView().findViewById(android.R.id.content);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            //当前窗口可见区域的大小
            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            //与当前可视区域的差值(软键盘的高度)
            int heightDiff = screenHeight - (rect.bottom - rect.top);
            isKeyboardShowing = heightDiff > screenHeight / 3;
        });

        //从数据库获取表情
        mEmojiBeans = EmojiDao.getInstance().getEmojiBeanList();
        //添加删除表情按钮信息
        Emoji emojiDelete = new Emoji(0, 000);
        int emojiDeleteCount = (int) Math.ceil(mEmojiBeans.size() * 1.0 / 21);
        for (int i = 1; i <= emojiDeleteCount; i++) {
            if (i == emojiDeleteCount) {
                mEmojiBeans.add(mEmojiBeans.size(), emojiDelete);
            } else {
                mEmojiBeans.add(i * 21 - 1, emojiDelete);
            }
        }
        //为每个Vp添加Rv，并初始化Rv
        List<View> views = new ArrayList<>();
        mEmojiAdapters = new ArrayList<>(emojiDeleteCount);
        for (int i = 0; i < emojiDeleteCount; i++) {
            RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(this).inflate(R.layout.item_emoji_vp, vpEmoji, false);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 7));
            if (i == emojiDeleteCount - 1) {
                mEmojiAdapters.add(new RvEmojiAdapter(mEmojiBeans.subList(i * 21, mEmojiBeans.size()), R.layout.item_emoji));
            } else {
                mEmojiAdapters.add(new RvEmojiAdapter(mEmojiBeans.subList(i * 21, i * 21 + 21), R.layout.item_emoji));
            }
            recyclerView.setAdapter(mEmojiAdapters.get(i));
            views.add(recyclerView);
            int index = i;
            //为每个Rv添加item监听
            mEmojiAdapters.get(i).setOnItemClickListener((adapter, view, position) -> {
                Emoji emojiBean = mEmojiBeans.get(position + index * 21);
                if (emojiBean.getId() == 0) {
                    edEdit.setText("");
                } else {
                    edEdit.setText(edEdit.getText().append(emojiBean.getUnicodeInt()));
                }
                edEdit.setSelection(edEdit.length());
            });

        }
        //初始化Vp
        VpEmojiAdapter vpEmojiAdapter = new VpEmojiAdapter(views);
        vpEmoji.setAdapter(vpEmojiAdapter);
        idvEmoji.setIndicatorCount(views.size());
        //初始化聊天的Rv
        mMessageList = new ArrayList<>();
        mRvChatAdapter = new RvChatAdapter(mMessageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(mRvChatAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initCallback() {
        //下拉刷新监听
        srlChat.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> {
                srlChat.setRefreshing(false);
            }, 2000);
        });
        //editText文本变化监听
        edEdit.addTextChangedListener(new SimpleTextWatchListener() {
            @Override
            public void afterTextChanged(Editable s) {
                int visibility = "".equals(s.toString().trim()) ? View.GONE : View.VISIBLE;
                btnSend.setVisibility(visibility);
            }
        });
        //editText触摸监听
        edEdit.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && isButtomLayoutShown()) {
                if (clMore.isShown()) clMore.setVisibility(View.GONE);
                if (llEmoji.isShown()) llEmoji.setVisibility(View.GONE);
            }
            return false;
        });
        //表情列表左右滑动监听
        vpEmoji.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                idvEmoji.setCurrentIndicator(position);
            }
        });
        //聊天列表触摸监听
        rvChat.setOnTouchListener((view, event) -> {
            edEdit.clearFocus();
            KeyBoardUtil.closeKeyBoard(ChatActivity.this, edEdit);
            if (clMore.isShown()) clMore.setVisibility(View.GONE);
            if (llEmoji.isShown()) llEmoji.setVisibility(View.GONE);
            return false;
        });
        //底部布局弹出,聊天列表上滑
        rvChat.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if(bottom < oldBottom){
                if(mMessageList.isEmpty()) return;
                rvChat.post(() -> rvChat.smoothScrollToPosition(mMessageList.size() - 1));
            }
        });
        //接收消息回调监听
        ConnectManager.getInstance().addReceiveMessageCallback(mTargetUser.getIp(), new IReceiveMessageCallback() {

            @Override
            public void onReceiveSuccess(Mes<?> message) {
                message.userIp = mTargetUser.getIp();
                switch (message.mesType){
                    case TEXT:
                        message.id = Constant.TYPE_ITEM_RECEIVE_TEXT;
                        break;
                    case AUDIO:
                        message.id = Constant.TYPE_ITEM_RECEIVE_AUDIO;
                        break;
                    default:
                        LogUtils.d(TAG, "接受消息失败， message = " + message);
                        break;
                }
                mMessageList.add(message);
                mRvChatAdapter.notifyItemInserted(mMessageList.size());
                rvChat.smoothScrollToPosition(mMessageList.size() - 1);
            }

            @Override
            public void onReceiveFail(Mes<?> message) {
                LogUtils.d(TAG, "接受消息失败， message = " + message);
            }
        });
        //发送消息回调监听
        ConnectManager.getInstance().setSendMessgeCallback(new ISendMessgeCallback() {
            @Override
            public void onSendSuccess(Mes<?> message) {
                message.userIp = mUser.getIp();
                switch (message.mesType){
                    case TEXT:
                        message.id = Constant.TYPE_ITEM_SEND_TEXT;
                        break;
                    case AUDIO:
                        message.id = Constant.TYPE_ITEM_SEND_AUDIO;
                        break;
                    default:
                        break;
                }
                mMessageList.add(message);
                mRvChatAdapter.notifyItemInserted(mMessageList.size());
                rvChat.smoothScrollToPosition(mMessageList.size() - 1);
            }

            @Override
            public void onSendFail(Mes<?> message) {
                ToastUtil.showToast(ChatActivity.this, "发送消息失败");
            }
        });
        //录音结束回调
        tvAudio.setRecordedCallback(mTargetUser, new IRecordedCallback() {
            @Override
            public void onFinish(String audioPath, int duration) {
                sendAudio(audioPath, duration);
            }

            @Override
            public void onError() {
                ToastUtil.showToast(ChatActivity.this, getString(R.string.chat_audio_error));
            }
        });
        //聊天列表的item点击回调
        mRvChatAdapter.setOnItemClickListener((adapter, view, position) -> {
            Mes message = mMessageList.get(position);
            if(message.mesType == MesType.AUDIO){
                Audio audio = (Audio) message.data;
                ImageView imageView = view.findViewById(R.id.iv_message);
                Drawable drawable = imageView.getBackground();
                int audioBg = message.id == Constant.TYPE_ITEM_SEND_AUDIO ? R.drawable.ic_audio_right_3 : R.drawable.ic_audio_left_3;
                int audioBgAnim =  message.id == Constant.TYPE_ITEM_SEND_AUDIO ? R.drawable.anim_item_audio_right : R.drawable.anim_item_audio_left;
                if(drawable instanceof AnimationDrawable){
                    MediaPlayerManager.getInstance().stopPlayAudio();
                    imageView.setBackgroundResource(audioBg);
                }else {
                    if(mLastPosition != -1 && position != mLastPosition){
                        Mes lastMessage = mMessageList.get(mLastPosition);
                        int lastAudioBg = lastMessage.id == Constant.TYPE_ITEM_SEND_AUDIO ? R.drawable.ic_audio_right_3 : R.drawable.ic_audio_left_3;
                        LinearLayoutManager manager = (LinearLayoutManager) rvChat.getLayoutManager();
                        View lastView = manager.findViewByPosition(mLastPosition);
                        if(lastView != null){
                            lastView.findViewById(R.id.iv_message).setBackgroundResource(lastAudioBg);
                        }
                    }
                    imageView.setBackgroundResource(audioBgAnim);
                    AnimationDrawable audioAnimDrawable = (AnimationDrawable)imageView.getBackground();
                    audioAnimDrawable.start();
                    MediaPlayerManager.getInstance().startPlayAudio(audio.path, mp -> imageView.setBackgroundResource(audioBg));
                }
            }
            mLastPosition = position;
        });
    }

    @OnClick({R.id.iv_add, R.id.iv_back, R.id.iv_emoji, R.id.btn_send, R.id.iv_audio, R.id.iv_keyborad})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_audio:
                changeAudioLayout();
                break;
            case R.id.iv_keyborad:
                changeEditLayout();
                break;
            case R.id.iv_add:
                changeMoreLayout();
                break;
            case R.id.iv_emoji:
                changeEmojiLayout();
                break;
            case R.id.btn_send:
                sendText();
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 发送文字消息
     */
    private void sendText() {
        String text = edEdit.getText().toString();
        Mes<String> message = new Mes<>(MesType.TEXT, text);
        ConnectManager.getInstance().sendMessage(mTargetUser.getIp(), message);
        edEdit.setText("");
    }

    /**
     * 发送音频消息
     */
    private void sendAudio(String audioPath, int duration) {
        Audio audio = new Audio(duration, audioPath);
        Mes<Audio> message = new Mes<Audio>(MesType.AUDIO, audio);
        ConnectManager.getInstance().sendMessage(mTargetUser.getIp(), message);
    }

    /**
     * 改变输入键盘显示
     */
    private void changeEditLayout() {
        ivKeyborad.setVisibility(View.INVISIBLE);
        ivAudio.setVisibility(View.VISIBLE);
        if(clMore.isShown()) clMore.setVisibility(View.GONE);
        if(llEmoji.isShown()) llEmoji.setVisibility(View.GONE);
        if(!isKeyboardShowing) KeyBoardUtil.openKeyBoard(this, edEdit);
        edEdit.setVisibility(View.VISIBLE);
        tvAudio.setVisibility(View.INVISIBLE);
        edEdit.requestFocus();
    }

    /**
     * 改变音频布局显示
     */
    private void changeAudioLayout() {
        edEdit.clearFocus();
        ivAudio.setVisibility(View.INVISIBLE);
        ivKeyborad.setVisibility(View.VISIBLE);
        if(clMore.isShown()) clMore.setVisibility(View.GONE);
        if(llEmoji.isShown()) llEmoji.setVisibility(View.GONE);
        if(isKeyboardShowing) KeyBoardUtil.closeKeyBoard(this, edEdit);
        edEdit.setVisibility(View.INVISIBLE);
        tvAudio.setVisibility(View.VISIBLE);
    }


    /**
     * 改变更多布局显示
     */
    private void changeMoreLayout() {
        edEdit.clearFocus();
        int visibility = clMore.isShown() ? View.GONE : View.VISIBLE;
        if (!clMore.isShown() && !isKeyboardShowing) {//如果键盘没有显示，且更多布局也没有显示，只显示更多布局
            clMore.setVisibility(visibility);
            if (llEmoji.isShown()) llEmoji.setVisibility(View.GONE);
        } else if (clMore.isShown() && !isKeyboardShowing) {//如果键盘没有显示，但更多布局显示，隐藏更多布局，显示键盘
            clMore.setVisibility(visibility);
            KeyBoardUtil.openKeyBoard(this, edEdit);
        } else if (!clMore.isShown() && isKeyboardShowing) {//如果只有键盘显示，就隐藏键盘，显示更多布局
            lockContentHeight();
            KeyBoardUtil.closeKeyBoard(this, edEdit);
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
        int visibility = llEmoji.isShown() ? View.GONE : View.VISIBLE;
        if (!llEmoji.isShown() && !isKeyboardShowing) {
            llEmoji.setVisibility(visibility);
            if (clMore.isShown()) clMore.setVisibility(View.GONE);
        } else if (llEmoji.isShown() && !isKeyboardShowing) {
            llEmoji.setVisibility(visibility);
            KeyBoardUtil.openKeyBoard(this, edEdit);
        } else if (!llEmoji.isShown() && isKeyboardShowing) {
            lockContentHeight();
            KeyBoardUtil.closeKeyBoard(this, edEdit);
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

    public static void startActiivty(Activity context, User user, int code) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constant.EXTRA_TARGET_USER, user);
        context.startActivityForResult(intent, code);
    }
}
