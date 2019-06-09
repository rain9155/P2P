package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.p2p.callback.IConnectCallback;
import com.example.p2p.utils.LogUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 管理着每个客户端的连接状态，控制和客户端的连接
 * Created by 陈健宇 at 2019/6/9
 */
public class ConnectManager {

    private static final String TAG = ConnectManager.class.getSimpleName();
    private static ConnectManager sInstance;
    private static final int TYPE_CONNECTION_SUCCESS = 0x000;
    private static final int TYPE_CONNECTION_FAIL = 0x001;

    private ServerSocket mServerSocket;
    private Map<String, Socket> mClients;//保存每个Socket连接到ip地址的映射
    private ExecutorService mExecutor;
    private int mPort = 9155;
    private IConnectCallback mCallback;



    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            String targetIp = (String) msg.obj;
            switch (msg.what){
                case TYPE_CONNECTION_SUCCESS:
                    mCallback.onConnectSuccess(targetIp);
                    break;
                case TYPE_CONNECTION_FAIL:
                    mCallback.onConnectFail(targetIp);
                    break;
                default:
                    break;
            }
        }
    };

    private ConnectManager(){
        mClients = new HashMap<>();
        mExecutor = Executors.newCachedThreadPool();
    }

    public static ConnectManager getInstance(){
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
        new Thread(() -> {
            try {
                //创建ServerSocket监听，并绑定端口号
                mServerSocket = new ServerSocket(mPort);
                LogUtils.d(TAG, "绑定端口号，port = " + mPort);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "绑定端口号失败，e = " + e.getMessage());
            }
            while (true){
                try {
                    //调用accept()开始监听，等待客户端的连接
                    Socket socket = mServerSocket.accept();
                    String ipAddress = socket.getInetAddress().getHostAddress();
                    if(isClose(ipAddress)){
                        LogUtils.d(TAG, "一个用户加入聊天，socket = " + socket);
                        //每个客户端连接用一个线程不断的读
                        ReadWriteThread readWriteThread = new ReadWriteThread(socket);
                        //缓存客户端的连接
                        mClients.put(ipAddress, socket);
                        LogUtils.d(TAG, "已连接的客户端数量：" + mClients.size());
                        //放到线程池中执行
                        mExecutor.execute(readWriteThread);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "调用accept()监听失败， e = " + e.getMessage());
                    break;
                }
            }
            try {
                //释放掉ServerSocket占用的端口号
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "关闭端口号失败， e = " + e.getMessage());
            }
        }).start();
    }

    /**
     * 根据给定的ip建立Socket连接
     * @param targetIp 客户端ip
     * @return true表示成功建立连接
     */
    public void connect(String targetIp){
        if(isContains(targetIp)){
            LogUtils.d(TAG, "客户端连接已经存在");
            return;
        }
        mExecutor.execute(() -> {
            try {
                Socket socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(targetIp, mPort);
                socket.connect(socketAddress, 5000);
                Log.d(TAG, "连接targetIp = " + targetIp + "成功");
                if(mCallback != null){
                    mHandler.obtainMessage(TYPE_CONNECTION_SUCCESS, targetIp).sendToTarget();
                }
                mClients.put(targetIp, socket);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "连接targetIp = " + targetIp + "失败，e = " + e.getMessage());
                if(mCallback != null){
                    mHandler.obtainMessage(TYPE_CONNECTION_FAIL, targetIp).sendToTarget();
                }
            }
        });
    }

    /**
     * 放入一个连接到客户端集合中
     * @param ipAddress 客户端的ip地址
     * @param socket socket连接
     */
    public void put(String ipAddress, Socket socket){
        if(mClients.containsKey(ipAddress)) return;
        mClients.put(ipAddress, socket);
    }

    /**
     * 从客户端集合中移除一个连接
     * @param ipAddress 客户端的ip地址
     */
    public void remove(String ipAddress){
        Socket socket = mClients.remove(ipAddress);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.d(TAG, "关闭移除的Socket连接出现错误， e = " + e.getMessage());
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
     * 判断指定的ip的连接是否关闭
     * @param ipAddress 客户端的ip地址
     * @return true表示关闭了，false反之
     */
    public boolean isClose(String ipAddress){
        if(!isContains(ipAddress)) return true;
        Socket socket = mClients.get(ipAddress);
        return socket.isClosed();
    }

    public void setConnectCallback(IConnectCallback callback){
        this.mCallback = callback;
    }
}
