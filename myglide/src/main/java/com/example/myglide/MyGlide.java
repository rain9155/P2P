package com.example.myglide;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.myglide.cache.DiskCache;
import com.example.myglide.cache.LruDiskCache;
import com.example.myglide.cache.LruMemoryCache;
import com.example.myglide.cache.MemoryCache;
import com.example.myglide.job.Engine;
import com.example.myglide.request.RequestManager;
import com.example.myglide.request.RequestManagerRetriever;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 加载图片工具类
 */
public class MyGlide {

    private static final String TAG = "MyGlide";

    private static MyGlide mImageLoader;//单例模式

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();//cpu核心数
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));//线程池的核心线程数（大于等于2，小于等于4）
    private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;//线程池的最大线程数
    private static final long KEEP_ALIVE = 30L;//非核心线程数的闲置时间

    private static final ThreadFactory mThreadFactory = new ThreadFactory() {//在线程池用于创建线程

         private final AtomicInteger mCount = new AtomicInteger();

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "MyGlide#" + mCount.getAndIncrement());
        }
    };

    private static final Executor EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAX_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(256),
            mThreadFactory,
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    private final RequestManagerRetriever mRequestManagerRetriever;
    private final Engine mEngine;
    private final Context mContext;
    private LruDiskCache mDiskCache;
    private LruMemoryCache mMemoryCache;

    private MyGlide(Context context) {
        mContext = context.getApplicationContext();
        mMemoryCache = new LruMemoryCache();
        mDiskCache = new LruDiskCache(mContext);
        mRequestManagerRetriever = new RequestManagerRetriever();
        mEngine = new Engine(this);
    }

    public static MyGlide getInstance(Context context) {
        if (mImageLoader == null) {
            synchronized (MyGlide.class){
                if(mImageLoader == null){
                    mImageLoader = new MyGlide(context);
                }
            }
        }
        return mImageLoader;
    }

    public Context getContext(){
        return mContext;
    }

    public Executor getExecutor(){
        return EXECUTOR;
    }

    public Engine getEngine(){
        return mEngine;
    }

    public MemoryCache getMemoryCache(){
        return mMemoryCache;
    }

    public DiskCache getDiskCache(){
        return mDiskCache;
    }

    public RequestManagerRetriever getRequestManagerRetriever(){
        return mRequestManagerRetriever;
    }

    public static RequestManager with(Context context){
        return getInstance(context)
                .getRequestManagerRetriever()
                .get(context);
    }

    public static RequestManager with(Activity activity){
        return getInstance(activity)
                .getRequestManagerRetriever()
                .get(activity);
    }

    public static RequestManager with(FragmentActivity activity){
        return  getInstance(activity)
                .getRequestManagerRetriever()
                .get(activity);
    }

}
