package com.example.p2p.core;

import android.media.MediaPlayer;

import com.example.p2p.utils.LogUtils;

import java.io.IOException;

/**
 * 统一管理音频的播放
 * Created by 陈健宇 at 2019/6/16
 */
public class MediaPlayerManager {

    private static final String TAG = MediaPlayerManager.class.getSimpleName();
    private static MediaPlayerManager sInstance;
    private MediaPlayer mMediaPlayer;

    private MediaPlayerManager(){}

    public static MediaPlayerManager getInstance(){
        if(sInstance == null){
            synchronized (MediaPlayer.class){
                MediaPlayerManager mediaPlayerManager;
                if(sInstance == null){
                    mediaPlayerManager = new MediaPlayerManager();
                    sInstance = mediaPlayerManager;
                }
            }
        }
        return sInstance;
    }

    /**
     * 开始播放音频
     * @param path 音频文件路径
     */
    public void startPlayAudio(String path){
        initRecordPlayer(path);
        mMediaPlayer.start();
    }

    /**
     * 停止播放音频
     */
    public void startPlayAudio(){
        if(mMediaPlayer == null) return;
        mMediaPlayer.stop();
    }

    /**
     * 是否正在播放中
     * @return true表示是，false表示不是或被销毁
     */
    public boolean isPlaying(){
        if(mMediaPlayer == null) return false;
        return mMediaPlayer.isPlaying();
    }


    /**
     * 获取音频时长，单位秒
     */
    public int getDuration(String path){
        initRecordPlayer(path);
        return mMediaPlayer.getDuration() / 1000;
    }

    /**
     * 释放资源
     */
    public void release(){
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * 初始化音频播放
     */
    private void initRecordPlayer(String path) {
        if(mMediaPlayer == null){
            mMediaPlayer = new MediaPlayer();
        }else {
            mMediaPlayer.reset();
        }
        try {
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "设置音频文件或准备错误，path = " + path);
        }
    }
}
