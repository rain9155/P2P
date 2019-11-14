package com.example.myglide.cache;

import android.graphics.Bitmap;

/**
 * 内存缓存
 * Created by 陈健宇 at 2019/11/4
 */
public interface MemoryCache {


    /**
     * 将一张图片存储到内存缓存中
     * @param key 图片的唯一标识
     * @param bitmap 要缓存的图片
     */
    void put(Key key, Bitmap bitmap);

    /**
     * 从内存缓存中获取一张图片，如果不存在就返回null
     * @param key 图片的唯一标识
     * @return 如果找到就返回图片，否则返回null
     */
    Bitmap get(Key key);

    /**
     * 用于在发生lowMemory时清除所有缓存
     */
    void clear();

}
