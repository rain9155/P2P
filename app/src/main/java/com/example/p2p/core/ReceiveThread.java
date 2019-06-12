package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.p2p.callback.IReceiveMessageCallback;
import com.example.p2p.utils.LogUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by 陈健宇 at 2019/6/9
 */
public class ReceiveThread implements Runnable{

    private static final String TAG = ReceiveThread.class.getSimpleName();
    private static final int TYPE_RECEVICE_SUCCESS = 0x000;
    private static final int TYPE_RECEVICE_FAIL = 0x001;

    private Socket mSocket;
    private String mClientIp;
    private List<String> mMessageList;
    private volatile IReceiveMessageCallback mReceiveMessageCallback;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TYPE_RECEVICE_SUCCESS:
                    mReceiveMessageCallback.onReceiveSuccess((String)msg.obj);
                    break;
                case TYPE_RECEVICE_FAIL:
                    mReceiveMessageCallback.onReceiveFail((String)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    public ReceiveThread(Socket socket) {
        this.mSocket = socket;
        this.mClientIp = socket.getInetAddress().getHostAddress();
        this.mMessageList = new CopyOnWriteArrayList<>();
    }

    @Override
    public void run() {
        while (true){
            String message = "";
            try{
                //获取输入流，读取客户端发送过来的消息
                InputStream in = mSocket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(in);
                message = dataInputStream.readUTF();
                LogUtils.d(TAG, "收到来自客户端的信息，message = " + message);
                //用一个列表暂时保存信息
                //mMessageList.add(message);
                if(hasReceviceCallback(mClientIp)){
                    mHandler.obtainMessage(TYPE_RECEVICE_SUCCESS, message).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "获取客户端输入流失败, 连接断开，e = " + e.getMessage());
                ConnectManager.getInstance().removeConnect(mClientIp);
                ConnectManager.getInstance().removeReceiveCallback(mClientIp);
                if(hasReceviceCallback(mClientIp))
                    mHandler.obtainMessage(TYPE_RECEVICE_FAIL, message).sendToTarget();
                break;
            }
        }
    }

    /**
     * 获得暂存的信息列表
     */
    public List<String> getMessageList() {
        if (mMessageList == null) {
            return new ArrayList<>();
        }
        return mMessageList;
    }

    /**
     * 清空信息列表
     */
    public void clearMessageList() {
        mMessageList.clear();
    }

    /**
     * 判断是否有设置回调
     * @param targetIp 客户端ip
     * @return true表示有，false表示没有
     */
    public boolean hasReceviceCallback(String targetIp){
        if(mReceiveMessageCallback == null){
            IReceiveMessageCallback receiveMessageCallback = ConnectManager.getInstance().getReceiveCallback(targetIp);
            if(receiveMessageCallback == null)
                return false;
            mReceiveMessageCallback = receiveMessageCallback;
        }
        return true;
    }

}
