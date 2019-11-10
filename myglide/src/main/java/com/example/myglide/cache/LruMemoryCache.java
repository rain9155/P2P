package com.example.myglide.cache;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.collection.LruCache;
import com.example.myglide.utils.Log;

/**
 * 用LRU实现内存缓存:
 * 用于缓存下载好的图片，在程序内存达到设定值时会将最近最久未使用的图片从缓存中移除
 * Created by 陈健宇 at 2018/9/19
 */
public class LruMemoryCache implements MemoryCache {

    private static final String TAG = LruMemoryCache.class.getSimpleName();

    private LruCache<String, Bitmap> mMemoryCache;//内存缓存

    public LruMemoryCache() {
        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        //设置图片内存缓存大小为程序最大可用内存的1/8
        int cacheSize = maxMemory / 8;
        Log.d(TAG, "LruMemoryCache, The maximum amount of memory is "  + (maxMemory / 1024 / 1024) + "MB");
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return getBitmapSize(bitmap);
            }
        };
    }

    @Override
    public void put(String key, Bitmap bitmap) {
        if (get(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    @Override
    public Bitmap get(String key) {
        return mMemoryCache.get(key);
    }

    @Override
    public void clear() {
        mMemoryCache.evictAll();
    }

    /**
     * 获取一张Bitmap图片的字节大小
     * getByteCount()： 会返回图片每行像素乘以图片的高的大小，即图片的实际占用内存大小
     * getAllocationByteCount()： 会返回图片已经分配的内存大小
     * 在一般情况下，getByteCount() == getAllocationByteCount()，但是在图片复用的情况下，
     * getAllocationByteCount()返回的是复用图像所占内存的大小，getByteCount()返回的是新解码图片占用内存的大小，
     * 如图片被解码成更小的图片，这时getAllocationByteCount() > getByteCount()
     */
    private int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        }
        return bitmap.getByteCount();
    }
}
