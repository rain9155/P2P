package com.example.myglide.lifecycle;

import androidx.annotation.NonNull;

/**
 * 管理着所有的listener，当生命周期事件到达时，
 * 负责通知所有的listener回调相应的生命周期事件回调
 * Created by 陈健宇 at 2019/11/4
 */
public interface Lifecycle {

    /**
     * 添加一个Listener到Lifecycle管理的集合中
     */
    void addListener(@NonNull LifecycleListener listener);

    /**
     * 从Lifecycle管理的集合中移除掉这个listener
     */
    void removeListener(@NonNull LifecycleListener listener);

}
