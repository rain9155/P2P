package com.example.myglide.job;

import android.graphics.Bitmap;

import com.example.myglide.MyGlide;
import com.example.myglide.cache.Key;
import com.example.myglide.cache.MemoryCache;
import com.example.myglide.callback.JobCallback;
import com.example.myglide.callback.ResourceCallback;
import com.example.myglide.utils.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 陈健宇 at 2019/11/5
 */
public class Engine implements JobCallback {

    private static final String TAG = Engine.class.getSimpleName();

    private Map<Key, EngineDecodeJob> mJobs;
    private MyGlide mImageLoader;
    private MemoryCache mMemoryCache;
    private EngineDecodeJobFactory mJobFactory;
    private KeyFactory mKeyFactory;

    public Engine(MyGlide imageLoader){
            this.mImageLoader = imageLoader;
            this.mMemoryCache = imageLoader.getMemoryCache();
            mJobFactory = new EngineDecodeJobFactory();
            mJobs = new HashMap<>();
            mKeyFactory = new KeyFactory();
    }

    public EngineDecodeJob load(
            final String model,
            final int width,
            final int height,
            final boolean isSkipMemory,
            final boolean isSkipDisk,
            final ResourceCallback callback

    ){
        final Key key = mKeyFactory.build(
                model,
                width,
                height);

        Bitmap bitmap = loadFromMemoryCache(key, isSkipMemory);
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
                isSkipMemory,
                isSkipDisk,
                this);
        engineDecodeJob.setCallback(callback);
        engineDecodeJob.start();

        mJobs.put(key, engineDecodeJob);

        return engineDecodeJob;
    }

    /**
     * 从内存加载
     */
    private Bitmap loadFromMemoryCache(Key key, boolean isSkipMemory){
        if(isSkipMemory){
            return null;
        }
        if(mMemoryCache == null){
            return null;
        }
        return mMemoryCache.get(key);
    }


    @Override
    public void onJobComplete(Key key, Bitmap bitmap, boolean isSkipMemory) {
        mJobs.remove(key);
        if(!isSkipMemory && mMemoryCache != null && bitmap != null){
            mMemoryCache.put(key, bitmap);
        }
    }

    @Override
    public void onJobCancelled(Key key) {
        mJobs.remove(key);
    }

    static class EngineDecodeJobFactory{
        EngineDecodeJob build(
                MyGlide imageLoader,
                Key key,
                int width,
                int height,
                String uri,
                boolean isSkipMemory,
                boolean isSkipDisk,
                JobCallback jobListener)
        {
            EngineDecodeJob job = new EngineDecodeJob();
            job.init(
                    imageLoader,
                    key,
                    width,
                    height,
                    uri,
                    isSkipMemory,
                    isSkipDisk,
                    jobListener
            );
            return job;
        }
    }

    static class KeyFactory{
        Key build(String url, int width, int height){
            return new Key(url, width, height);
        }
    }

}
