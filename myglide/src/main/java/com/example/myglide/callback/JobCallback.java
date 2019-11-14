package com.example.myglide.callback;

import android.graphics.Bitmap;

import com.example.myglide.cache.Key;

/**
 * Created by 陈健宇 at 2019/11/7
 */
public interface JobCallback {

    void onJobComplete(Key key, Bitmap bitmap, boolean isSkipMemory);
    void onJobCancelled(Key key);

}
