package com.example.myglide.job;

import android.graphics.Bitmap;

import com.example.myglide.MyGlide;
import com.example.myglide.cache.MemoryCache;
import com.example.myglide.callback.JobListener;
import com.example.myglide.callback.ResourceCallback;
import com.example.myglide.utils.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 陈健宇 at 2019/11/5
 */
public class Engine implements JobListener {

    private static final String TAG = Engine.class.getSimpleName();

    private Map<String, EngineDecodeJob> mJobs;
    private MyGlide mImageLoader;
    private MemoryCache mMemoryCache;
    private EngineDecodeJobFactory mJobFactory;


    public Engine(MyGlide imageLoader){
            this.mImageLoader = imageLoader;
            this.mMemoryCache = imageLoader.getMemoryCache();
            mJobFactory = new EngineDecodeJobFactory();
            mJobs = new HashMap<>();
    }

    public EngineDecodeJob load(
            final String model,
            final int width,
            final int height,
            final ResourceCallback callback

    ){
        final String key = hashKeyFromUrl(model);

        Bitmap bitmap = loadFromMemoryCache(key);
        if(bitmap != null){
            Log.d(TAG, "load, loadBitmapFromMemory, url =  " + model);
            callback.onResourceReady(bitmap);
            return null;
        }

        EngineDecodeJob engineDecodeJob = mJobs.get(key);

        if(engineDecodeJob != null){
            engineDecodeJob.setCallback(callback);
            return engineDecodeJob;
        }
        engineDecodeJob = mJobFactory.build(
                mImageLoader,
                key,
                width,
                height,
                model,
                this);
        engineDecodeJob.setCallback(callback);
        engineDecodeJob.start();

        mJobs.put(key, engineDecodeJob);

        return engineDecodeJob;
    }

    /**
     * 从内存加载
     */
    private Bitmap loadFromMemoryCache(String key){
        if(mMemoryCache == null){
            return null;
        }
        return mMemoryCache.get(key);
    }

    /**
     * 把图片的url转换成合法字符串（MD5编码）
     * @param url url地址
     * @return 编码后的字符串
     */
    private String hashKeyFromUrl(String url){
        String cacheKey;
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            cacheKey = bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    /**
     * 把字节数组转成16进制字符串
     * @param bytes 字节数组
     * @return 16进制字符串
     */
    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    @Override
    public void onJobComplete(String key, Bitmap bitmap) {
        mJobs.remove(key);
        if(mMemoryCache != null && bitmap != null){
            mMemoryCache.put(key, bitmap);
        }
    }

    @Override
    public void onJobCancelled(String key) {
        mJobs.remove(key);
    }

    static class EngineDecodeJobFactory{
        EngineDecodeJob build(
                MyGlide imageLoader,
                String key,
                int width,
                int height,
                String uri,
                JobListener jobListener)
        {
            EngineDecodeJob job = new EngineDecodeJob();
            job.init(
                    imageLoader,
                    key,
                    width,
                    height,
                    uri,
                    jobListener
            );
            return job;
        }
    }

}
