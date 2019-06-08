package com.example.p2p.core;

import com.example.p2p.utils.LogUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 陈健宇 at 2019/6/9
 */
public class SocketManager {

    private static final String TAG = SocketManager.class.getSimpleName();
    private static SocketManager sInstance;
    private ServerSocket mServerSocket;
    private Map<Socket, ReadWriteThread> mClients;
    private ExecutorService mExecutor;

    private SocketManager(){
        mClients = new HashMap<>();
        mExecutor = Executors.newCachedThreadPool();
    }

    public static SocketManager getInstance(){
        if(sInstance == null){
            synchronized (SocketManager.class){
                SocketManager socketManager;
                if(sInstance == null){
                    socketManager = new SocketManager();
                    sInstance = socketManager;
                }
            }
        }
        return sInstance;
    }

    public void initListener(){
        try {
            //创建ServerSocket监听，并绑定端口号
            mServerSocket = new ServerSocket(9155);
            //调用accept()开始监听，等待客户端的连接
            Socket socket = mServerSocket.accept();
            LogUtils.d(TAG, "一个用户加入聊天，socket = " + socket);
            //每个客户端连接用一个线程不断的读
            ReadWriteThread readWriteThread = new ReadWriteThread(socket);
            //缓存客户端的连接
            mClients.put(socket, readWriteThread);
            //放到线程池中执行
            mExecutor.execute(readWriteThread);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "初始化ServerSocket监听失败，e = " + e.getMessage());
        }
    }

}
