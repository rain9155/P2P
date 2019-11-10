package com.example.myglide.request;

import androidx.fragment.app.Fragment;

import com.example.myglide.lifecycle.FragmentLifecycle;
import com.example.myglide.utils.Log;


/**
 * Created by 陈健宇 at 2019/11/5
 */
public class SupportRequestManagerFragment extends Fragment {

    private static final String TAG = SupportRequestManagerFragment.class.getSimpleName();
    private final FragmentLifecycle mLifecycle;
    private RequestManager mRequestManager;

    public SupportRequestManagerFragment() {
        mLifecycle = new FragmentLifecycle();
    }


    public FragmentLifecycle getFragmentLifecycle(){
        return mLifecycle;
    }

    public RequestManager getRequestManager(){
        return mRequestManager;
    }

    public void setRequestManager(RequestManager requestManager){
        this.mRequestManager = requestManager;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        mLifecycle.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        mLifecycle.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        mLifecycle.onDestroy();
    }

}
