package com.example.p2p.bean;

import androidx.annotation.NonNull;

/**
 * 聊天内容
 * Created by 陈健宇 at 2019/6/10
 */
public class Mes<T>{

    public int id;
    public MesType mesType;
    public String userIp;
    public T data;

    public Mes(MesType type){
        this(type, "", null);
    }

    public Mes(MesType type, T data){
        this(type, "", data);
    }

    public Mes(MesType type, String userIp, T data) {
        this.mesType = type;
        this.userIp = userIp;
        this.data = data;
    }

    @NonNull
    @Override
    public String toString() {
        return "Mes[id = " + id
                + ", type = " + mesType
                + ", userIp = " + userIp
                +  ", data = " + data
                + "]";
    }
}
