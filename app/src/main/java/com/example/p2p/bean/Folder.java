package com.example.p2p.bean;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Photo对应的文件夹实体类
 * Created by 陈健宇 at 2019/9/27
 */
public class Folder {

    public String name;
    public List<Photo> photos;

    public Folder(String name, List<Photo> photos) {
        this.name = name;
        this.photos = photos;
    }

    @NonNull
    @Override
    public String toString() {
        return "Folder[" + "name = " + name + "]";
    }
}
