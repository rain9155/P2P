package com.example.p2p.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.example.p2p.app.App;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 有关Ip地址操作的方法
 * Created by 陈健宇 at 2019/6/6
 */
public class IpUtils {

    private static final String TAG  = IpUtils.class.getSimpleName();

    /**
     * 获得本机ip地址
     */
    public static String getIpAddress(){
        String ip = "";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                //遍历每个网络接口绑定的所以ip地址
                while (addresses.hasMoreElements()){
                    InetAddress address = addresses.nextElement();
                    //当address是IPV4并且不是环回测试地址时
                    if(address instanceof Inet4Address && !address.isLoopbackAddress()){
                        ip = address.getHostAddress();
                        LogUtils.d(TAG,
                                "网卡接口名称 = " + networkInterface.getName() +
                                "ip地址 = " + address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            LogUtils.e(TAG, "获取本地Ip地址失败");
            e.printStackTrace();
        }
        if("".equals(ip)) LogUtils.d(TAG, "获取本地IP地址为空");
        else LogUtils.d(TAG, "获取本地IP地址成功，ip = " + ip);
        return ip;
    }

    /**
     * 获得本机ip地址
     */
    @SuppressLint("DefaultLocale")
    public static String getLocIpAddress(){
        String ip = "0.0.0.0";
        WifiManager wifiManager = (WifiManager) App.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        ip = String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff)
        );
        LogUtils.d(TAG, "获取本机ip地址， ip = " + ip);
        return ip;
    }

    /**
     * 获取本机ip地址的前缀
     */
    public static String getLocIpAddressPrefix(){
        String ipPrefix = getLocIpAddress().substring(0, 12);
        LogUtils.d(TAG, "本机ip地址的前缀为， ipPrefix = " + ipPrefix);
        return ipPrefix;
    }

}
