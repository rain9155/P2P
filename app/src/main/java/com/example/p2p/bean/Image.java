package com.example.p2p.bean;

import androidx.annotation.NonNull;

/**
 * 图片数据类
 * Created by 陈健宇 at 2019/6/20
 */
public class Image {

    public String imagePath;
    public int len;
    public int progress;

    public Image(String path) {
        this.imagePath = path;
    }

    public Image(String path, int len) {
        this.imagePath = path;
        this.len = len;
    }

    @NonNull
    @Override
    public String toString() {
        return "Image[imagePath = " + imagePath + ", len = " + len + "]";
    }
}
