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
import java.util.Stack;

/**
 * Created by 陈健宇 at 2019/6/9
 */
public class ReceiveThread implements Runnable{

    private static final String TAG = ReceiveThread.class.getSimpleName();
    private static final int TYPE_RECEVICE_SUCCESS = 0x000;
    private static final int TYPE_RECEVICE_FAIL = 0x001;

    private Socket mSocket;
    private String mClientIp;
    private IReceiveMessageCallback mReceiveMessageCallback;

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
                if(mReceiveMessageCallback != null){
                    mHandler.obtainMessage(TYPE_RECEVICE_SUCCESS, message).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
                ConnectManager.getInstance().remove(mClientIp);
                LogUtils.e(TAG, "获取客户端输入流失败, 连接断开，e = " + e.getMessage());
                if(mReceiveMessageCallback != null){
                    mHandler.obtainMessage(TYPE_RECEVICE_FAIL, message).sendToTarget();
                }
                break;
            }
        }
    }

    public void setReceiveMessageCallback(IReceiveMessageCallback callback){
        this.mReceiveMessageCallback = callback;
    }

}
