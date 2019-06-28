package com.example.p2p.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * 用户数据类
 * Created by 陈健宇 at 2019/6/9
 */
public class User implements Serializable {

    private String mName;
    private String mIp;
    private String mImagePath;
    private int mImageLen;

    public User(String name, String ip, String imagePath) {
       this(name, ip, imagePath, 0);
    }

    public User(String name, String ip, String imagePath, int imageLen) {
        this.mName = name;
        this.mIp = ip;
        this.mImagePath = imagePath;
        this.mImageLen = imageLen;
    }

    public String getImagePath() {
        return mImagePath == null ? "" : mImagePath;
    }

    public void setImagePath(String imagePath) {
        mImagePath = imagePath;
    }

    public int getImageLen() {
        return mImageLen;
    }

    public void setImageLen(int imageLen) {
        mImageLen = imageLen;
    }

    public String getName() {
        return mName == null ? "" : mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getIp() {
        return mIp == null ? "" : mIp;
    }

    public void setIp(String ip) {
        this.mIp = ip;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || obj.getClass() != this.getClass()) return false;
        User user = (User)obj;
        if(!this.getName().equals(user.getName())) return false;
        return this.mIp.equals(user.getIp());
    }

    @NonNull
    @Override
    public String toString() {
        return "User[name = " + mName
                + ", ip = " + mIp
                + ", imagePath = " + mImagePath
                + ", imageLen = " + mImageLen
                + "]";
    }
}
