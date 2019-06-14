package com.example.p2p.callback;

/**
 * 录音结果回调
 * Created by 陈健宇 at 2019/6/14
 */
public interface IRecordedCallback {

    void onFinish(String audioPath, int duration);
    void onError();
}
