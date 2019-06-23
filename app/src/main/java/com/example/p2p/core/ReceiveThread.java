package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.p2p.bean.Audio;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.MesType;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IReceiveMessageCallback;
import com.example.p2p.config.Constant;
import com.example.p2p.utils.FileUtils;
import com.example.p2p.utils.LogUtils;
import com.example.utils.FileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
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
    private User mUser;
    private List<String> mMessageList;
    private volatile IReceiveMessageCallback mReceiveMessageCallback;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TYPE_RECEVICE_SUCCESS:
                    mReceiveMessageCallback.onReceiveSuccess((Mes<?>) msg.obj);
                    break;
                case TYPE_RECEVICE_FAIL:
                    mReceiveMessageCallback.onReceiveFail((Mes<?>) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    public ReceiveThread(Socket socket, User user) {
        this.mSocket = socket;
        this.mUser = user;
        this.mMessageList = new CopyOnWriteArrayList<>();
    }

    @Override
    public void run() {
        while (true){
            Mes mes = null;
            try{
                InputStream in = mSocket.getInputStream();
                mes = receiveMessageByType(in);
                LogUtils.d(TAG, "收到来自客户端的信息，message = " + mes);
                if(hasReceviceCallback(mUser.getIp())){
                    mHandler.obtainMessage(TYPE_RECEVICE_SUCCESS, mes).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "获取客户端输入流失败, 连接断开，e = " + e.getMessage());
                ConnectManager.getInstance().removeConnect(mUser.getIp());
                ConnectManager.getInstance().removeReceiveCallback(mUser.getIp());
                if(hasReceviceCallback(mUser.getIp()))
                    mHandler.obtainMessage(TYPE_RECEVICE_FAIL, mes).sendToTarget();
                break;
            }
        }
    }

    /**
     * 根据消息类型接受消息
     */
    private Mes<?> receiveMessageByType(InputStream inputStream) {
        DataInputStream in = new DataInputStream(inputStream);
        Mes mes = new Mes(MesType.ERROR);
        int type = -1;
        try {
            type = in.readInt();
            switch(type){
                case 0:
                    String text = in.readUTF();
                    mes = new Mes<>(MesType.TEXT, mUser.getIp(), text);
                    break;
                case 1:
                    int duration = in.readInt();
                    int len = in.readInt();
                    byte[] bytes = new byte[len];
                    in.readFully(bytes);
                    String audioPath = saveReceiveAudio(bytes);
                    Audio audio = new Audio(duration, audioPath);
                    mes = new Mes<>(MesType.AUDIO, mUser.getIp(), audio);
                    break;
                default:
                    break;
            }
            LogUtils.d(TAG, "读取消息成功， type = " + type);
        } catch (IOException e) {
            e.printStackTrace();
            if(hasReceviceCallback(mUser.getIp())){
                mReceiveMessageCallback.onReceiveFail(mes);
            }
            Log.e(TAG, "读取消息失败，类型type = " + type + ", e = " + e.getMessage());
        }
        return mes;
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
        IReceiveMessageCallback receiveMessageCallback = ConnectManager.getInstance().getReceiveCallback(targetIp);
        if(receiveMessageCallback == null) return false;
        mReceiveMessageCallback = receiveMessageCallback;
        return true;
    }


    /**
     * 保存接收到的音频
     * @throws IOException
     */
    public String saveReceiveAudio(byte[] bytes) throws IOException {
        String audioPath = FileUtils.getAudioPath(mUser.getIp(), Constant.TYPE_ITEM_RECEIVE_AUDIO);
        String path = audioPath + System.currentTimeMillis() + ".mp3";
        File file = new File(path);
        if(!file.exists()){
            file.createNewFile();
        }
        try(
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)
        ){
            bufferedOutputStream.write(bytes);
        }
        return path;
    }
}
