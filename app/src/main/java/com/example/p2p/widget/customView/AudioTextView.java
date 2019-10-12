package com.example.p2p.widget.customView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.example.p2p.R;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IRecordedCallback;
import com.example.p2p.core.MediaPlayerManager;
import com.example.p2p.utils.FileUtil;
import com.example.utils.DisplayUtil;
import com.example.utils.FileUtils;
import com.example.utils.LogUtils;
import com.example.utils.VibrateUtils;

import java.io.File;
import java.io.IOException;

/**
 * 录制音频的按钮
 * Created by 陈健宇 at 2019/6/13
 */
public class AudioTextView extends AppCompatTextView {

    private static final String TAG = AudioTextView.class.getSimpleName();
    private static int MIN_RECORS_TIME = 1000;//最小录音间隔，1s
    private Dialog mDialog;
    private Drawable mAudioImage;
    private TextView mAudioTextView;
    private int mBoundary;//判断是否取消发送录音的分界线
    private Drawable mPressBg;
    private Drawable mNormalBg;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording;
    private String mFileName;
    private long mStartRecordTime;
    private IRecordedCallback mRecordedCallback;
    private User mTargetUser;

    public AudioTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        this.setOnLongClickListener(v -> {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            final int mode = audioManager.getRingerMode();
            if(mode == AudioManager.RINGER_MODE_SILENT){//静音
                //nothing
            }else if(mode == AudioManager.RINGER_MODE_VIBRATE){ //震动
                VibrateUtils.Vibrate(getContext(), 100);
            }else if(mode == AudioManager.RINGER_MODE_NORMAL){//响铃
                VibrateUtils.Vibrate(getContext(), 100);
            }
            mDialog.show();
            mAudioTextView.setText(getContext().getString(R.string.dialog_audio_undo));
            mAudioImage.setLevel(0);
            mAudioTextView.setBackgroundColor(Color.TRANSPARENT);
            mStartRecordTime = System.currentTimeMillis();
            startRecord();
            return false;
        });
    }

    private void init() {
        //初始化dialog及其资源
         View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_audio, null);
         mDialog = new AlertDialog.Builder(getContext(), R.style.DialogAudioStyle)
                .setView(view)
                .setCancelable(false)
                .create();
        WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
        lp.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        lp.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        mDialog.getWindow().setAttributes(lp);
        mAudioTextView = view.findViewById(R.id.tv_toast);
        mAudioImage = view.findViewById(R.id.iv_audio).getBackground();
        int screenHeight = DisplayUtil.getScreenHeight(getContext());
        mBoundary =  screenHeight - screenHeight / 6;
        //初始化按钮资源
        mPressBg = ContextCompat.getDrawable(getContext(), R.drawable.bg_chat_audio_selected);
        mNormalBg = ContextCompat.getDrawable(getContext(), R.drawable.bg_chat_audio);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float curY = event.getRawY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                this.setBackground(mPressBg);
                this.setText(getContext().getString(R.string.chat_tvAudio_undo));
                break;
            case MotionEvent.ACTION_MOVE:
                if(isRecording){
                    if(curY < mBoundary){
                        this.setText(getContext().getString(R.string.chat_tvAudio_cancel));
                        mAudioTextView.setText(getContext().getString(R.string.dialog_audio_cancel));
                        mAudioTextView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAudioCancel));
                        mAudioImage.setLevel(8);
                    }else {
                        this.setText(getContext().getString(R.string.chat_tvAudio_undo));
                        mAudioTextView.setBackgroundColor(Color.TRANSPARENT);
                        mAudioTextView.setText(getContext().getString(R.string.dialog_audio_undo));
                        int amplitude = mMediaRecorder.getMaxAmplitude();
                        int index = amplitude / 500;
                        if(index >= 8) index = 7;
                        mAudioImage.setLevel(index);
                    }
                    LogUtils.d(TAG, "MaxAmplitude = " + mMediaRecorder.getMaxAmplitude());
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(isRecording){
                    if(curY < mBoundary){
                        cancelRecord();
                    }else {
                        sendRecord();
                    }
                }
                this.setPressed(false);
                this.setText(getContext().getString(R.string.chat_tvAudio_press));
                this.setBackground(mNormalBg);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        release();
        super.onDetachedFromWindow();
    }

    /**
     * 初始化MediaRecord
     */
    private void initRecord() {
        if(mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }else {
            mMediaRecorder.reset();
        }
        //设置音频源，这里是麦克风采集
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置音频的输出格式
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        //设置音频的编码格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //设置音频的采样率
        mMediaRecorder.setAudioSamplingRate(10000);
        //设置音频文件的输出路径
        String audioPath = FileUtil.getAudioPath(mTargetUser.getIp(), ItemType.SEND_AUDIO);
        mFileName = audioPath + System.currentTimeMillis() + ".mp3";
        new File(mFileName);
        mMediaRecorder.setOutputFile(mFileName);
        LogUtils.d(TAG, "初始化录音");
    }

    /**
     * 准备录音
     */
    private void preperRecord() {
        if(mMediaRecorder == null) return;
        try {
            mMediaRecorder.prepare();
            LogUtils.d(TAG, "准备录音");
        } catch (IOException e) {
            e.printStackTrace();
            if(mRecordedCallback != null){
                mRecordedCallback.onError();
            }
            LogUtils.d(TAG, "准备录制音频失败， e = " + e.getMessage());
        }
    }

    /**
     * 开始录音
     */
    private void startRecord(){
        initRecord();
        preperRecord();
        if(!isRecording){
            mMediaRecorder.start();
            isRecording = true;
            LogUtils.d(TAG, "开始录音");
        }
    }

    /**
     * 取消录音, 并删除该录音文件
     */
    private void cancelRecord(){
        stopRecord();
        FileUtils.deleteFiles(new File(mFileName));
        LogUtils.d(TAG, "取消录音");
        mDialog.dismiss();
    }

    /**
     * 录音完成，并通知活动发送录音
     */
    private void sendRecord(){
        stopRecord();
        long inteval = System.currentTimeMillis() - mStartRecordTime;
        if(inteval < MIN_RECORS_TIME){
            cancelStartRecord();
        }else {
            int duration = MediaPlayerManager.getInstance().getDuration(mFileName);
            if(duration < 0){
                if(mRecordedCallback != null){
                    mRecordedCallback.onError();
                }
                return;
            }
            LogUtils.d(TAG, "音频文件位置，imagePath = " + mFileName + ", 音频时长，duration = " + duration + "秒");
            if(mRecordedCallback != null){
                mRecordedCallback.onFinish(mFileName, duration);
            }
            mDialog.dismiss();
        }
        LogUtils.d(TAG, "录音完成");
    }

    /**
     * 解决一个小bug：
     * 当MediaRecord的start方法和stop方法调用的时间间隔很近时，会报错
     * 所以这里会捕获stop方法的异常，然后停止录音
     */
    private void cancelStartRecord() {
        mAudioImage.setLevel(9);
        mAudioTextView.setText(getContext().getString(R.string.dialog_audio_warnning));
        FileUtils.deleteFiles(new File(mFileName));
        new Handler().postDelayed(() -> {
            mDialog.dismiss();
        }, 500);
    }

    /**
     * 结束录音
     */
    private void stopRecord(){
        if(mMediaRecorder == null) return;
        if(isRecording){
            try{
                mMediaRecorder.stop();
                LogUtils.d(TAG, "结束录音");
            }catch (Exception e){
                LogUtils.d(TAG, "结束录音失败， e = " + e.getMessage());
                cancelStartRecord();
            }
            isRecording = false;
        }
    }

    /**
     * 释放资源
     */
    private void release(){
        if(mMediaRecorder != null){
            mMediaRecorder.release();
            mMediaRecorder = null;
            isRecording = false;
        }
        MediaPlayerManager.getInstance().release();
        LogUtils.d(TAG, "释放资源");
    }

    public void setRecordedCallback(User user, IRecordedCallback callback){
        mTargetUser = user;
        this.mRecordedCallback = callback;
    }
}
