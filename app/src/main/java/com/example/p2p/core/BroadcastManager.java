package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.p2p.bean.Data;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IBroadcastCallback;
import com.example.p2p.utils.IpUtils;
import com.example.p2p.utils.JsonUtils;
import com.example.p2p.utils.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用UDP广播本地ip地址
 * Created by 陈健宇 at 2019/6/11
 */
public class BroadcastManager {

    private String TAG = BroadcastManager.class.getSimpleName();
    private static BroadcastManager sInstance;
    private static final int TYPE_JOIN_USER = 0x000;
    private static final int TYPE_EXIT_USER = 0x001;

    private ExecutorService mExecutor;
    private int mPort = 9156;
    private DatagramSocket mDatagramSocket;
    private Map<String, User> mOnlineUsers;
    private IBroadcastCallback mBroadcastCallback;
    private volatile boolean isRefresh = true;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TYPE_JOIN_USER:
                    mBroadcastCallback.onJoin((User)msg.obj);
                    break;
                case TYPE_EXIT_USER:
                    mBroadcastCallback.onExit((User)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private BroadcastManager(){
        mExecutor = Executors.newCachedThreadPool();
        mOnlineUsers = new ConcurrentHashMap<>();
    }

    public static BroadcastManager getInstance(){
        if(sInstance == null){
            synchronized (BroadcastManager.class){
                BroadcastManager scan;
                if(sInstance == null){
                    scan = new BroadcastManager();
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
                    //获得发送方的ip地址
                    String receiveIp = datagramPacket.getAddress().getHostAddress();
                    LogUtils.d(TAG, "接收到一个广播，地址 = " + receiveIp);
                    //解析数据
                    Data datas = resolveData(data);
                    int code = datas.getCode();
                    User user = datas.getUser();
                    user.setIp(receiveIp);
                    if(code == 0){
                        //把它加入在线用户列表
                        if(!mOnlineUsers.containsKey(receiveIp)){
                            mOnlineUsers.put(receiveIp, user);
                            if(mBroadcastCallback != null && !isRefresh){
                                mHandler.obtainMessage(TYPE_JOIN_USER, user).sendToTarget();
                            }
                        }
                        //回复它
                        reply(receiveIp);
                    }else if(code == 1){
                        //用户退出在线用户列表
                        mOnlineUsers.remove(receiveIp);
                        if(mBroadcastCallback != null && !isRefresh){
                            mHandler.obtainMessage(TYPE_EXIT_USER, user).sendToTarget();
                        }
                    }else {
                        //把它加入在线用户列表
                        if(!mOnlineUsers.containsKey(receiveIp)) {
                            mOnlineUsers.put(receiveIp, user);
                            if(mBroadcastCallback != null && !isRefresh){
                                mHandler.obtainMessage(TYPE_JOIN_USER, user).sendToTarget();
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
    public void broadcast(Data data){
        String datas = JsonUtils.toJson(data);
        String broadcastAddress = getBroadcastAddress();
        sendAddress(broadcastAddress, datas);
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
           if(mBroadcastCallback != null){
               List<User> list = new ArrayList<>();
               for(User user : mOnlineUsers.values()){
                   list.add(user);
               }
               mBroadcastCallback.onOnlineUsers(list);
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
        mExecutor.execute(() -> {
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
        });
    }

    /**
     * 解析字节流
     * @param data 字节数组
     * @return Data数据类
     */
    private Data resolveData(byte[] data){
        String receiveData = "";
        try{
            InputStream in = new ByteArrayInputStream(data);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int c;
            while ((c = in.read()) != 0) {
                os.write(c);
            }
            receiveData = new String(os.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "解析接收数据时失败，e = " + e.getMessage());
            return new Data(-1);
        }
        Data datas = JsonUtils.toObject(receiveData, Data.class);
        return datas;
    }

    public void setBroadcastCallback(IBroadcastCallback callback){
        this.mBroadcastCallback = callback;
    }
}
