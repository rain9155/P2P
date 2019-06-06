package com.example.p2p.kernel;

import com.example.p2p.utils.IpUtils;
import com.example.p2p.utils.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 扫描获得同一个局域网下的所有ip地址
 * Created by 陈健宇 at 2019/6/6
 */
public class Ping {

    private String TAG = Ping.class.getSimpleName();

    private Runtime mRuntime;
    private String mPingArgs = "ping -c 3 -w 3";//ping尝试3次， 超时时间3毫秒
    private CopyOnWriteArrayList<String> mPingSuccessList;
    private ExecutorService mExecutor;
    private static Ping sInstance;

    private Ping(){
        mExecutor = Executors.newCachedThreadPool();
        mRuntime = Runtime.getRuntime();
        mPingSuccessList = new CopyOnWriteArrayList<>();
    }

    public static Ping getInstance(){
        if(sInstance == null){
            synchronized (Ping.class){
                Ping ping;
                if(sInstance == null){
                    ping = new Ping();
                    sInstance = ping;
                }
            }
        }
        return sInstance;
    }

    /**
     * 枚举的ping后缀1 ~ 255 的本局域网内的ip地址
     */
    public void start(){
        String locIpAddressPrefix = IpUtils.getLocIpAddressPrefix();
        String locIpAddress = IpUtils.getLocIpAddress();
        for(int i = 1; i <= 255; i++){
            final String ipAddress = locIpAddressPrefix + i;
            if(!ipAddress.equals(locIpAddress)){
                mExecutor.execute(() -> {
                    int result = ping(ipAddress);
                    if(result == 0){
                        mPingSuccessList.add(ipAddress);
                        LogUtils.d(TAG, "ping Ip成功， ip = " + ipAddress);
                    }else {
                        LogUtils.d(TAG, "ping Ip失败， ip = " + ipAddress);
                    }
                });
            }
        }
    }

    /**
     * ping一个ip地址
     * @param ipAddress 要ping的ip地址
     * @return 0表示ping成功，否则失败
     */
    public int ping(String ipAddress){
        int exit = -1;
        try {
            Process process = mRuntime.exec(mPingArgs + ipAddress);
            process.waitFor();
            exit = process.exitValue();
        } catch (InterruptedException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "等待ping命令返回出错");
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "执行ping命令出错");
        }
        return exit;
    }

    /**
     * 关闭ping过程
     */
    public void close(){

        mExecutor.shutdownNow();
    }

    /**
     * ping过程是否结束
     * @return true表示结束，false反之
     */
    public boolean isClose(){
        return mExecutor.isTerminated();
    }


    /**
     * 获得ping成功的ip地址列表
     */
    public CopyOnWriteArrayList<String> getPingSuccessList() {
        return mPingSuccessList;
    }
}
