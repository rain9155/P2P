package com.example.p2p.kernel;

import android.app.Activity;

import com.example.p2p.callback.IScanCallback;
import com.example.p2p.utils.IpUtils;
import com.example.p2p.utils.LogUtils;
import com.example.utils.NetWorkUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 扫描获得同一个局域网下的所有ip地址
 * Created by 陈健宇 at 2019/6/6
 */
public class Scan {

    private String TAG = Scan.class.getSimpleName();

    private Runtime mRuntime;
    private String mPingArgs = "ping -c 1 -w 1 ";
    private List<String> mPingSuccessList;
    private ExecutorService mExecutor;
    private CountDownLatch mCountDownLatch;
    private IScanCallback mScanCallback;

    private static Scan sInstance;

    private Scan(){
        mRuntime = Runtime.getRuntime();
        mExecutor = Executors.newCachedThreadPool();
        mPingSuccessList = new CopyOnWriteArrayList<>();
        mCountDownLatch = new CountDownLatch(0);
    }

    public static Scan getInstance(){
        if(sInstance == null){
            synchronized (Scan.class){
                Scan ping;
                if(sInstance == null){
                    ping = new Scan();
                    sInstance = ping;
                }
            }
        }
        return sInstance;
    }

    /**
     * 枚举的ping后缀1 ~ 255 的本局域网内的ip地址
     */
    public void start(Activity activity){
        if(NetWorkUtil.isWifiConnected(activity)){
            if(mScanCallback != null){
                mScanCallback.onScanError();
            }
            return;
        }
        if(mCountDownLatch.getCount() == 0){
            mCountDownLatch = new CountDownLatch(254);
            String locIpAddressPrefix = IpUtils.getLocIpAddressPrefix();
            String locIpAddress = IpUtils.getLocIpAddress();
            for(int i = 1; i <= 255; i++){
                final String ipAddress = locIpAddressPrefix + i;
                if(ipAddress.equals(locIpAddress)) continue;
                mExecutor.execute(() -> {
                    int result = ping(ipAddress);
                    if(result == 0){
                        mPingSuccessList.add(ipAddress);
                    }
                    mCountDownLatch.countDown();
                });
            }
        }
        waitResult(activity);
    }

    /**
     * ping一个ip地址
     * @param ipAddress 要ping的ip地址
     * @return 0表示ping成功，否则失败
     */
    public int ping(String ipAddress){
        int exit = -1;
        Process process = null;
        try{
            process = mRuntime.exec(mPingArgs + ipAddress);
            exit = process.waitFor();
            if(exit == 0){
                LogUtils.d(TAG, "ping Ip成功， ip = " + ipAddress);
            }else if(exit == 1){
                LogUtils.d(TAG, "ping Ip失败， ip = " + ipAddress + ", exit = " + exit);
            }else if(exit == 2){
                LogUtils.d(TAG, "ping Ip失败， ip = " + ipAddress + ", exit = " + exit);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "等待ping命令返回出错，" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "执行ping命令出错, " + e.getMessage());
        }finally {
            if(process != null) process.destroy();
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
     * 等待获得ping成功的ip地址列表
     */
    private void waitResult(Activity activity) {
        new Thread(() -> {
            try {
                mCountDownLatch.await();
            } catch (InterruptedException e) {
                LogUtils.d(TAG, "等待ping任务执行完毕出错， e = " + e.getMessage());
                e.printStackTrace();
            }
            activity.runOnUiThread(() -> {
                if(mScanCallback != null){
                    if(mPingSuccessList.isEmpty()){
                        mScanCallback.onScanEmpty();
                    }else {
                        mScanCallback.onScanSuccess(mPingSuccessList);
                    }
                }
            });
        }).start();
    }

    public void setScanCallback(IScanCallback callback){
        this.mScanCallback = callback;
    }
}
