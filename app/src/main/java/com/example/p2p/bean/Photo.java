package com.example.p2p.bean;

import androidx.annotation.NonNull;

/**
 * 在照片墙展示的照片实体类
 * Created by 陈健宇 at 2019/9/27
 */
public class Photo {

    public String path;
    public long time;
    public String name;
    public int position;
    public boolean isChecked;

    public Photo(String name, String path) {
        this.path = path;
        this.name = name;
    }

    public Photo(String name, String path, long time) {
        this.path = path;
        this.name = name;
        this.time = time;
    }

    @NonNull
    @Override
    public String toString() {
        return "photo[" +
                "name = " + name +
                "path = " + path + "]";
    }
}
