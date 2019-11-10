package com.example.myglide.utils;

/**
 * log工具类
 * Create by 陈健宇 at 2018/8/23
 */
public class Log{

    private static final boolean isDebug = true;

    // 下面是传入自定义tag的函数
    public static void i(String tag, String msg)
    {
        if (isDebug)
            android.util.Log.i(tag, msg);
    }

    public static void d(String tag, String msg)
    {
        if (isDebug)
            android.util.Log.d(tag, msg);
    }

    public static void e(String tag, String msg)
    {
        if (isDebug)
            android.util.Log.e(tag, msg);
    }

    public static void v(String tag, String msg)
    {
        if (isDebug)
            android.util.Log.v(tag, msg);
    }
}
