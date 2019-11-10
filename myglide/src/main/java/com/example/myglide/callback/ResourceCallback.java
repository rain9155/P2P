package com.example.myglide.callback;

import android.graphics.Bitmap;

/**
 * Created by 陈健宇 at 2019/11/5
 */
public interface ResourceCallback {

    void onResourceReady(Bitmap bitmap);
    void onLoadFailed();

}
