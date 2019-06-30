package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.p2p.bean.Audio;
import com.example.p2p.bean.Document;
import com.example.p2p.bean.Image;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.MesType;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IConnectCallback;
import com.example.p2p.callback.IProgressCallback;
import com.example.p2p.callback.IReceiveMessageCallback;
import com.example.p2p.callback.ISendMessgeCallback;
import com.example.p2p.callback.IImageReceiveCallback;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 管理着每个客户端的连接状态，控制和客户端的连接
 * Created by 陈健宇 at 2019/6/9
 */
public class ConnectManager {

    private static final String TAG = ConnectManager.class.getSimpleName();
    private static final int PORT = 9155;
    private static final int MAX_SEND_DATA = 30000;//大约30Kb
    private static final int MAX_FILE_SEND_DATA = 45000000;//大约45Mb
    private static final int TYPE_SEND_SUCCESS = 0x002;
    private static final int TYPE_SEND_FAIL = 0x003;
    private static ConnectManager sInstance;

    private ServerSocket mServerSocket;
    private Map<String, Socket> mClients;//保存每个Socket连接到ip地址的映射
    private Map<String, IReceiveMessageCallback> mReceiveCallbacks;//保存每个消息接受回调到ip地址的映射
    private Map<String, IImageReceiveCallback> mImageReceiveCallbacks;//接收用户头像回调
    private Map<String, List<Mes>> mSaveMessages;//暂存消息回调
    private ExecutorService mExecutor;
    private volatile ISendMessgeCallback mSendMessgeCallback;
    private volatile boolean isRelease;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
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
        mImageReceiveCallbacks = new ConcurrentHashMap<>();
        mExecutor = Executors.newCachedThreadPool();
        mSaveMessages = new HashMap<>();
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
                        ReceiveThread receiveThread = new ReceiveThread(socket);
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
    public void connect(String targetIp, IConnectCallback callback){
        if(isContains(targetIp)){
            LogUtils.d(TAG, "客户端连接已经存在");
            if(callback != null){
                callback.onConnectSuccess(targetIp);
            }
            return;
        }
        mExecutor.execute(() -> {
            try {
                Socket socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(targetIp, PORT);
                socket.connect(socketAddress, 5000);
                Log.d(TAG, "连接targetIp = " + targetIp + "成功");
                if(callback != null){
                    mHandler.post(() -> callback.onConnectSuccess(targetIp));
                }
                ReceiveThread receiveThread = new ReceiveThread(socket);
                mClients.put(targetIp, socket);
                mExecutor.execute(receiveThread);
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
     * 发送消息给给定ip的客户端
     */
    public void sendMessage(String targetIp, Mes<?> message){
        sendMessage(targetIp, message, null);
    }

    /**
     * 发送消息给给定ip的客户端
     */
    public void sendMessage(String targetIp, Mes<?> mes, IProgressCallback callback){
        Mes<?> message = mes.clone();
        if(!isContains(targetIp)){
            LogUtils.d(TAG, "客户端连接已经断开");
            //重连
            connect(targetIp, new IConnectCallback() {
                @Override
                public void onConnectSuccess(String targetIp) {
                    sendMessageChecked(targetIp, message, callback);
                }

                @Override
                public void onConnectFail(String targetIp) {
                    if(mSendMessgeCallback != null){
                        mHandler.obtainMessage(TYPE_SEND_FAIL, message).sendToTarget();
                    }
                }
            });
            return;
        }
        sendMessageChecked(targetIp, message, callback);
    }

    /**
     * 检查后发送消息
     * @param targetIp 客户端的ip
     * @param message 消息
     * @param callback 进度回调
     */
    private void sendMessageChecked(String targetIp, Mes<?> message,  IProgressCallback callback) {
        final Socket socket = mClients.get(targetIp);
        mExecutor.execute(() -> {
            try {
                OutputStream os = socket.getOutputStream();
                sendMessageByType(os, message, callback);
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
                LogUtils.d(TAG, "一个用户退出聊天，socket = " + socket);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.d(TAG, "关闭移除的Socket连接出现错误， e = " + e.getMessage());
            }
        }
    }

    /**
     * 清理一些资源
     */
    public void release(){
        isRelease = true;
        mReceiveCallbacks.clear();
        if(mSendMessgeCallback != null) mSendMessgeCallback = null;
    }

    /**
     * 销毁所有连接
     */
    public void destory(){
        release();
        mImageReceiveCallbacks.clear();
        for(String ip : mClients.keySet()){
            removeConnect(ip);
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

    /**
     * 添加一个消息
     */
    public void addMessage(String targetIp, Mes<?> mes){
        List<Mes> list = mSaveMessages.get(targetIp);
        if(list == null){
            list = new ArrayList<>();
            mSaveMessages.put(targetIp, list);
        }
        list.add(mes);
    }

    /**
     * 获取暂存的消息
     */
    public List<Mes> getMessages(String targetIp){
        List<Mes> list = mSaveMessages.get(targetIp);
        if(list != null){
            List<Mes> mesList = new ArrayList<>(list);
            list.clear();
            return mesList;
        } else {
            return new ArrayList<>();
        }
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
     * @param targetIp 指定ip
     */
    public IReceiveMessageCallback getReceiveCallback(String targetIp) {
        return mReceiveCallbacks.get(targetIp);
    }


    /**
     * 移除指定客户端ip的消息回调
     * @param ipAddress 客户端的ip地址
     */
    public void removeReceiveCallback(String ipAddress){
        mReceiveCallbacks.remove(ipAddress);
    }

    /**
     * 给指定客户端ip添加一个接受图片消息的回调
     * @param targetIp 客户端ip
     * @param callback 接受图片消息的回调
     */
    public void addImageReceiveCallback(String targetIp, IImageReceiveCallback callback){
        mImageReceiveCallbacks.put(targetIp, callback);
    }

    /**
     * 获得接收图片消息的回调
     * @param targetIp 指定ip
     */
    public IImageReceiveCallback getImageReceiveCallback(String targetIp){
        return mImageReceiveCallbacks.get(targetIp);
    }

    /**
     * 移除指定客户端ip的图片消息回调
     * @param ipAddress 客户端的ip地址
     */
    public void removeImageReceiveCallback(String ipAddress){
        mImageReceiveCallbacks.remove(ipAddress);
    }

    /**
     * 执行一个任务
     */
    public void executeTast(Runnable tast){
        mExecutor.execute(tast);
    }

    public void setSendMessgeCallback(ISendMessgeCallback callback){
        this.mSendMessgeCallback = callback;
    }


    /**
     * 根据消息类型发送消息
     */
    private void sendMessageByType(OutputStream outputStream, Mes<?> message, IProgressCallback callback) throws IOException {
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
                os.writeInt(message.itemType.ordinal());
                sendBytes(os, imageBytes, imageLen, imageLen, 0, callback);
                image.len = imageLen;
                image.progress = 100;
                break;
            case FILE:
                Document file = (Document) message.data;
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
                if(fileLen < MAX_FILE_SEND_DATA){
                    fileBytes = FileUtils.getFileBytes(filePath);
                    sendBytes(os, fileBytes, fileLen, fileLen, 0, callback);
                }else {//文件太大，分段发送
                    int count = 0;
                    try(InputStream in = new BufferedInputStream(new FileInputStream(filePath))){
                        while (count < fileLen){
                            int maxSendFileLen = MAX_FILE_SEND_DATA;
                            if(count + MAX_FILE_SEND_DATA >= fileLen){
                                maxSendFileLen = fileLen - count;
                            }
                            byte[] tempBytes = new byte[maxSendFileLen];
                            in.read(tempBytes);
                            sendBytes(os, tempBytes, maxSendFileLen, fileLen, count, callback);
                            count += maxSendFileLen;
                        }
                    }
                }
                file.len = fileLen;
                file.progress = 100;
                break;
            default:
                break;
        }
    }

    private void sendBytes(DataOutputStream os, byte[] bytes, int maxSendLen, int fileLen, int preSendLen, IProgressCallback callback) throws IOException {
        int start = 0;
        int end = 0;
        while (end < maxSendLen){
            end += MAX_SEND_DATA;
            if(end >= maxSendLen) end = maxSendLen;
            os.write(bytes, start, end - start);
            LogUtils.d(TAG, "传送数据中，offet = " + (end - start) + ", 长度， len = " + maxSendLen);
            start = end;
            if(callback != null){
                double num = (preSendLen + start) / (fileLen * 1.0);
                int progress = (int) (num * 100);
                mHandler.post(() -> callback.onProgress(progress));
            }
        }
    }
}
