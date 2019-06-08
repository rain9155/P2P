package com.example.p2p.core;

import com.example.p2p.utils.LogUtils;

import java.io.BufferedReader;
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
    private Stack<String> mMessages;

    public ReadWriteThread(Socket socket) {
        this.mSocket = socket;
        mMessages = new Stack<>();
    }

    @Override
    public void run() {
        while (true){
            try(
                    //获取输入流，读取客户端发送过来的消息
                    InputStream in = mSocket.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))
            ){
                String message = "";
                if((message = bufferedReader.readLine()) != null){
                    LogUtils.d(TAG, "收到来自客户端的信息，message = " + message);
                    mMessages.push(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "获取客户端输入流失败，e = " + e.getMessage());
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
