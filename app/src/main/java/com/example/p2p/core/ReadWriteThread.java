package com.example.p2p.core;

import com.example.p2p.utils.LogUtils;
import com.example.utils.CommonUtil;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by 陈健宇 at 2019/6/9
 */
public class ReadWriteThread implements Runnable{

    private static final String TAG = ReadWriteThread.class.getSimpleName();
    private Socket mSocket;
    private String mClientIp;
    private Stack<String> mMessages;

    public ReadWriteThread(Socket socket) {
        this.mSocket = socket;
        this.mClientIp = socket.getInetAddress().getHostAddress();
        mMessages = new Stack<>();
    }

    @Override
    public void run() {
        while (true){
            try{
                //获取输入流，读取客户端发送过来的消息
                InputStream in = mSocket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(in);
                String message = dataInputStream.readUTF();
                LogUtils.d(TAG, "收到来自客户端的信息，message = " + message);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "获取客户端输入流失败，e = " + e.getMessage());
                break;
            }
        }
    }

    /**
     * 获取最新的消息
     */
    public String getMessage(){
        return mMessages.pop();
    }

}
