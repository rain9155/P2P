package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.p2p.bean.Data;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IUserCallback;
import com.example.p2p.config.Constant;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


/**
 * 使用UDP广播本地ip地址
 * Created by 陈健宇 at 2019/6/11
 */
public class OnlineUserManager {

    private String TAG = OnlineUserManager.class.getSimpleName();
    private static OnlineUserManager sInstance;
    private static final int PORT = 9156;
    private static final int MAX_RECEIVE_DATA = 50000;
    private static final int TYPE_JOIN_USER = 0x000;
    private static final int TYPE_EXIT_USER = 0x001;
    private static final int TYPE_JOIN_USERS = 0x002;

    private ExecutorService mExecutor;
    private DatagramSocket mDatagramSocket;
    private Map<String, User> mOnlineUsers;
    private IUserCallback mUserCallback;
    private volatile boolean isRefresh = true;
    private volatile boolean isSendingImage = false;

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
                case TYPE_JOIN_USERS:
                    mUserCallback.onOnlineUsers((List<User>) msg.obj);
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
                mDatagramSocket = new DatagramSocket(PORT);
                LogUtils.d(TAG, "开启广播监听，端口号 = " + PORT);
            } catch (SocketException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "创建DatagramSocket监听失败， e = " + e.getMessage());
            }
            while (true){
                try {
                    byte[] buffer = new byte[MAX_RECEIVE_DATA];
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                    mDatagramSocket.receive(datagramPacket);
                    byte[] data = datagramPacket.getData();
                    //获得发送方的ip地址
                    String receiveIp = datagramPacket.getAddress().getHostAddress();
                    //解析数据
                    Data datas = resolveData(data);
                    if(datas != null){
                        //用户数据
                        int code = datas.getCode();
                        User user = datas.getUser();
                        user.setIp(receiveIp);
                        if(code == 0){
                            //把它加入在线用户列表
                            if(!mOnlineUsers.containsKey(receiveIp)){
                                mOnlineUsers.put(receiveIp, user);
                                //通知主活动用用户加入
                                if(mUserCallback != null && !isRefresh){
                                    mHandler.obtainMessage(TYPE_JOIN_USER, mOnlineUsers.get(receiveIp)).sendToTarget();
                                }
                                LogUtils.d(TAG, "一个用户加入，地址 = " + receiveIp);
                            }
                            //回复它
                            reply(receiveIp);
                        }else if(code == 1){
                            //用户退出在线用户列表
                            if(mOnlineUsers.containsKey(receiveIp)){
                                User exitUser = mOnlineUsers.remove(receiveIp);
                                if(mUserCallback != null && !isRefresh){
                                    mHandler.obtainMessage(TYPE_EXIT_USER, exitUser).sendToTarget();
                                }
                                LogUtils.d(TAG, "一个用户退出，地址 = " + receiveIp);
                            }

                        }else {
                            //得到所有在线用户列表
                            if(!mOnlineUsers.containsKey(receiveIp)) {
                                mOnlineUsers.put(receiveIp, user);
                                LogUtils.d(TAG, "获得一个用户信息，地址 = " + receiveIp);
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
    public void login(User user){
        String broadcastAddress = getBroadcastAddress();
        String datas = JsonUtils.toJson(new Data(0, user));
        sendAddress(broadcastAddress, datas);
        LogUtils.d(TAG, "广播本地ip地址成功");
    }

    /**
     * 广播同一网段下的所有主机，我要退出了
     */
    public void exit(){
        String datas = JsonUtils.toJson(new Data(1));
        String broadcastAddress = getBroadcastAddress();
        sendAddress(broadcastAddress, datas);
        mOnlineUsers.clear();
        if(mDatagramSocket != null) mDatagramSocket.close();
        LogUtils.d(TAG, "广播退出");
    }

    /**
     * 回复本地ip地址给发送方，我知道你加入聊天了
     * @param targetIp 发送方的ip地址
     */
    public void reply(String targetIp){
        Data data = new Data(2);
        String datas = JsonUtils.toJson(data);
        sendAddress(targetIp, datas);
        LogUtils.d(TAG, "回复本地ip地址成功");
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
        mExecutor.execute(() -> {
            try {
                Thread.sleep(Constant.WAITING_TIME);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            if(mUserCallback != null){
                List<User> list = new ArrayList<>();
                for(User user : mOnlineUsers.values()){
                    list.add(user);
                }
                mHandler.obtainMessage(TYPE_JOIN_USERS, list).sendToTarget();
            }
            isRefresh = false;
        });
    }

    /**
     * 返回在线用户
     */
    public User getOnlineUser(String ip) {
       return mOnlineUsers.get(ip);
    }

    /**
     * 是否正在刷新
     */
    public boolean isRefresh(){
        return isRefresh;
    }

    /**
     * 发送带code字段的UDP数据包
     * @param targetIp 发送方的ip地址
     * @param datas 用户信息
     */
    private void sendAddress(String targetIp, String datas){
       final FutureTask<Boolean> futureTask = new FutureTask<>(() -> {
                    DatagramSocket datagramSocket = null;
                    try {
                        //创建DatagramSocket类对象，此类表示用来发送和接收数据报包的套接字
                        datagramSocket = new DatagramSocket();
                        //创建要发送的数据，这里是code字段
                        byte[] data = datas.getBytes();
                        //构造DatagramPacket，DatagramPacket表示数据包，构造函数表示用来将长度为 length 偏移量为 offset 的包发送到指定主机上的指定端口号
                        DatagramPacket datagramPacket = new DatagramPacket(data, 0, data.length, InetAddress.getByName(targetIp), PORT);
                        //调用send方法发送数据报
                        datagramSocket.send(datagramPacket);
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
        });
        mExecutor.submit(futureTask);
    }

    /**
     * 发送图片数据
     */
    private void sendUserWithImage(byte[] bytes, String targetIp) {
        isSendingImage = true;
        mExecutor.execute(() -> {
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket();
                int start = 0;
                int end = 0;
                int length = bytes.length;
                while (end < length){
                    if(!isSendingImage) break;//退出后停止发送
                    end += MAX_RECEIVE_DATA;
                    if(end >= length) end = length;
                    DatagramPacket datagramPacket = new DatagramPacket(bytes, start, end - start, InetAddress.getByName(targetIp), PORT);
                    datagramSocket.send(datagramPacket);
                    LogUtils.d(TAG, "发送一段图片数据成功, offet = " + (end - start) + ", 总长度， len = " + length);
                    start = end;
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
            }finally {
                if(datagramSocket != null) datagramSocket.close();
            }
        });
    }

    /**
     * 解析字节流
     */
    private Data resolveData(byte[] data) throws IOException {
        String receiveDatas = "";
        try(
            InputStream in = new ByteArrayInputStream(data);
            ByteArrayOutputStream os = new ByteArrayOutputStream(data.length)
        ){
            int c;
            while ((c = in.read()) != 0) {
                os.write(c);
            }
            receiveDatas = new String(os.toByteArray());
        }
        try {
            Data datas = JsonUtils.toObject(receiveDatas, Data.class);
            return datas;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void setUserCallback(IUserCallback callback){
        this.mUserCallback = callback;
    }

}
