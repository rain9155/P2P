package com.example.p2p.utils;

import android.app.Activity;
import android.content.Intent;

/**
 * 打开Wifi的工具类
 * Created by 陈健宇 at 2019/6/7
 */
public class WifiUtils {

    /**
     * 跳转到wifi设置界面，带请求码
     */
    public static void gotoWifiSettings(Activity activity, int requestCode){
        Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
        activity.startActivityForResult(wifiSettingsIntent, requestCode);
    }

}
