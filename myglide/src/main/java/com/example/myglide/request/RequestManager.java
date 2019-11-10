package com.example.myglide.request;


import android.content.Context;

import com.example.myglide.MyGlide;
import com.example.myglide.lifecycle.Lifecycle;
import com.example.myglide.lifecycle.LifecycleListener;
import com.example.myglide.utils.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Request管理者，实现了LifecycleListener
 * 在创建RequestManager时把自己注册到Fragment中的的Lifecycle
 * 这样当Fragment的生命周期事件发生时，RequestManager就能收到相应的回调
 * Created by 陈健宇 at 2019/11/4
 */
public class RequestManager implements LifecycleListener {

    private static final String TAG = RequestManager.class.getSimpleName();

    //通过Set，管理着所有的请求，这个Set是根据WeakHashMap创建的
    // 当gc时，会自动移除Request，避免Request过多时出现OOM
    private final Set<Request> mRequests = Collections.newSetFromMap(new WeakHashMap<Request, Boolean>());
    //强引用列表，当Fragment进入pause状态时，把Request添加进这个列表，这样这个Request就有一个强引用，当进行gc时，它在WeakHashMap中就不会被自动移除
    // 等待下次resume时，重新把所有在列表中的Request移除，再次进行请求
    private final List<Request> mPendingRequests = new ArrayList<>();
    private final Lifecycle mLifecycle;
    private MyGlide mImageLoader;
    private Context mContext;
    private boolean isPause;//是否Pause状态，当OnStop回调时，isPause = true


    public RequestManager(MyGlide imageLoader, Lifecycle lifecycle, Context context) {
        mLifecycle = lifecycle;
        mImageLoader = imageLoader;
        mContext = context;
        mLifecycle.addListener(this);
    }

    public Request.Builder load(String uri){
        return new Request
                .Builder(mImageLoader.getEngine(), this, mContext)
                .load(uri);
    }


    void track(Request request){
        mRequests.add(request);
        if(!isPause){
            request.begin();
        }else {
            request.cancel();
            mPendingRequests.add(request);
        }
    }

    public boolean isPause(){
        return isPause;
    }

    @Override
    public void onStart() {
        isPause = false;
        for(Request request : mRequests){
            //当Fragment进入Pause状态时，即onStop方法调用时，Request会处于CANCEL状态
            //在track方法中，当处于Pause状态时，不会去调用request的begin方法，而是取消加载，使得Request处于CANCEL状态，等待onStart()方法调用begin方法
            //当进入Pause状态时，Manager调用了Request的cancel方法，使得Request处于CANCEL状态，等待onStart()方法重新调用begin方法
            if(request.isCancel()){
                request.begin();
            }
        }
        Log.d(TAG, "onStart()"
                + ", resume request size = " + mPendingRequests.size()
                + ", total request size = " + mRequests.size());
        mPendingRequests.clear();
    }

    @Override
    public void onStop() {
        isPause = true;
        for (Request request : mRequests){
            if(request.isRunning()){
                request.cancel();
                mPendingRequests.add(request);
            }
        }
        Log.d(TAG, "onStop()"
                + ", cancel request size = " + mPendingRequests.size()
                + ", total request size = " + mRequests.size());

    }

    @Override
    public void onDestroy() {
        isPause = true;
        Log.d(TAG, "onDestroy()"
                + ", cancel request size = " + mPendingRequests.size());
        //Activity的生命周期：onStop -> onDestory，onStop方法比onDestory先执行
        // onStop方法中已经把所有正在执行的Request取消了，并用mPendingRequests保留着对它们的强引用
        //所以onDestroy方法只需要释放调用所有对Request的强引用，gc会自动的把所有Request回收掉
        mPendingRequests.clear();
    }
}
