package com.example.p2p.callback;

import com.example.p2p.bean.User;

import java.util.List;

/**
 * 用户登陆、退出的回调接口
 * Created by 陈健宇 at 2019/6/11
 */
public interface IBroadcastCallback {
    void onOnlineUsers(List<User> users);
    void onJoin(User user);
    void onExit(User user);
}
