package com.example.p2p.utils;

import android.util.Log;


/**
 * Created by 陈健宇 at 2019/10/12
 */
public class LogUtils {

    private static boolean isDebug = true;

    public static void d(String tag, String msg){
        if(isDebug) Log.d(tag, msg);
    }

    public static void e(String tag, String msg){
        if(isDebug) Log.e(tag, msg);
    }

}
