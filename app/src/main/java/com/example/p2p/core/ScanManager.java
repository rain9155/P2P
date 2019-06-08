package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.p2p.app.App;
import com.example.p2p.callback.IScanCallback;
import com.example.p2p.utils.IpUtils;
import com.example.p2p.utils.LogUtils;
import com.example.p2p.utils.WifiUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 扫描获得同一个局域网下的所有ip地址
 * Created by 陈健宇 at 2019/6/6
 */
public class ScanManager {

    private String TAG = ScanManager.class.getSimpleName();
    private static ScanManager sInstance;
    private static final int TYPE_EMPTY = 0x000;
    private static final int TYPE_SUCCESS = 0x001;
    private Runtime mRuntime;
    private List<String> mPingSuccessList;
    private ExecutorService mExecutor;
    private CountDownLatch mCountDownLatch;
    private IScanCallback mScanCallback;

    private Handler mHandler  = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TYPE_EMPTY:
                    mScanCallback.onScanEmpty();
                    break;
                case TYPE_SUCCESS:
                    mScanCallback.onScanSuccess(mPingSuccessList);
                    break;
                default:
                    break;
            }
        }
    };

    private ScanManager(){
        mRuntime = Runtime.getRuntime();
        mExecutor = new ThreadPoolExecutor(
                mRuntime.availableProcessors() * 2,
                100,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(200));
        mPingSuccessList = new CopyOnWriteArrayList<>();
        mCountDownLatch = new CountDownLatch(0);

    }

    public static ScanManager getInstance(){
        if(sInstance == null){
            synchronized (ScanManager.class){
                ScanManager ping;
                if(sInstance == null){
                    ping = new ScanManager();
                    sInstance = ping;
                }
            }
        }
        return sInstance;
    }

    /**
     * 枚举的ping后缀1 ~ 255 的本局域网内的ip地址
     */
    public void startScan(){
        if(isScanning()) return;
        if(!WifiUtils.isWifiConnected(App.getContext())){
            if(mScanCallback != null){
                mScanCallback.onScanError();
            }
            return;
        }
        mPingSuccessList.clear();
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
                LogUtils.d(TAG, Thread.currentThread().getName());
            });
        }
        waitForResult();
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
            String pingArgs = "ping -c 1 -w 1 ";
            process = mRuntime.exec(pingArgs + ipAddress);
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
     * 是否正在扫描中
     * @return true表示是，false反之
     */
    public boolean isScanning(){
        return mCountDownLatch.getCount() > 0;
    }

    /**
     * 是否关闭
     * @return true表示是，false反之
     */
    public boolean isClose(){
        return mExecutor.isTerminated();
    }

    /**
     * 关闭扫描过程
     */
    public void close(){
        mExecutor.shutdownNow();
    }

    /**
     * 等待获得ping成功的ip地址列表
     */
    private void waitForResult() {
        new Thread(() -> {
            try {
                mCountDownLatch.await();
            } catch (InterruptedException e) {
                LogUtils.d(TAG, "等待ping任务执行完毕出错， e = " + e.getMessage());
                e.printStackTrace();
            }
            if(mScanCallback != null){
                if(mPingSuccessList.isEmpty()){
                    mHandler.obtainMessage(TYPE_EMPTY).sendToTarget();
                }else {
                    mHandler.obtainMessage(TYPE_SUCCESS).sendToTarget();
                }
            }
        }).start();
    }

    public void setScanCallback(IScanCallback callback){
        this.mScanCallback = callback;
    }
}
