package com.example.p2p.callback;

import com.example.p2p.bean.Mes;
import com.example.p2p.bean.MesType;

/**
 * 发送消息的接口回调
 * Created by 陈健宇 at 2019/6/10
 */
public interface ISendMessgeCallback {

    void onSendSuccess(Mes<?> message);
    void onSendFail(Mes<?> message);

}
