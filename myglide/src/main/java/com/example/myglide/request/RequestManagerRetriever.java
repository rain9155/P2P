package com.example.myglide.request;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.myglide.MyGlide;
import com.example.myglide.lifecycle.ApplicationLifecycle;
import com.example.myglide.utils.Util;

import java.util.HashMap;
import java.util.Map;


/**
 * 获取RequestManager（暂时支持Application和Activity）
 * 根据是Application的Context还是非Application的Context来获取RequestManager
 * 如果是Application的Context，返回一个单例的RequestManager，并关联Context
 * 如果是非Application的Context，就返回这个Context关联的RequestManager，如果没有就创建它并关联Context
 * Created by 陈健宇 at 2019/11/5
 */
public class RequestManagerRetriever {

    private static final String TAG = RequestManagerRetriever.class.getSimpleName();
    private static final String FRAGMENT_TAG = RequestManagerRetriever.class.getName();
    private static final int MES_REMOVE_FRAGMENT = 0;
    private static final int MES_REMOVE_SUPPORT_FRAGMENT = 1;


    private volatile RequestManager mApplicationManager;

    //因为FragmentTransaction的commit方法执行后并不是马上把Fragment提交到FragmentManager，它会等待主线程准备好后（消息轮询到）才把Fragment提交到FragmentManager
    //所以如果这时有大量的请求发起，就会因为FragmentManager还没有准备好，而导致Fragment在FragmentManager中根据TAG找不到，从而创建多个Fragment添加到FragmentManager中
    //所以Glide的解决办法是：先 new Fragment 然后根据FragmentManager放入Map中缓存起来，接着再commit，若下次请求是同一个FragmentManager就把直接返回缓存的Fragment
    // 最后发送消息给Handler，根据FragmentManager移除掉缓存的Fragment，相当于延时，这个消息一定是在commit方法执行后才会轮询到，所以此时Fragment已经提交到FragmentManager了
    private final Map<FragmentManager, SupportRequestManagerFragment> mPendingSupportRequestManagerFragments = new HashMap<>();
    private final Map<android.app.FragmentManager, RequestManagerFragment> mPendingRequestManagerFragments = new HashMap<>();

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MES_REMOVE_FRAGMENT){
                mPendingRequestManagerFragments.remove((android.app.FragmentManager)msg.obj);
            }else if(msg.what == MES_REMOVE_SUPPORT_FRAGMENT){
                mPendingSupportRequestManagerFragments.remove((FragmentManager)msg.obj);
            }
            super.handleMessage(msg);
        }
    };


    public RequestManager get(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("You cannot start a load on a null context");
        } else if (!(context instanceof Application)) {
            if (context instanceof FragmentActivity) {
                return get((FragmentActivity) context);
            } else if (context instanceof Activity) {
                return get((Activity) context);
            } else if (context instanceof ContextWrapper) {
                return get(((ContextWrapper) context).getBaseContext());
            }
        }
        return getApplicationManager(context);
    }


    /**
     * 如果是在子线程或者传进来的Context是Application的
     * 我们就要获取和Application关联的RequestManager
     * 这时传进RequestManager的Context和Lifecycle在本应用内都是唯一的
     */
    private RequestManager getApplicationManager(@NonNull Context context) {
        if (mApplicationManager == null) {
            synchronized (this) {
                if (mApplicationManager == null) {
                    mApplicationManager = new RequestManager(
                            MyGlide.getInstance(context.getApplicationContext()),
                            new ApplicationLifecycle(),
                            context.getApplicationContext());
                }
            }
        }
        return mApplicationManager;
    }

    /**
     * 向FragmentActivity中添加SupportFragment
     * SupportFragment在FragmentActivity中是唯一的
     * RequestManager在SupportFragment是唯一的
     */
    public RequestManager get(@NonNull FragmentActivity activity) {
        if(!Util.isOnUIThread()){
            return getApplicationManager(activity.getApplicationContext());
        }
        assertNotDestroyed(activity);
        FragmentManager fm = activity.getSupportFragmentManager();
        return getSupportRequestManager(activity, fm);
    }

    private RequestManager getSupportRequestManager(@NonNull Context context, @NonNull FragmentManager fm) {
        SupportRequestManagerFragment current = getSupportRequestManagerFragment(fm);
        RequestManager requestManager = current.getRequestManager();
        if (requestManager == null) {
            requestManager = new RequestManager(
                    MyGlide.getInstance(context),
                    current.getFragmentLifecycle(),
                    context
            );
            current.setRequestManager(requestManager);
        }
        return requestManager;
    }

    private SupportRequestManagerFragment getSupportRequestManagerFragment(@NonNull final FragmentManager fm) {
        SupportRequestManagerFragment current = (SupportRequestManagerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        if (current == null) {
            current = mPendingSupportRequestManagerFragments.get(fm);
            if(current == null){
                current = new SupportRequestManagerFragment();
                mPendingSupportRequestManagerFragments.put(fm, current);
                fm.beginTransaction()
                        .add(current, FRAGMENT_TAG)
                        .commitAllowingStateLoss();
                mHandler.obtainMessage(MES_REMOVE_SUPPORT_FRAGMENT, fm).sendToTarget();
            }

        }
        return current;
    }

    /**
     * 向Activity中添加android.app.Fragment
     * Fragment在Activity中是唯一的
     * RequestManager在Fragment是唯一的
     */
    @SuppressWarnings("deprecation")
    public RequestManager get(@NonNull Activity activity) {
        if(!Util.isOnUIThread()){
            return getApplicationManager(activity.getApplicationContext());
        }
        assertNotDestroyed(activity);
        android.app.FragmentManager fm = activity.getFragmentManager();
        return getRequestManager(activity, fm);
    }

    private RequestManager getRequestManager(@NonNull Context context, @NonNull android.app.FragmentManager fm) {
        RequestManagerFragment current = getRequestManagerFragment(fm);
        RequestManager requestManager = current.getRequestManager();
        if (requestManager == null) {
            requestManager = new RequestManager(
                    MyGlide.getInstance(context),
                    current.getFragmentLifecycle(),
                    context);
            current.setRequestManager(requestManager);
        }
        return requestManager;
    }


    @SuppressWarnings("deprecation")
    private RequestManagerFragment getRequestManagerFragment(@NonNull final android.app.FragmentManager fm) {
        RequestManagerFragment current = (RequestManagerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        if (current == null) {
            current = mPendingRequestManagerFragments.get(fm);
            if(current == null){
                current = new RequestManagerFragment();
                fm.beginTransaction()
                        .add(current, FRAGMENT_TAG)
                        .commitAllowingStateLoss();
                mHandler.obtainMessage(MES_REMOVE_FRAGMENT, fm).sendToTarget();
            }
        }
        return current;
    }


    private boolean isActivityVisible(Activity activity) {
        return !activity.isFinishing();
    }

    private void assertNotDestroyed(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) {
                throw new IllegalArgumentException("You cannot start a load for a destroyed activity");
            }
        }
    }

}
