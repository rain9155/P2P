package com.example.p2p.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * 用户数据类
 * Created by 陈健宇 at 2019/6/9
 */
public class User implements Serializable {

    private String name;
    private String ip;

    public User(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip == null ? "" : ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || obj.getClass() != this.getClass()) return false;
        User user = (User)obj;
        if(!this.getName().equals(user.getName())) return false;
        return this.ip.equals(user.getIp());
    }

    @NonNull
    @Override
    public String toString() {
        return "User[name = " + name + ", ip = " + ip + "]";
    }
}
