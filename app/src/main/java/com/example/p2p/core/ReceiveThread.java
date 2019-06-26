package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.p2p.bean.Audio;
import com.example.p2p.bean.File;
import com.example.p2p.bean.Image;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.MesType;
import com.example.p2p.bean.User;
import com.example.p2p.callback.IReceiveMessageCallback;
import com.example.p2p.utils.FileUtils;
import com.example.p2p.utils.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
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
                byte[] imageBytes = receiveBytes(in, imageLen);
                String imagePath = saveReceiveImage(imageBytes);
                Image image = new Image(imagePath, imageLen);
                mes = new Mes<Image>(ItemType.RECEIVE_IMAGE, MesType.IMAGE, mUser.getIp(), image);
                break;
            case 3:
                int flieLen = in.readInt();
                String flieType = in.readUTF();
                String fileSize = in.readUTF();
                String fileName = in.readUTF();
                byte[] fileBytes = receiveBytes(in, flieLen);
                String filePath = saveReceiveFile(fileBytes, fileName, flieType);
                File file = new File(filePath, fileName, flieLen, fileSize, flieType);
                mes = new Mes<File>(ItemType.RECEIVE_FILE, MesType.FILE, mUser.getIp(), file);
                break;
            default:
                break;
        }
        return mes;
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

    /**
     * 保存接收到的文件
     */
    private String saveReceiveFile(byte[] fileBytes, String fileName, String fileType) throws IOException {
        String filePath = FileUtils.getFilePath(mUser.getIp(), ItemType.RECEIVE_FILE);
        String path = filePath + fileName + "." + fileType;
        if(!FileUtils.saveFileBytes(fileBytes, path)) throw new IOException();
        return path;
    }

    /**
     * 接收字节流
     */
    private byte[] receiveBytes(DataInputStream in, int len) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] bytes = os.toByteArray();
        while (bytes.length != len){
            byte[] tempBytes = new byte[in.available()];
            in.readFully(tempBytes);
            os.write(tempBytes, 0, tempBytes.length);
            bytes = os.toByteArray();
            LogUtils.d(TAG, "接收中，目前长度 = " + bytes.length);
        }
        os.close();
        return bytes;
    }
}
