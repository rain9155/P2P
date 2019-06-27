package com.example.p2p.core;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.p2p.app.App;
import com.example.p2p.bean.Audio;
import com.example.p2p.bean.File;
import com.example.p2p.bean.Image;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.MesType;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IConnectCallback;
import com.example.p2p.callback.IProgressCallback;
import com.example.p2p.callback.IReceiveMessageCallback;
import com.example.p2p.callback.ISendMessgeCallback;
import com.example.p2p.utils.FileUtils;
import com.example.p2p.utils.LogUtils;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 管理着每个客户端的连接状态，控制和客户端的连接
 * Created by 陈健宇 at 2019/6/9
 */
public class ConnectManager {

    private static final String TAG = ConnectManager.class.getSimpleName();
    private static final int PORT = 9155;
    private static final int MAX_SEND_DATA = 10000;//大约10Kb
    private static final int MAX_FILE_SEND_DATA = 45000000;//大约45Mb
    private static final int TYPE_CONNECTION_SUCCESS = 0x000;
    private static final int TYPE_CONNECTION_FAIL = 0x001;
    private static final int TYPE_SEND_SUCCESS = 0x002;
    private static final int TYPE_SEND_FAIL = 0x003;
    private static ConnectManager sInstance;

    private ServerSocket mServerSocket;
    private Map<String, Socket> mClients;//保存每个Socket连接到ip地址的映射
    private Map<String, IReceiveMessageCallback> mReceiveCallbacks;//保存每个消息接受回调到ip地址的映射
    private Map<String, IProgressCallback> mProgressCallbacks;
    private ExecutorService mExecutor;
    private volatile IConnectCallback mConnectCallback;
    private volatile ISendMessgeCallback mSendMessgeCallback;
    private volatile boolean isRelease;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TYPE_CONNECTION_SUCCESS:
                    mConnectCallback.onConnectSuccess((String) msg.obj);
                    break;
                case TYPE_CONNECTION_FAIL:
                    mConnectCallback.onConnectFail((String) msg.obj);
                    break;
                case TYPE_SEND_SUCCESS:
                    mSendMessgeCallback.onSendSuccess((Mes<?>) msg.obj);
                    break;
                case TYPE_SEND_FAIL:
                    mSendMessgeCallback.onSendFail((Mes<?>) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private ConnectManager(){
        mClients = new ConcurrentHashMap<>();
        mReceiveCallbacks = new ConcurrentHashMap<>();
        mProgressCallbacks = new ConcurrentHashMap<>();
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
                mServerSocket = new ServerSocket(PORT);
                LogUtils.d(TAG, "开启服务端监听，端口号 = " + PORT);
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
                        User user = OnlineUserManager.getInstance().getOnlineUser(ipAddress);
                        ReceiveThread receiveThread = new ReceiveThread(socket, user);
                        //缓存客户端的连接
                        mClients.put(ipAddress, socket);
                        LogUtils.d(TAG, "已连接的客户端数量：" + mClients.size());
                        //放到线程池中执行
                        mExecutor.execute(receiveThread);
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
            if(mConnectCallback != null){
                mConnectCallback.onConnectSuccess(targetIp);
            }
            return;
        }
        mExecutor.execute(() -> {
            try {
                Socket socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(targetIp, PORT);
                socket.connect(socketAddress, 5000);
                Log.d(TAG, "连接targetIp = " + targetIp + "成功");
                if(mConnectCallback != null){
                    mHandler.obtainMessage(TYPE_CONNECTION_SUCCESS, targetIp).sendToTarget();
                }
                User user = OnlineUserManager.getInstance().getOnlineUser(targetIp);
                ReceiveThread receiveThread = new ReceiveThread(socket, user);
                mClients.put(targetIp, socket);
                mExecutor.execute(receiveThread);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "连接targetIp = " + targetIp + "失败，e = " + e.getMessage());
                if(mConnectCallback != null){
                    mHandler.obtainMessage(TYPE_CONNECTION_FAIL, targetIp).sendToTarget();
                }
            }
        });
    }

    /**
     * 发送消息给给定ip的客户端
     */
    public void sendMessage(String targetIp, Mes<?> message){
        sendMessage(targetIp, message, null);
    }

    /**
     * 发送消息给给定ip的客户端
     * @param targetIp 客户端的ip
     * @param mes 消息
     * @param callback 进度回调
     */
    public void sendMessage(String targetIp, Mes<?> mes, IProgressCallback callback){
        Mes<?> message = mes.clone();
        if(!isContains(targetIp)){
            LogUtils.d(TAG, "客户端连接已经断开");
            if(mSendMessgeCallback != null){
                mHandler.obtainMessage(TYPE_SEND_FAIL, message).sendToTarget();
            }
            return;
        }
        if(callback != null) mProgressCallbacks.put(targetIp, callback);
        final Socket socket = mClients.get(targetIp);
        mExecutor.execute(() -> {
            try {
                OutputStream os = socket.getOutputStream();
                sendMessageByType(targetIp, os, message);
                Log.d(TAG, "发送消息成功， message = " + message);
                if(mSendMessgeCallback != null){
                    mHandler.obtainMessage(TYPE_SEND_SUCCESS, message).sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "发送消息失败， e = " + e.getMessage());
                if(mSendMessgeCallback != null){
                    mHandler.obtainMessage(TYPE_SEND_FAIL, message).sendToTarget();
                }
            }
        });
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
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.d(TAG, "关闭移除的Socket连接出现错误， e = " + e.getMessage());
            }
        }
    }

    /**
     * 移除指定客户端ip的消息回调
     * @param ipAddress 客户端的ip地址
     */
    public void removeReceiveCallback(String ipAddress){
        mReceiveCallbacks.remove(ipAddress);
    }

    /**
     * 清理一些资源
     */
    public void release(){
        isRelease = true;
        mReceiveCallbacks.clear();
        mProgressCallbacks.clear();
        if(mSendMessgeCallback != null) mSendMessgeCallback = null;
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

    /**
     * 给指定客户端ip添加一个接受消息的回调
     * @param targetIp 客户端ip
     * @param callback 接受消息的回调
     */
    public void addReceiveMessageCallback(String targetIp, IReceiveMessageCallback callback){
        mReceiveCallbacks.put(targetIp, callback);
    }

    /**
     * 获得接受消息回调接口
     */
    public IReceiveMessageCallback getReceiveCallback(String targetIp) {
        return mReceiveCallbacks.get(targetIp);
    }

    /**
     * 执行一个任务
     */
    public void executeTast(Runnable tast){
        mExecutor.execute(tast);
    }

    public void setConnectCallback(IConnectCallback callback){
        this.mConnectCallback = callback;
    }

    public void setSendMessgeCallback(ISendMessgeCallback callback){
        this.mSendMessgeCallback = callback;
    }


    /**
     * 根据消息类型发送消息
     */
    private void sendMessageByType(String targetIp, OutputStream outputStream, Mes<?> message) throws IOException, InterruptedException {
        DataOutputStream os = new DataOutputStream(outputStream);
        MesType type = message.mesType;
        isRelease = false;
        switch (type){
            case TEXT:
                String text = (String)message.data;
                os.writeInt(type.ordinal());
                os.writeUTF(text);
                break;
            case AUDIO:
                Audio audio = (Audio) message.data;
                os.writeInt(type.ordinal());
                os.writeInt(audio.duartion);
                byte[] audioBytes = FileUtils.getFileBytes(audio.audioPath);
                os.writeInt(audioBytes.length);
                os.write(audioBytes);
                break;
            case IMAGE:
                Image image = (Image) message.data;
                byte[] imageBytes = FileUtils.getFileBytes(image.imagePath);
                int imageLen  = imageBytes.length;
                os.writeInt(type.ordinal());
                os.writeInt(imageLen);
                sendBytes(targetIp, os, imageBytes, imageLen);
                image.len = imageLen;
                image.progress = 100;
                break;
            case FILE:
                File file = (File) message.data;
                String filePath = file.filePath;
                InputStream fileIn = new FileInputStream(new java.io.File(filePath));
                int fileLen = fileIn.available();
                fileIn.close();
                os.writeInt(type.ordinal());
                os.writeInt(fileLen);
                os.writeUTF(file.fileType);
                os.writeUTF(file.fileSize);
                os.writeUTF(file.fileName);
                byte[] fileBytes;
                LogUtils.d(TAG, "文件长度，len = " + fileLen);
                if(fileLen < MAX_FILE_SEND_DATA){
                    fileBytes = FileUtils.getFileBytes(filePath);
                    sendBytes(targetIp, os, fileBytes, fileLen);
                }else {//文件太大，分段发送
                    int count = 0;
                    while (count < fileLen){
                        int maxSendFileLen = MAX_FILE_SEND_DATA;
                        if(count + MAX_FILE_SEND_DATA >= fileLen){
                            maxSendFileLen = fileLen - count;
                        }
                        byte[] tempBytes = new byte[maxSendFileLen];
                        try(InputStream in = new BufferedInputStream(new FileInputStream(filePath))){
                            in.read(tempBytes);
                            sendBytes(targetIp, os, tempBytes, maxSendFileLen);
                        }
                        count += maxSendFileLen;
                    }
                }
                file.len = fileLen;
                file.progress = 100;
                break;
            default:
                break;
        }
    }

    private void sendBytes(String targetIp, DataOutputStream os, byte[] bytes, int len) throws IOException {
        int start = 0;
        int end = 0;
        while (end < len){
            if(isRelease) break;//退出聊天后不要传送数据
            end += MAX_SEND_DATA;
            if(end >= len) end = len;
            os.write(bytes, start, end - start);
            LogUtils.d(TAG, "传送数据中，offet = " + (end - start) + ", 长度， len = " + len);
            start = end;
            IProgressCallback callback = mProgressCallbacks.get(targetIp);
            if(callback != null){
                double num = start / (len * 1.0);
                int progress = (int) (num * 100);
                mHandler.post(() -> callback.onProgress(progress));
            }
        }
        mProgressCallbacks.remove(targetIp);
    }
}
