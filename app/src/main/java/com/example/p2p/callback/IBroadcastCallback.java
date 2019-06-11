package com.example.p2p.callback;

/**
 * 用户登陆、退出的回调接口
 * Created by 陈健宇 at 2019/6/11
 */
public interface IBroadcastCallback {
    void onJoin(String ip);
    void onExit(String ip);
}
