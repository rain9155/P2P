package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;

import com.example.p2p.callback.IConnectCallback;
import com.example.p2p.utils.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * 管理着每个客户端的连接状态，控制和客户端的连接
 * Created by 陈健宇 at 2019/6/9
 */
public class ConnectManager {

    private static final String TAG = ConnectManager.class.getSimpleName();
    private static final int PORT = 9155;
    private static ConnectManager sInstance;
    private static final ExecutorService EXECUTOR;

    private ServerSocket mServerSocket;
    private Map<String, Socket> mClients;//保存每个Socket连接到ip地址的映射
    private Map<String, ScheduledFuture> mScheduledTasks;
    private ScheduledExecutorService mScheduledExecutor;
    private Runtime mRuntime;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    static {
        ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();
        EXECUTOR = threadPoolExecutor;
    }

    private ConnectManager(){
        mRuntime = Runtime.getRuntime();
        mClients = new ConcurrentHashMap<>();
        mScheduledTasks = new ConcurrentHashMap<>();
        mScheduledExecutor = Executors.newScheduledThreadPool(mRuntime.availableProcessors() * 2);
    }

    public static ConnectManager get(){
        if(sInstance == null){
            synchronized (ConnectManager.class){
                ConnectManager socketManager;
                if(sInstance == null){
                    socketManager = new ConnectManager();
                    sInstance = socketManager;
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化ServerSocket监听，绑定端口号, 等待客户端连接
     */
    public void initListener(){
        execute(() -> {
            try {
                //创建ServerSocket监听，并绑定端口号
                mServerSocket = new ServerSocket(PORT);
                Log.d(TAG, "开启服务端监听，端口号 = " + PORT);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "绑定端口号失败，e = " + e.getMessage());
            }
            while (true){
                try {
                    //调用accept()开始监听，等待客户端的连接
                    Socket socket = mServerSocket.accept();
                    String ipAddress = socket.getInetAddress().getHostAddress();
                    if(isClose(ipAddress)){
                        Log.d(TAG, "一个用户加入聊天，socket = " + socket);
                        //每个客户端连接用一个线程不断的读
                        MessageManager.ReceiveMessageThread receiveThread = new MessageManager.ReceiveMessageThread(socket);
                        //缓存客户端的连接
                        mClients.put(ipAddress, socket);
                        //放到线程池中执行
                        execute(receiveThread);
                        Log.d(TAG, "已连接的客户端数量：" + mClients.size());
                        //简单的心跳机制
                        heartBeat(ipAddress);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "调用accept()监听失败， e = " + e.getMessage());
                    break;
                }
            }
            try {
                //释放掉ServerSocket占用的端口号
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "关闭端口号失败， e = " + e.getMessage());
            }
        });
    }

    /**
     * 根据给定的ip建立Socket连接
     * @param targetIp 客户端ip
     */
    public void connect(String targetIp, IConnectCallback callback){
        if(isContains(targetIp)){
            Log.d(TAG, "客户端连接已经存在");
            if(callback != null){
                callback.onConnectSuccess(targetIp);
            }
            return;
        }
        execute(() -> {
            try {
                Socket socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(targetIp, PORT);
                socket.connect(socketAddress, 5000);
                Log.d(TAG, "连接targetIp = " + targetIp + "成功");
                if(callback != null){
                    mHandler.post(() -> callback.onConnectSuccess(targetIp));
                }
                MessageManager.ReceiveMessageThread receiveThread = new MessageManager.ReceiveMessageThread(socket);
                mClients.put(targetIp, socket);
                execute(receiveThread);
                heartBeat(targetIp);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "连接targetIp = " + targetIp + "失败，e = " + e.getMessage());
                if(callback != null){
                    mHandler.post(() -> callback.onConnectFail(targetIp));
                }
            }
        });
    }


    /**
     * 从定时任务列表中取消一个任务
     */
    public void cancelScheduledTask(String ipAddress){
        ScheduledFuture futureTask = mScheduledTasks.remove(ipAddress);
        if(futureTask != null){
            futureTask.cancel(true);
            Log.d(TAG, "移除一个定时任务， ip = " + ipAddress);
        }
    }

    /**
     * 从客户端集合中移除一个连接
     * @param ipAddress 客户端的ip地址
     */
    public void removeConnect(String ipAddress){
        Socket socket = mClients.remove(ipAddress);
        if(socket != null){
            try {
                socket.close();
                Log.d(TAG, "一个用户退出聊天，socket = " + socket);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "关闭移除的Socket连接出现错误， e = " + e.getMessage());
            }
        }
    }

    /**
     * removeConnect和cancelScheduledTask
     */
    public void remove(String ipAdress){
        removeConnect(ipAdress);
        cancelScheduledTask(ipAdress);
    }

    /**
     * 销毁所有连接
     */
    public void destory(){
        Set<String> ipSet = mClients.keySet();
        for(String ip : ipSet){
            removeConnect(ip);
        }
        Set<String> ipSet2 = mScheduledTasks.keySet();
        for(String ip : ipSet2){
            cancelScheduledTask(ip);
        }
    }

    /**
     * 判断客户端集合是否缓存了该Socket连接
     * @param ipAddress 客户端的ip地址
     * @return true表示有缓存，false反之
     */
    public boolean isContains(String ipAddress){
        return mClients.containsKey(ipAddress);
    }

    /**
     * 根据客户端ip获取相应的连接
     * @param ipAddress 客户端的ip地址
     * @return 对应的Socket连接
     */
    public Socket getSocket(String ipAddress){
        return mClients.get(ipAddress);
    }

    /**
     * 判断指定的ip的连接是否关闭
     * @param ipAddress 客户端的ip地址
     * @return true表示关闭了，false反之
     */
    public boolean isClose(String ipAddress){
        if(!isContains(ipAddress)) return true;
        Socket socket = mClients.get(ipAddress);
        return socket.isClosed();
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
            String pingArgs = "ping -c 1 -w 3 ";
            process = mRuntime.exec(pingArgs + ipAddress);
            exit = process.waitFor();
            if(exit == 0){
                Log.d(TAG, "ping Ip成功， userIp = " + ipAddress);
            }else if(exit == 1){
                Log.d(TAG, "ping Ip失败， userIp = " + ipAddress + ", exit = " + exit);
            }else if(exit == 2){
                Log.d(TAG, "ping Ip失败， userIp = " + ipAddress + ", exit = " + exit);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "等待ping命令返回出错，" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "执行ping命令出错, " + e.getMessage());
        }finally {
            if(process != null) process.destroy();
        }
        return exit;
    }

    /**
     * 简单心跳机制
     */
    private void heartBeat(String ipAddress) {
        if(!mScheduledTasks.containsKey(ipAddress)){
            ScheduledFuture task = mScheduledExecutor.scheduleAtFixedRate(() -> {
                int result = ping(ipAddress);
                Log.d(TAG, "探测对方是否在线, result = " + result + ", ipAddress = " + ipAddress);
                if(result != 0){
                    removeConnect(ipAddress);
                    cancelScheduledTask(ipAddress);
                }
            }, 10, 10, TimeUnit.SECONDS);
            mScheduledTasks.put(ipAddress, task);
        }
    }

    public static void execute(Runnable task){
        EXECUTOR.execute(task);
    }

    public static void submit(FutureTask task){
        EXECUTOR.submit(task);
    }


    public static boolean isClose(){
        return EXECUTOR.isTerminated();
    }

    public static void close(){
        EXECUTOR.shutdownNow();
    }

}
