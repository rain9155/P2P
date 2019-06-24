package com.example.p2p.callback;

import com.example.p2p.bean.Mes;

/**
 * 接受消息接口回调
 * Created by 陈健宇 at 2019/6/10
 */
public interface IReceiveMessageCallback {
    void onReceiveSuccess(Mes<?> message);
}
