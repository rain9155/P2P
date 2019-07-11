package com.example.p2p;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
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
import com.example.p2p.app.App;
import com.example.p2p.base.activity.BaseActivity;
import com.example.p2p.bean.Audio;
import com.example.p2p.bean.Document;
import com.example.p2p.bean.Emoji;
import com.example.p2p.bean.Image;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.MesType;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IRecordedCallback;
import com.example.p2p.callback.ISendMessgeCallback;
import com.example.p2p.config.Constant;
import com.example.p2p.core.ConnectManager;
import com.example.p2p.core.MediaPlayerManager;
import com.example.p2p.db.EmojiDao;
import com.example.p2p.utils.FileUtil;
import com.example.p2p.utils.ImageUtil;
import com.example.p2p.utils.LogUtil;
import com.example.p2p.widget.customView.AudioTextView;
import com.example.p2p.widget.customView.IndicatorView;
import com.example.p2p.widget.customView.SendButton;
import com.example.p2p.widget.customView.WrapViewPager;
import com.example.p2p.widget.dialog.LocatingDialog;
import com.example.permission.PermissionHelper;
import com.example.permission.bean.Permission;
import com.example.permission.callback.IPermissionCallback;
import com.example.permission.callback.IPermissionsCallback;
import com.example.utils.CommonUtil;
import com.example.utils.DisplayUtil;
import com.example.utils.FileUtils;
import com.example.utils.ImageUtils;
import com.example.utils.IntentUtils;
import com.example.utils.KeyBoardUtils;
import com.example.utils.ToastUtils;
import com.example.utils.listener.TextWatchListener;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

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
    @BindView(R.id.iv_more)
    ImageView ivMore;
    @BindView(R.id.iv_keyborad)
    ImageView ivKeyborad;
    @BindView(R.id.tv_audio)
    AudioTextView tvAudio;

    private final String TAG = this.getClass().getSimpleName();
    private final static int REQUEST_CODE_GET_IMAGE= 0x000;
    private final static int REQUEST_CODE_TAKE_IMAGE = 0x001;
    private final static int REQUEST_CODE_GET_FILE = 0x002;
    private boolean isKeyboardShowing;
    private int screenHeight;
    private int mLastPosition = -1;
    private Uri mTakedImageUri;
    private List<RvEmojiAdapter> mEmojiAdapters;
    private List<Emoji> mEmojiBeans;
    private User mTargetUser;
    private User mUser;
    private RvChatAdapter mRvChatAdapter;
    private List<Mes> mMessageList;
    private ViewGroup mContentView;
    private LocatingDialog mLocatingDialog;
    private boolean isSendingImage, isSendingFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mTargetUser = (User) getIntent().getSerializableExtra(Constant.EXTRA_TARGET_USER);
        mUser = (User) FileUtils.restoreObject(this, Constant.FILE_NAME_USER);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (edEdit.hasFocus()) edEdit.clearFocus();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        KeyBoardUtils.closeKeyBoard(this, edEdit);
        if (clMore.isShown()) clMore.setVisibility(View.GONE);
        if (llEmoji.isShown()) llEmoji.setVisibility(View.GONE);
        super.onBackPressed();
    }
    
    @Override
    protected void onDestroy() {
        ConnectManager.getInstance().release();
        mMessageList.clear();
        if(mLocatingDialog != null) mLocatingDialog = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode != Activity.RESULT_OK) return;
        if(requestCode == REQUEST_CODE_GET_IMAGE) sendImage(data.getData());
        if(requestCode == REQUEST_CODE_TAKE_IMAGE) sendImage(mTakedImageUri);
        if(requestCode == REQUEST_CODE_GET_FILE) sendFile(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initView() {
        tvTitle.setText(mTargetUser.getName());
        ivMore.setVisibility(View.GONE);
        mLocatingDialog = new LocatingDialog();
        PermissionHelper.getInstance().with(this).requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO},
                new IPermissionsCallback() {
                    @Override
                    public void onAccepted(List<Permission> permissions) {

                    }

                    @Override
                    public void onDenied(List<Permission> permissions) {
                        ToastUtils.showToast(App.getContext(), getString(R.string.toast_permission_rejected));
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
        mMessageList = ConnectManager.getInstance().getMessages(mTargetUser.getIp());
        mRvChatAdapter = new RvChatAdapter(mMessageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(mRvChatAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initCallback() {
        //下拉刷新监听
        srlChat.setOnRefreshListener(() ->
                new Handler().postDelayed(() ->
                srlChat.setRefreshing(false), 2000)
        );
        //editText文本变化监听
        edEdit.addTextChangedListener(new TextWatchListener() {
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
            if(isKeyboardShowing) KeyBoardUtils.closeKeyBoard(ChatActivity.this, edEdit);
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
        ConnectManager.getInstance().addReceiveMessageCallback(mTargetUser.getIp(), message -> {
            if(message.mesType == MesType.ERROR){
                return;
            }
            addMessage(message);
        });
        //发送消息回调监听
        ConnectManager.getInstance().setSendMessgeCallback(new ISendMessgeCallback() {
            @Override
            public void onSendSuccess(Mes<?> message) {
                if(message.mesType == MesType.IMAGE || message.mesType == MesType.FILE || message.mesType == MesType.ERROR){
                    return;
                }
                addMessage(message);
            }

            @Override
            public void onSendFail(Mes<?> message) {
                ToastUtils.showToast(App.getContext(), "发送消息失败");
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
                ToastUtils.showToast(App.getContext(), getString(R.string.chat_audio_error));
            }
        });
        //聊天列表的item点击回调
        mRvChatAdapter.setOnItemClickListener((adapter, view, position) -> {
            Mes message = mMessageList.get(position);
            if(message.mesType == MesType.AUDIO){
                Audio audio = (Audio) message.data;
                ImageView imageView = view.findViewById(R.id.iv_message);
                Drawable drawable = imageView.getBackground();
                int audioBg = message.itemType == ItemType.SEND_AUDIO ? R.drawable.ic_audio_right_3 : R.drawable.ic_audio_left_3;
                int audioBgAnim =  message.itemType == ItemType.SEND_AUDIO ? R.drawable.anim_item_audio_right : R.drawable.anim_item_audio_left;
                if(drawable instanceof AnimationDrawable){
                    MediaPlayerManager.getInstance().stopPlayAudio();
                    imageView.setBackgroundResource(audioBg);
                }else {
                    if(mLastPosition != -1 && position != mLastPosition){
                        Mes lastMessage = mMessageList.get(mLastPosition);
                        int lastAudioBg = lastMessage.itemType == ItemType.SEND_AUDIO ? R.drawable.ic_audio_right_3 : R.drawable.ic_audio_left_3;
                        LinearLayoutManager manager = (LinearLayoutManager) rvChat.getLayoutManager();
                        View lastView = manager.findViewByPosition(mLastPosition);
                        if(lastView != null){
                            lastView.findViewById(R.id.iv_message).setBackgroundResource(lastAudioBg);
                        }
                    }
                    imageView.setBackgroundResource(audioBgAnim);
                    AnimationDrawable audioAnimDrawable = (AnimationDrawable)imageView.getBackground();
                    audioAnimDrawable.start();
                    MediaPlayerManager.getInstance().startPlayAudio(audio.audioPath, mp -> imageView.setBackgroundResource(audioBg));
                }
            }
            if(message.mesType == MesType.IMAGE && !isSendingImage){
                Image image = (Image) message.data;
                FileUtils.openFile(ChatActivity.this, image.imagePath);
            }
            if(message.mesType == MesType.FILE && !isSendingFile){
                Document file = (Document) message.data;
                FileUtils.openFile(ChatActivity.this, file.filePath);
            }
            mLastPosition = position;
        });
    }

    @OnClick({R.id.iv_add, R.id.iv_back, R.id.iv_emoji, R.id.btn_send, R.id.iv_audio, R.id.iv_keyborad, R.id.rl_album, R.id.rl_camera, R.id.rl_file, R.id.rl_location})
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
            case R.id.rl_album:
                chooseImage();
                break;
            case R.id.rl_camera:
                takeImage();
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.rl_file:
                chooseFile();
                break;
            case R.id.rl_location:
                sendLocation();
                break;
            default:
                break;
        }
    }

    /**
     * 选择文件
     */
    private void chooseFile() {
        String regex = ".*\\.(txt|ppt|doc|xls|pdf|apk|zip|rar|pptx|docx|xlsx|mp3|mp4)$";
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(REQUEST_CODE_GET_FILE)
                .withHiddenFiles(false)
                .withFilter(Pattern.compile(regex))
                .withTitle(getString(R.string.chat_choose_file))
                .start();
    }

    /**
     * 拍照
     */
    private void takeImage() {
        PermissionHelper.getInstance().with(this).requestPermission(
                Manifest.permission.CAMERA,
                new IPermissionCallback() {
                    @Override
                    public void onAccepted(Permission permission) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String imageFileName = System.currentTimeMillis() + ".png";
                        mTakedImageUri = ImageUtil.getImageUri(ChatActivity.this, FileUtil.getImagePath(mTargetUser.getIp(), ItemType.SEND_IMAGE), imageFileName);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTakedImageUri);
                        startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_IMAGE);
                    }

                    @Override
                    public void onDenied(Permission permission) {
                        ToastUtils.showToast(App.getContext(), getString(R.string.toast_permission_rejected));
                    }
                }
        );
    }

    /**
     * 选择照片
     */
    private void chooseImage() {
        startActivityForResult(IntentUtils.getChooseImageIntent(), REQUEST_CODE_GET_IMAGE);
    }

    /**
     * 发送文字消息
     */
    private void sendText() {
        String text = edEdit.getText().toString();
        Mes<String> message = new Mes<>(ItemType.SEND_TEXT, MesType.TEXT, mUser.getIp(), text);
        ConnectManager.getInstance().sendMessage(mTargetUser.getIp(), message);
        edEdit.setText("");
    }

    /**
     * 发送音频消息
     */
    private void sendAudio(String audioPath, int duration) {
        Audio audio = new Audio(duration, audioPath);
        Mes<Audio> message = new Mes<>(ItemType.SEND_AUDIO, MesType.AUDIO, mUser.getIp(), audio);
        ConnectManager.getInstance().sendMessage(mTargetUser.getIp(), message);
    }

    /**
     * 发送图片消息
     */
    private void sendImage(Uri imageUri) {
        isSendingImage = true;
        String imagePath = ImageUtil.saveImageByUri(this, imageUri, mTargetUser.getIp());
        Image image = new Image(imagePath);
        Mes<Image> message = new Mes<>(ItemType.SEND_IMAGE, MesType.IMAGE, mUser.getIp(), image);
        addMessage(message);
        final int sendingImagePostion = mMessageList.indexOf(message);
        ConnectManager.getInstance().sendMessage(mTargetUser.getIp(), message, progress -> {
            if(progress >= 100){
                isSendingImage = false;
            }
            if(mMessageList.isEmpty()){
                isSendingImage = false;
                return;
            }            Image sendingImage = (Image) mMessageList.get(sendingImagePostion).data;
            sendingImage.progress = progress;
            mRvChatAdapter.notifyItemChanged(sendingImagePostion);
        });
    }

    /**
     * 发送定位信息
     */
    private void sendLocation() {
        Criteria criteria = new Criteria();//配置定位的一些配置信息
        criteria.setPowerRequirement(Criteria.POWER_LOW);//低电耗
        criteria.setBearingAccuracy(Criteria.ACCURACY_COARSE);//标准精度为粗糙
        criteria.setAltitudeRequired(false);//不需要海拔
        criteria.setBearingRequired(false);//不需要导向
        criteria.setAccuracy(Criteria.ACCURACY_LOW);//精度为低
        criteria.setCostAllowed(false);//不需要成本
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String bestProvider = locationManager.getBestProvider(criteria, true);//得到最好的位置提供者，如GPS，netWork等
        LogUtil.d(TAG, "provider = " + bestProvider);
        PermissionHelper.getInstance().with(this).requestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                new IPermissionCallback() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onAccepted(Permission permission) {
                        mLocatingDialog.show(getSupportFragmentManager());
                        ConnectManager.getInstance().executeTast(() -> {
                            Location location = null;//里面存放着定位的信息,经纬度,海拔等
                            if(!TextUtils.isEmpty(bestProvider)){
                                location = locationManager.getLastKnownLocation(bestProvider);
                                LogUtil.d(TAG, "location = " + location);
                            }else {//没有最好的定位方案则手动配置
                                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                else if(locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))
                                    location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                            }
                            LogUtil.d(TAG, "location = " + location);
                            final Location finalLocation = location;
                            runOnUiThread(() -> {
                                if(null == finalLocation){
                                    mLocatingDialog.dismiss();
                                    ToastUtils.showToast(App.getContext(), getString(R.string.toast_location_fail));
                                    return;
                                }
                                Geocoder geocoder = new Geocoder(ChatActivity.this, Locale.getDefault());//地区编码,可以得到具体的地理位置
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(finalLocation.getLatitude(), finalLocation.getLongitude(), 1);
                                    if(CommonUtil.isEmptyList(addresses)){
                                        mLocatingDialog.dismiss();
                                        ToastUtils.showToast(App.getContext(), getString(R.string.toast_location_fail));
                                        return;
                                    }
                                    Address address = addresses.get(0);
                                    String country = address.getCountryName();
                                    String city = address.getLocality();
                                    String citySub = address.getSubLocality();
                                    String thoroughfare = address.getThoroughfare();
                                    LogUtil.d(TAG, "country = " + country
                                            + ", city = " + city
                                            + ", citySub = " + citySub
                                            + ", fare = " + thoroughfare);
                                    StringBuilder builder = new StringBuilder(32);
                                    builder.append("位置：").append(country).append(city);
                                    if(!TextUtils.isEmpty(citySub)) builder.append(citySub);
                                    if(!TextUtils.isEmpty(thoroughfare)) builder.append(thoroughfare);
                                    ConnectManager.getInstance().sendMessage(
                                            mTargetUser.getIp(),
                                            new Mes<String>(ItemType.SEND_TEXT, MesType.TEXT, mUser.getIp(), builder.toString()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    ToastUtils.showToast(App.getContext(), getString(R.string.toast_location_fail));
                                    LogUtil.e(TAG, "定位失败， e = " + e.getMessage());
                                }
                                mLocatingDialog.dismiss();
                            });
                        });
                    }

                    @Override
                    public void onDenied(Permission permission) {
                        ToastUtils.showToast(App.getContext(), getString(R.string.toast_permission_rejected));
                    }
                }
        );
    }

    /**
     * 发送文件
     */
    private void sendFile(String filePath) {
        isSendingFile = true;
        String fileType = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase(Locale.getDefault());
        String size = FileUtil.getFileSize(filePath);
        String name = filePath.substring(filePath.lastIndexOf(java.io.File.separator) + 1, filePath.lastIndexOf("."));
        Document file = new Document(filePath, name, size, fileType);
        Mes<Document> message = new Mes<>(ItemType.SEND_FILE, MesType.FILE, mUser.getIp(), file);
        addMessage(message);
        final int sendingFilePosition = mMessageList.indexOf(message);
        ConnectManager.getInstance().sendMessage(mTargetUser.getIp(), message, progress -> {
            if(progress >= 100){
                isSendingFile = false;
            }
            if(mMessageList.isEmpty()){
                isSendingFile = false;
                return;
            }
            Document sendingFile = (Document) mMessageList.get(sendingFilePosition).data;
            sendingFile.progress = progress;
            mRvChatAdapter.notifyItemChanged(sendingFilePosition);
        });
    }

    /**
     * 往底部添加一条信息
     */
    private void addMessage(Mes<?> message) {
        mMessageList.add(message);
        mRvChatAdapter.notifyItemInserted(mMessageList.size());
        rvChat.smoothScrollToPosition(mMessageList.size() - 1);
    }

    /**
     * 改变输入键盘显示
     */
    private void changeEditLayout() {
        ivKeyborad.setVisibility(View.INVISIBLE);
        ivAudio.setVisibility(View.VISIBLE);
        if(clMore.isShown()) clMore.setVisibility(View.GONE);
        if(llEmoji.isShown()) llEmoji.setVisibility(View.GONE);
        if(!isKeyboardShowing) KeyBoardUtils.openKeyBoard(this, edEdit);
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
        if(isKeyboardShowing) KeyBoardUtils.closeKeyBoard(this, edEdit);
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
            tvAudio.setVisibility(View.INVISIBLE);
            edEdit.setVisibility(View.VISIBLE);
            ivAudio.setVisibility(View.VISIBLE);
            ivKeyborad.setVisibility(View.INVISIBLE);
            if (llEmoji.isShown()) llEmoji.setVisibility(View.GONE);
        } else if (clMore.isShown() && !isKeyboardShowing) {//如果键盘没有显示，但更多布局显示，隐藏更多布局，显示键盘
            clMore.setVisibility(visibility);
            KeyBoardUtils.openKeyBoard(this, edEdit);
        } else if (!clMore.isShown() && isKeyboardShowing) {//如果只有键盘显示，就隐藏键盘，显示更多布局
            lockContentHeight();
            KeyBoardUtils.closeKeyBoard(this, edEdit);
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
            tvAudio.setVisibility(View.INVISIBLE);
            edEdit.setVisibility(View.VISIBLE);
            edEdit.requestFocus();
            ivAudio.setVisibility(View.VISIBLE);
            ivKeyborad.setVisibility(View.INVISIBLE);
            if (clMore.isShown()) clMore.setVisibility(View.GONE);
        } else if (llEmoji.isShown() && !isKeyboardShowing) {
            llEmoji.setVisibility(visibility);
            KeyBoardUtils.openKeyBoard(this, edEdit);
        } else if (!llEmoji.isShown() && isKeyboardShowing) {
            lockContentHeight();
            KeyBoardUtils.closeKeyBoard(this, edEdit);
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
