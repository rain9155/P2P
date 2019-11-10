package com.example.myglide.lifecycle;

import androidx.annotation.NonNull;

/**
 * ApplicationLifecycle
 * Created by 陈健宇 at 2019/11/5
 */
public class ApplicationLifecycle implements Lifecycle {

    @Override
    public void addListener(@NonNull LifecycleListener listener) {
        listener.onStart();
    }

    @Override
    public void removeListener(@NonNull LifecycleListener listener) {

    }
}
