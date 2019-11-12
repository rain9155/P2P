package com.example.p2p.utils;



/**
 * Created by 陈健宇 at 2019/10/12
 */
public class Log {

    private static boolean isDebug = true;

    public static void d(String tag, String msg){
        if(isDebug) android.util.Log.d(tag, msg);
    }

    public static void e(String tag, String msg){
        if(isDebug) android.util.Log.e(tag, msg);
    }

}
