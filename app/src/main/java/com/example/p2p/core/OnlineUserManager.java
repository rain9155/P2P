package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.p2p.bean.Data;
import com.example.p2p.bean.Image;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.MesType;
import com.example.p2p.bean.User;
import com.example.p2p.callback.ILoginCallback;
import com.example.p2p.callback.IUserCallback;
import com.example.p2p.config.Constant;
import com.example.p2p.utils.FileUtils;
import com.example.p2p.utils.IpUtils;
import com.example.p2p.utils.JsonUtils;
import com.example.p2p.utils.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * 使用UDP广播本地ip地址
 * Created by 陈健宇 at 2019/6/11
 */
public class OnlineUserManager {

    private String TAG = OnlineUserManager.class.getSimpleName();
    private static OnlineUserManager sInstance;
    private static final int TYPE_JOIN_USER = 0x000;
    private static final int TYPE_EXIT_USER = 0x001;
    private static final int TYPE_LOGIN_SUCCESS = 0x002;
    private static final int TYPE_LOGIN_FIAL = 0x003;

    private ExecutorService mExecutor;
    private int mPort = 9156;
    private DatagramSocket mDatagramSocket;
    private Map<String, User> mOnlineUsers;
    private IUserCallback mUserCallback;
    private volatile ILoginCallback mLoginCallback;
    private volatile boolean isRefresh = true;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TYPE_JOIN_USER:
                    mUserCallback.onJoin((User)msg.obj);
                    break;
                case TYPE_EXIT_USER:
                    mUserCallback.onExit((User)msg.obj);
                    break;
                case TYPE_LOGIN_SUCCESS:
                    mLoginCallback.onSuccess();
                    break;
                case TYPE_LOGIN_FIAL:
                    mLoginCallback.onFail();
                    break;
                default:
                    break;
            }
        }
    };

    private OnlineUserManager(){
        mExecutor = Executors.newCachedThreadPool();
        mOnlineUsers = new ConcurrentHashMap<>();
    }

    public static OnlineUserManager getInstance(){
        if(sInstance == null){
            synchronized (OnlineUserManager.class){
                OnlineUserManager scan;
                if(sInstance == null){
                    scan = new OnlineUserManager();
                    sInstance = scan;
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化监听，绑定指定端口, 等待接受广播
     */
    public void initListener(){
        new Thread(() -> {
            try {
                mDatagramSocket = new DatagramSocket(mPort);
                LogUtils.d(TAG, "开启广播监听，端口号 = " + mPort);
            } catch (SocketException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "创建DatagramSocket监听失败， e = " + e.getMessage());
            }
            while (true){
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                    mDatagramSocket.receive(datagramPacket);
                    byte[] data = datagramPacket.getData();
                    LogUtils.d(TAG, "接收长度，len = " + data.length);
                    //获得发送方的ip地址
                    String receiveIp = datagramPacket.getAddress().getHostAddress();
                    LogUtils.d(TAG, "接收到一个广播，地址 = " + receiveIp);
                    //解析数据
                    Data datas = resolveData(data);
                    LogUtils.d(TAG, "接收长度，len = " + data.length);
                    int code = datas.getCode();
                    User user = datas.getUser();
                    user.setIp(receiveIp);
                    if(code == 0){
                        //把它加入在线用户列表
                        if(!mOnlineUsers.containsKey(receiveIp)){
                            mOnlineUsers.put(receiveIp, user);
                            if(mUserCallback != null && !isRefresh){
                                mHandler.obtainMessage(TYPE_JOIN_USER, user).sendToTarget();
                            }
                        }
                        //回复它
                        reply(receiveIp);
                    }else if(code == 1){
                        //用户退出在线用户列表
                        mOnlineUsers.remove(receiveIp);
                        if(mUserCallback != null && !isRefresh){
                            mHandler.obtainMessage(TYPE_EXIT_USER, user).sendToTarget();
                        }
                    }else {
                        //把它加入在线用户列表
                        if(!mOnlineUsers.containsKey(receiveIp)) {
                            mOnlineUsers.put(receiveIp, user);
                            if(mUserCallback != null && !isRefresh){
                                mHandler.obtainMessage(TYPE_JOIN_USER, user).sendToTarget();
                            }
                            if(mLoginCallback != null){
                                mHandler.obtainMessage(TYPE_LOGIN_SUCCESS, receiveIp).sendToTarget();
                            }
                        }
                    }
                    LogUtils.d(TAG, "当前在线用户，count = " + mOnlineUsers.size());
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "接受广播失败， e = " + e.getMessage());
                    break;
                }
            }
            if(mDatagramSocket != null){
                mDatagramSocket.close();
            }
        }).start();
    }

    /**
     * 广播本地ip地址给同一网段下的所有主机, 我要加入聊天
     */
    public void login(User user, byte[] bytes){
        String targetIp = getBroadcastAddress();
        mExecutor.execute(() -> {
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket();
                int start = 0;
                int end = 0;
                int length = bytes.length;
                while (end < length){
                    end += 2048;
                    if(end >= length) end = length;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byteArrayOutputStream.write(bytes, start, end - start);
                    byte[] bytes1 = byteArrayOutputStream.toByteArray();
                    byteArrayOutputStream.close();
                    user.setBytes(bytes1);
                    String datas = JsonUtils.toJson(new Data(0, user));
                    LogUtils.d(TAG, "发送长度，len = " + datas.getBytes().length);
                    DatagramPacket datagramPacket = new DatagramPacket(datas.getBytes(), 0, datas.getBytes().length, InetAddress.getByName(targetIp), mPort);
                    datagramSocket.send(datagramPacket);
                    LogUtils.d(TAG, "发送一段用户数据成功, start = " + start + ", end = " + end + ", len = " + length);
                    start = end;
                    break;
                }
            } catch (SocketException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "创建DatagramSocket失败， e = " + e.getMessage());
            } catch (UnknownHostException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "构造广播地址失败， e = " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "发送一段用户数据失败， e = " + e.getMessage());
            }  finally {
                if(datagramSocket != null) datagramSocket.close();
            }
        });
        //String broadcastAddress = getBroadcastAddress();
        //sendAddress(broadcastAddress, datas);
        LogUtils.d(TAG, "广播本地ip地址");
    }

    /**
     * 广播同一网段下的所有主机，我要退出了
     */
    public void exit(){
        String datas = JsonUtils.toJson(new Data(1));
        String broadcastAddress = getBroadcastAddress();
        sendAddress(broadcastAddress, datas);
        LogUtils.d(TAG, "广播退出");
    }

    /**
     * 回复本地ip地址给发送方，我知道你加入聊天了
     * @param targetIp 发送方的ip地址
     */
    public void reply(String targetIp){
        String datas = JsonUtils.toJson(new Data(2));
        sendAddress(targetIp, datas);
        LogUtils.d(TAG, "回复本地ip地址");
    }

    public void sendUserImage(byte[] bytes){
        String targetIp = getBroadcastAddress();
        mExecutor.execute(() -> {
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket();
                int start = 0;
                int end = 0;
                int length = bytes.length;
                while (end < length){
                    end += 4096;
                    if(end >= length) end = length;
                    DatagramPacket datagramPacket = new DatagramPacket(bytes, start, end - start, InetAddress.getByName(targetIp), mPort);
                    datagramSocket.send(datagramPacket);
                    LogUtils.d(TAG, "发送一段图片数据成功, start = " + start + ", end = " + end + ", len = " + length);
                    start = end;
                    Thread.sleep(500);
                }
            } catch (SocketException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "创建DatagramSocket失败， e = " + e.getMessage());
            } catch (UnknownHostException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "构造广播地址失败， e = " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "发送一段图片数据失败， e = " + e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if(datagramSocket != null) datagramSocket.close();
            }
        });
    }


    /**
     * 获取广播地址
     */
    public String getBroadcastAddress() {
        String locAddressPre = IpUtils.getLocIpAddressPrefix();
        return locAddressPre + "255";
    }

    /**
     * 等待返回在线用户列表
     */
    public void getOnlineUsers() {
        isRefresh = true;
       new Handler().postDelayed(() -> {
           if(mUserCallback != null){
               List<User> list = new ArrayList<>();
               for(User user : mOnlineUsers.values()){
                   list.add(user);
               }
               mUserCallback.onOnlineUsers(list);
           }
           isRefresh = false;
       }, 2000);
    }

    /**
     * 返回在线用户
     */
    public User getOnlineUser(String ip) {
       return mOnlineUsers.get(ip);
    }

    /**
     * 发送带code字段的UDP数据包
     * @param targetIp 发送方的ip地址
     * @param datas 用户信息
     */
    private void sendAddress(String targetIp, String datas){
       final FutureTask<Boolean> futureTask = new FutureTask<Boolean>(() -> {
                    DatagramSocket datagramSocket = null;
                    try {
                        //创建DatagramSocket类对象，此类表示用来发送和接收数据报包的套接字
                        datagramSocket = new DatagramSocket();
                        //创建要发送的数据，这里是code字段
                        byte[] data = datas.getBytes();
                        //构造DatagramPacket，DatagramPacket表示数据包，构造函数表示用来将长度为 length 偏移量为 offset 的包发送到指定主机上的指定端口号
                        DatagramPacket datagramPacket = new DatagramPacket(data, 0, data.length, InetAddress.getByName(targetIp),mPort);
                        //调用send方法发送数据报
                        datagramSocket.send(datagramPacket);
                        LogUtils.d(TAG, "发送一个广播成功");
                        return true;
                    } catch (SocketException e) {
                        e.printStackTrace();
                        LogUtils.e(TAG, "创建DatagramSocket失败， e = " + e.getMessage());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        LogUtils.e(TAG, "构造广播地址失败， e = " + e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        LogUtils.e(TAG, "发送广播失败， e = " + e.getMessage());
                    }finally {
                        //关闭资源
                        if(datagramSocket != null) datagramSocket.close();
                    }
                    return false;
        }){
            @Override
            protected void done() {
                if(mLoginCallback == null) return;
                try {
                    if(this.get()){
                        Thread.sleep(1000);
                        mHandler.obtainMessage(TYPE_LOGIN_SUCCESS).sendToTarget();
                        return;
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.obtainMessage(TYPE_LOGIN_FIAL).sendToTarget();
            }
        };
        mExecutor.submit(futureTask);
    }

    /**
     * 解析字节流
     * @param data 字节数组
     * @return Data数据类
     */
    private Data resolveData(byte[] data){
        String receiveData = "";
        try(
            InputStream in = new ByteArrayInputStream(data);
            ByteArrayOutputStream os = new ByteArrayOutputStream(data.length)
        ){
//            int c;
//            while ((c = in.read()) != 0) {
//                os.write(c);
//            }
            receiveData = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "解析接收数据时失败，e = " + e.getMessage());
            return new Data(-1);
        }
        Data datas = JsonUtils.toObject(receiveData, Data.class);
        return datas;
    }

    public void setUserCallback(IUserCallback callback){
        this.mUserCallback = callback;
    }

    public void setLoginCallback(ILoginCallback callback){
        this.mLoginCallback = callback;
    }
}
