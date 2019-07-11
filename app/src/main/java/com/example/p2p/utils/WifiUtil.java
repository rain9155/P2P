package com.example.p2p.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * 打开Wifi的工具类
 * Created by 陈健宇 at 2019/6/7
 */
public class WifiUtil {

    /**
     * 跳转到wifi设置界面
     */
    public static void gotoWifiSettings(Activity activity){
        gotoWifiSettings(activity, 0x9155);
    }

    /**
     * 跳转到wifi设置界面，带请求码
     */
    public static void gotoWifiSettings(Activity activity, int requestCode){
        Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
        activity.startActivityForResult(wifiSettingsIntent, requestCode);
    }

    /**
     * 检查WIFI是否打开
     */
    public static boolean isWifiEnable(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    /**
     * 检查WIFI是否连接
     */
    public static boolean isWifiConnected(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiInfo != null && wifiInfo.isConnected();
    }

}
