package com.example.myglide.callback;

import android.graphics.Bitmap;

/**
 * Created by 陈健宇 at 2019/11/7
 */
public interface JobListener {

    void onJobComplete(String key, Bitmap bitmap);
    void onJobCancelled(String key);

}
