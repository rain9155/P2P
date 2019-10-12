package com.example.p2p.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by 陈健宇 at 2019/10/10
 */
public class Photo {

    public String path;
    public long time;
    public String name;
    public boolean isSelect;
    public int position;//在预览列表中的下标
    public int selectPos;//在预览中底部列表的下标

    public Photo(Photo photo){
        this.path = photo.path;
        this.time = photo.time;
        this.name = photo.name;
        this.isSelect = photo.isSelect;
        this.position = photo.position;
        this.selectPos = photo.selectPos;
    }

    public Photo(String name, String path) {
        this.path = path;
        this.name = name;
    }

    public Photo(String name, String path, long time) {
        this.path = path;
        this.name = name;
        this.time = time;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || obj.getClass() != this.getClass()) return false;
        Photo photo = (Photo) obj;
        if(!photo.name.equals(this.name)) return false;
        if(!photo.path.equals(this.path)) return false;
        return photo.position == this.position;
    }

    @NonNull
    @Override
    public String toString() {
        return "photo[" +
                "name = " + name +
                "path = " + path + "]";
    }

}
