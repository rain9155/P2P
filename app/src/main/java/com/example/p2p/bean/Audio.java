package com.example.p2p.bean;

import androidx.annotation.NonNull;

/**
 * 音频数据类
 * Created by 陈健宇 at 2019/6/16
 */
public class Audio {

    public int duartion;
    public byte[] bytes;
    public String path;

    public Audio(int duartion, String path) {
        this(duartion, null, path);
    }

    public Audio(int duartion, byte[] bytes) {
        this(duartion, bytes, "");
    }

    public Audio(int duartion, byte[] bytes, String path) {
        this.duartion = duartion;
        this.bytes = bytes;
        this.path = path;
    }

    @NonNull
    @Override
    public String toString() {
        return "Audio[duartion = " + duartion + ", bytes = " + bytes + ", path = " + path + "]";
    }
}
