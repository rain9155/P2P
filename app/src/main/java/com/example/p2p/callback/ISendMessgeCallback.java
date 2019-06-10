package com.example.p2p.callback;

/**
 * 发送消息的接口回调
 * Created by 陈健宇 at 2019/6/10
 */
public interface ISendMessgeCallback {

    void onSendSuccess(String message);
    void onSendFail(String message);

}
