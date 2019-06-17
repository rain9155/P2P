package com.example.p2p.bean;

import androidx.annotation.NonNull;

import com.example.p2p.app.App;
import com.example.p2p.config.Constant;
import com.example.utils.FileUtil;

/**
 * 广播的数据
 * Created by 陈健宇 at 2019/6/12
 */
public class Data {

    private int code; //code字段，0表示需要回复，1表示退出, 2代表回复
    private User user;

    public Data(int code) {
       this(code, (User) FileUtil.restoreObject(App.getContext(), Constant.FILE_NAME_USER));
    }

    public Data(int code, User user) {
        this.code = code;
        this.user = user;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @NonNull
    @Override
    public String toString() {
        return "Data[" + user.toString() + ", code = " + code + "]";
    }
}
