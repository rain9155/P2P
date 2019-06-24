package com.example.p2p.bean;

import androidx.annotation.NonNull;

/**
 * 音频数据类
 * Created by 陈健宇 at 2019/6/16
 */
public class Audio {

    public int duartion;
    public String audioPath;

    public Audio(int duartion, String path) {
        this.duartion = duartion;
        this.audioPath = path;
    }

    @NonNull
    @Override
    public String toString() {
        return "Audio[duartion = " + duartion +  ", imagePath = " + audioPath + "]";
    }
}
