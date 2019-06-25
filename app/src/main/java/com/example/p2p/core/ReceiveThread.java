package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.p2p.bean.Audio;
import com.example.p2p.bean.Image;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.MesType;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IReceiveMessageCallback;
import com.example.p2p.utils.FileUtils;
import com.example.p2p.utils.LogUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
                if(hasReceviceCallback(mUser.getIp())) mHandler.obtainMessage(TYPE_RECEVICE_SUCCESS, mes).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, "获取客户端消息失败，e = " + e.getMessage());
                //两端的Socker连接都要关闭
                try {
                    mSocket.close();
                    break;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                ConnectManager.getInstance().removeConnect(mUser.getIp());
                ConnectManager.getInstance().removeReceiveCallback(mUser.getIp());
                break;
            }
        }
    }

    /**
     * 根据消息类型接受消息
     */
    private Mes<?> receiveMessageByType(InputStream inputStream) throws IOException {
        DataInputStream in = new DataInputStream(inputStream);
        Mes mes = new Mes(MesType.ERROR);
        int type = in.readInt();
        switch(type){
            case 0:
                String text = in.readUTF();
                mes = new Mes<String>(ItemType.RECEIVE_TEXT, MesType.TEXT, mUser.getIp(), text);
                break;
            case 1:
                int duration = in.readInt();
                int audioLen = in.readInt();
                byte[] audioBytes = new byte[audioLen];
                in.readFully(audioBytes);
                String audioPath = saveReceiveAudio(audioBytes);
                Audio audio = new Audio(duration, audioPath);
                mes = new Mes<Audio>(ItemType.RECEIVE_AUDIO, MesType.AUDIO, mUser.getIp(), audio);
                break;
            case 2:
                int imageLen = in.readInt();
                LogUtils.d(TAG, "接收图片长度， len = " + imageLen);
                ByteArrayOutputStream imageOs = new ByteArrayOutputStream();
                byte[] imageBytes = imageOs.toByteArray();
                while (imageBytes.length != imageLen){
                    byte[] imageTempBytes = new byte[in.available()];
                    in.readFully(imageTempBytes);
                    imageOs.write(imageTempBytes, 0, imageTempBytes.length);
                    imageBytes = imageOs.toByteArray();
                    LogUtils.d(TAG, "图片接收中，目前长度 = " + imageBytes.length);
                }
                imageOs.close();
                String imagePath = saveReceiveImage(imageBytes);
                Image image = new Image(imagePath, imageLen);
                mes = new Mes<Image>(ItemType.RECEIVE_IMAGE, MesType.IMAGE, mUser.getIp(), image);
                break;
            default:
                break;
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
     */
    public String saveReceiveAudio(byte[] audioBytes) throws IOException {
        String audioPath = FileUtils.getAudioPath(mUser.getIp(), ItemType.RECEIVE_AUDIO);
        String path = audioPath + System.currentTimeMillis() + ".mp3";
        if(!FileUtils.saveFileBytes(audioBytes, path)) throw new IOException();
        return path;
    }

    /**
     * 保存接收到的图片
     */
    private String saveReceiveImage(byte[] imageBytes) throws IOException {
        String imagePath = FileUtils.getImagePath(mUser.getIp(), ItemType.RECEIVE_IMAGE);
        String path = imagePath + System.currentTimeMillis() + ".png";
        if(!FileUtils.saveFileBytes(imageBytes, path)) throw new IOException();
        return path;
    }
}
