package com.example.myglide.lifecycle;

/**
 * 生命周期回调
 * Created by 陈健宇 at 2019/11/4
 */
public interface LifecycleListener {

    /**
     * 当Activity或Fragment的onStart方法回调时，该方法回调
     */
    void onStart();

    /**
     * 当Activity或Fragment的onStop方法回调时，该方法回调
     */
    void onStop();

    /**
     * 当Activity或Fragment的onDestory方法回调时，该方法回调
     */
    void onDestroy();

}
