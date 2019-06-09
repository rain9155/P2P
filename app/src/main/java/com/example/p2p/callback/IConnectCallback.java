package com.example.p2p.callback;

/**
 * 连接回调接口
 * Created by 陈健宇 at 2019/6/9
 */
public interface IConnectCallback {

    void onConnectSuccess(String targetIp);
    void onConnectFail(String targetIp);

}
