package com.example.p2p.core;

import android.os.Handler;

import com.example.p2p.callback.IBroadcastCallback;
import com.example.p2p.utils.IpUtils;
import com.example.p2p.utils.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

    private ExecutorService mExecutor;
    private int mPort = 9156;
    private DatagramSocket mDatagramSocket;
    private List<String> mOnlineUsers;
    private IBroadcastCallback mBroadcastCallback;

    private BroadcastManager(){
        mExecutor = Executors.newCachedThreadPool();
        mOnlineUsers = new CopyOnWriteArrayList<>();
    }

    public static BroadcastManager getInstance(){
        if(sInstance == null){
            synchronized (PingManager.class){
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
                    StringBuilder receive = new StringBuilder();
                    for(byte b : data){
                        if(b == 0) break;
                        receive.append((char) b);
                    }
                    //获得发送方的ip地址
                    String receiveIp = datagramPacket.getAddress().getHostAddress();
                    if("0".equals(receive.toString())){
                        //把它加入在线用户列表
                        if(!mOnlineUsers.contains(receiveIp)) mOnlineUsers.add(receiveIp);
                        //回复它
                        replylocAddress(receiveIp);
                        if(mBroadcastCallback != null){
                            mBroadcastCallback.onJoin(receiveIp);
                        }
                    }else if("1".equals(receive.toString())){
                        mOnlineUsers.remove(receiveIp);
                        if(mBroadcastCallback != null){
                            mBroadcastCallback.onExit(receiveIp);
                        }
                    }else {
                        //把它加入在线用户列表
                        if(!mOnlineUsers.contains(receiveIp)) mOnlineUsers.add(receiveIp);
                    }
                    LogUtils.d(TAG, "接收到一个广播，地址 = " + receive.toString());
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
    public void sendlocAddress(){
        String broadcastAddress = getBroadcastAddress();
        sendAddress(broadcastAddress, "0");
        LogUtils.d(TAG, "发送本地ip地址");
    }

    /**
     * 广播同一网段下的所有主机，我要退出了
     */
    public void exit(){
        String broadcastAddress = getBroadcastAddress();
        sendAddress(broadcastAddress, "1");
        LogUtils.d(TAG, "广播退出");
    }

    /**
     * 回复本地ip地址给发送方，我知道你加入聊天了
     * @param targetIp 发送方的ip地址
     */
    public void replylocAddress(String targetIp){
        sendAddress(targetIp, "2");
        LogUtils.d(TAG, "回复本地ip地址");
    }


    /**
     * 获取广播地址
     */
    public String getBroadcastAddress() {
        String locAddressPre = IpUtils.getLocIpAddressPrefix();
        return locAddressPre + "225";
    }

    /**
     * 返回在线用户列表
     */
    public List<String> getOnlineUsers() {
        if (mOnlineUsers == null) {
            return new ArrayList<>();
        }
        return mOnlineUsers;
    }

    public void setBroadcastCallback(IBroadcastCallback callback){
        this.mBroadcastCallback = callback;
    }

    /**
     * 发送带code字段的UDP数据包
     * @param targetIp 发送方的ip地址
     * @param code 字段，0表示需要回复，1表示退出, 2代表回复
     */
    private void sendAddress(String targetIp, String code){
        mExecutor.execute(() -> {
            DatagramSocket datagramSocket = null;
            try {
                //创建DatagramSocket类对象，此类表示用来发送和接收数据报包的套接字
                datagramSocket = new DatagramSocket();
                //创建要发送的数据，这里是code字段
                byte[] data = code.getBytes();
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
}
