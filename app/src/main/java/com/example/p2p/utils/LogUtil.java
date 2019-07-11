package com.example.p2p.utils;

import android.util.Log;

import com.example.p2p.BuildConfig;

/**
 * log工具
 * Created by 陈健宇 at 2019/6/6
 */
public class LogUtil {

    private static boolean isDebug = BuildConfig.DEBUG;

    public static void d(String tag, String msg){
        if(isDebug) Log.d(tag, msg);
    }

    public static void e(String tag, String msg){
        if(isDebug) Log.e(tag, msg);
    }

}
