package com.example.p2p.bean;

import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * 图片数据类
 * Created by 陈健宇 at 2019/6/20
 */
public class Image {

    public String imagePath;
    public Uri imageUri;
    public int len;
    public int progress;

    public Image(Uri uri) {
        this("", uri, 0, 0);
    }

    public Image(String path, int len) {
        this(path, null, len, 0);
    }

    public Image(String path, Uri uri, int len, int progress) {
        this.imagePath = path;
        this.imageUri = uri;
        this.len = len;
        this.progress = progress;
    }

    @NonNull
    @Override
    public String toString() {
        return "Image[imagePath = " + imagePath
                + ", len = " + len
                + ", imageUri = " + imageUri
                + "]";
    }
}
