package com.example.p2p.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.p2p.app.App;
import com.example.p2p.bean.Data;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IUserCallback;
import com.example.p2p.config.Constant;
import com.example.p2p.utils.FileUtils;
import com.example.p2p.utils.IpUtils;
import com.example.p2p.utils.JsonUtils;
import com.example.p2p.utils.LogUtils;
import com.example.utils.FileUtil;

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
import java.util.concurrent.ExecutionException;
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
    private static final int MAX_RECEIVE_DATA = 300000;
    private static final int TYPE_JOIN_USER = 0x000;
    private static final int TYPE_EXIT_USER = 0x001;

    private ExecutorService mExecutor;
    private DatagramSocket mDatagramSocket;
    private Map<String, User> mOnlineUsers;
    private Map<String, ByteArrayOutputStream> mReceiveImagesBytes;
    private Map<String, ByteArrayOutputStream> mReceive2ImagesBytes;
    private IUserCallback mUserCallback;
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
                default:
                    break;
            }
        }
    };

    private OnlineUserManager(){
        mExecutor = Executors.newCachedThreadPool();
        mOnlineUsers = new ConcurrentHashMap<>();
        mReceiveImagesBytes = new ConcurrentHashMap<>();
        mReceive2ImagesBytes = new ConcurrentHashMap<>();
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
                            mReceiveImagesBytes.put(receiveIp, new ByteArrayOutputStream());
                            mReceiveImagesBytes.get(receiveIp).write(user.getBytes(), 0, user.getBytes().length);
                            if(mUserCallback != null && !isRefresh){
                                mHandler.obtainMessage(TYPE_JOIN_USER, user).sendToTarget();
                            }
                            //回复它
                            reply(receiveIp);
                        }else{
                            //读取图片流
                            ByteArrayOutputStream os = mReceiveImagesBytes.get(receiveIp);
                            os.write(user.getBytes(), 0, user.getBytes().length);
                            byte[] imageBytes = os.toByteArray();
                            if(imageBytes.length >= user.getImageBytesLen()){
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                FileUtils.saveOnlineUserBitmap(bitmap, receiveIp);
                                os.close();
                                mReceiveImagesBytes.remove(receiveIp);
                                LogUtils.d(TAG, "收到一个图片，receiveIp = " + receiveIp + ", 长度，len = " + os.toByteArray().length);
                            }
                        }
                    }else if(code == 1){
                        //用户退出在线用户列表
                        mOnlineUsers.remove(receiveIp);
                        if(mUserCallback != null && !isRefresh){
                            mHandler.obtainMessage(TYPE_EXIT_USER, user).sendToTarget();
                        }
                    }else {
                        if(!mReceive2ImagesBytes.containsKey(receiveIp)){
                            mReceive2ImagesBytes.put(receiveIp, new ByteArrayOutputStream());
                        }
                        //把它加入在线用户列表
                        if(!mOnlineUsers.containsKey(receiveIp)) {
                            mOnlineUsers.put(receiveIp, user);
                            if(mUserCallback != null && !isRefresh){
                                mHandler.obtainMessage(TYPE_JOIN_USER, user).sendToTarget();
                            }
                        }else {
                            ByteArrayOutputStream os = mReceive2ImagesBytes.get(receiveIp);
                            os.write(user.getBytes(), 0, user.getBytes().length);
                            byte[] imageBytes = os.toByteArray();
                            if(imageBytes.length >= user.getImageBytesLen()){
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                FileUtils.saveOnlineUserBitmap(bitmap, receiveIp);
                                os.close();
                                mReceive2ImagesBytes.remove(receiveIp);
                                LogUtils.d(TAG, "收到一个图片，receiveIp = " + receiveIp + ", 长度，len = " + os.toByteArray().length);
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
        String broadcastAddress = getBroadcastAddress();
        sendUserWithImage(user, bytes, broadcastAddress, 0);
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
        User user = (User)FileUtil.restoreObject(App.getContext(), Constant.FILE_NAME_USER);
        byte[] imageBytes = FileUtils.getImageBytes(user.getImagePath());
        user.setImageBytesLen(imageBytes.length);
        sendUserWithImage(user, imageBytes, targetIp, 2);
        LogUtils.d(TAG, "回复本地ip地址");
    }

    private void sendUserWithImage(User user, byte[] bytes, String targetIp, int code) {
        mExecutor.execute(() -> {
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket();
                int start = 0;
                int end = 0;
                int length = bytes.length;
                while (end < length){
                    end += 4094;
                    if(end >= length) end = length;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byteArrayOutputStream.write(bytes, start, end - start);
                    user.setBytes(byteArrayOutputStream.toByteArray());
                    byteArrayOutputStream.close();
                    String datas = JsonUtils.toJson(new Data(code, user));
                    byte[] sendData = datas.getBytes();
                    LogUtils.d(TAG, "发送长度，len = " + sendData.length);
                    DatagramPacket datagramPacket = new DatagramPacket(sendData, 0, sendData.length, InetAddress.getByName(targetIp), PORT);
                    datagramSocket.send(datagramPacket);
                    LogUtils.d(TAG, "发送一段用户数据成功, offet = " + (end - start) + ", len = " + length);
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
                LogUtils.e(TAG, "发送一段用户数据失败， e = " + e.getMessage());
            }  finally {
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
       }, 3000);
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
        });
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

    public void setUserCallback(IUserCallback callback){
        this.mUserCallback = callback;
    }

}
