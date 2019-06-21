package com.example.p2p.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

/**
 * 手机震动帮助类，记得添加权限：<uses-permission android:userIp="android.permission.VIBRATE"/>
 * Created by 陈健宇 at 2019/6/13
 */
public class VibrateUtils {

    /**
     * 震动milliseconds毫秒结束
     * @param milliseconds 毫秒
     */
    public static void Vibrate(final Context context, long milliseconds) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    /**
     * 自定义震动模式
     * @param pattern 数组中数字的含义依次是[静止时长，震动时长，静止时长，震动时长, ...], 时长的单位是毫秒
     * @param isRepeat 是否反复震动，如果是true，反复震动，如果是false，只震动一次
     */
    public static void Vibrate(final Context context, long[] pattern, boolean isRepeat) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, isRepeat ? 1 : -1);
    }

}
