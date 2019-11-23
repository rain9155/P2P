package com.example.p2p.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.p2p.app.App;
import com.example.p2p.bean.Audio;
import com.example.p2p.bean.Document;
import com.example.p2p.bean.Image;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.MesType;
import com.example.p2p.callback.IConnectCallback;
import com.example.p2p.callback.IImageReceiveCallback;
import com.example.p2p.callback.IProgressCallback;
import com.example.p2p.callback.IReceiveMessageCallback;
import com.example.p2p.callback.ISendMessgeCallback;
import com.example.p2p.config.Constant;
import com.example.p2p.utils.FileUtil;
import com.example.p2p.utils.Log;
import com.example.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static com.example.p2p.core.ConnectManager.execute;


/**
 * 管理消息的发送和接收
 * 不需要再创建线程池，直接使用ConnectionManager的线程池
 * Created by 陈健宇 at 2019/11/22
 */
public class MessageManager {

    private static final String TAG = MessageManager.class.getSimpleName();
    private static final int MAX_SEND_DATA = 30000;//大约30Kb
    private static final int MAX_FILE_SEND_DATA = 45000000;//大约45Mb
    private static final int TYPE_SEND_SUCCESS = 0x002;
    private static final int TYPE_SEND_FAIL = 0x003;
    private static MessageManager sInstance;
    
    private Map<String, IReceiveMessageCallback> mReceiveCallbacks;//保存每个消息接受回调到ip地址的映射
    private Map<String, IImageReceiveCallback> mImageReceiveCallbacks;//接收用户头像回调
    private Map<String, List<Mes>> mSaveMessages;//暂存消息回调
    private volatile ISendMessgeCallback mSendMessageCallback;
    private Deque<Runnable> mTasks;//任务排队发送
    private Runnable mActiveTask;
    private volatile boolean isRelease;


    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TYPE_SEND_SUCCESS:
                    mSendMessageCallback.onSendSuccess((Mes<?>) msg.obj);
                    break;
                case TYPE_SEND_FAIL:
                    mSendMessageCallback.onSendFail((Mes<?>) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private MessageManager() {
        mReceiveCallbacks = new ConcurrentHashMap<>();
        mImageReceiveCallbacks = new ConcurrentHashMap<>();
        mSaveMessages = new HashMap<>();
        mTasks = new ArrayDeque<>();
    }

    public static MessageManager get(){
        if(sInstance == null){
            synchronized (MessageManager.class){
                MessageManager messageManager;
                if(sInstance == null){
                    messageManager = new MessageManager();
                    sInstance = messageManager;
                }
            }
        }
        return sInstance;
    }

    /**
     * 清理一些资源
     */
    public void release(){
        isRelease = true;
        mReceiveCallbacks.clear();
        mSendMessageCallback = null;
    }

    /**
     * 清理所有映射
     */
    public void destory(){
        mImageReceiveCallbacks.clear();
    }

    /**
     * 发送消息给给定ip的客户端
     */
    public void sendMessage(String targetIp, Mes<?> message){
        sendMessage(targetIp, message, null);
    }

    /**
     * 发送消息给给定ip的客户端
     */
    public void sendMessage(String targetIp, Mes<?> mes, IProgressCallback callback){
        Mes<?> message = mes.clone();
        if(!ConnectManager.get().isContains(targetIp)){
            Log.d(TAG, "客户端连接已经断开");
            //重连
            ConnectManager.get().connect(targetIp, new IConnectCallback() {
                @Override
                public void onConnectSuccess(String targetIp) {
                    sendMessageChecked(targetIp, message, callback);
                }

                @Override
                public void onConnectFail(String targetIp) {
                    if(mSendMessageCallback != null){
                        mHandler.obtainMessage(TYPE_SEND_FAIL, message).sendToTarget();
                    }
                }
            });
            return;
        }
        sendMessageChecked(targetIp, message, callback);
    }

    /**
     * 检查后发送消息
     * @param targetIp 客户端的ip
     * @param message 消息
     * @param callback 进度回调
     */
    private void sendMessageChecked(String targetIp, Mes<?> message,  IProgressCallback callback) {
        final Socket socket = ConnectManager.get().getSocket(targetIp);
        mTasks.offer(() -> {
            try {
                OutputStream os = socket.getOutputStream();
                sendMessageByType(os, message, callback);
                Log.d(TAG, "发送消息成功， message = " + message);
                if(mSendMessageCallback != null){
                    mHandler.obtainMessage(TYPE_SEND_SUCCESS, message).sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "发送消息失败， e = " + e.getMessage());
                if(mSendMessageCallback != null){
                    mHandler.obtainMessage(TYPE_SEND_FAIL, message).sendToTarget();
                }
            }finally {
                scheduleNext();
            }
        });
        if(mActiveTask == null){
            scheduleNext();
        }
    }

    private void scheduleNext(){
        if((mActiveTask = mTasks.poll()) != null){
            execute(mActiveTask);
        }
    }

    /**
     * 根据消息类型发送消息
     */
    private void sendMessageByType(OutputStream outputStream, Mes<?> message, IProgressCallback callback) throws IOException, InterruptedException {
        DataOutputStream os = new DataOutputStream(outputStream);
        MesType type = message.mesType;
        isRelease = false;
        switch (type){
            case TEXT:
                String text = (String)message.data;
                os.writeInt(type.ordinal());
                os.writeUTF(text);
                break;
            case AUDIO:
                Audio audio = (Audio) message.data;
                os.writeInt(type.ordinal());
                os.writeInt(audio.duartion);
                byte[] audioBytes = FileUtils.getFileBytes(audio.audioPath);
                os.writeInt(audioBytes.length);
                os.write(audioBytes);
                break;
            case IMAGE:
                Image image = (Image) message.data;
                byte[] imageBytes;
                if(image.isRaw){
                    imageBytes = FileUtils.getFileBytes(image.imagePath);
                }else {
                    try {
                        File file = Luban.with(App.getContext()).get(image.imagePath);
                        imageBytes = FileUtils.getFileBytes(file.getAbsolutePath());
                    }catch (IOException e){
                        imageBytes = FileUtils.getFileBytes(image.imagePath);
                    }
                }
                int imageLen  = imageBytes.length;
                os.writeInt(type.ordinal());
                os.writeInt(imageLen);
                os.writeInt(message.itemType.ordinal());
                sendBytes(os, imageBytes, imageLen, imageLen, 0, callback);
                image.len = imageLen;
                image.progress = 100;
                Thread.sleep(100);
                break;
            case FILE:
                Document file = (Document) message.data;
                String filePath = file.filePath;
                InputStream fileIn = new FileInputStream(new java.io.File(filePath));
                int fileLen = fileIn.available();
                fileIn.close();
                os.writeInt(type.ordinal());
                os.writeInt(fileLen);
                os.writeUTF(file.fileType);
                os.writeUTF(file.fileSize);
                os.writeUTF(file.fileName);
                byte[] fileBytes;
                if(fileLen < MAX_FILE_SEND_DATA){
                    fileBytes = FileUtils.getFileBytes(filePath);
                    sendBytes(os, fileBytes, fileLen, fileLen, 0, callback);
                }else {//文件太大，分段发送
                    int count = 0;
                    try(InputStream in = new BufferedInputStream(new FileInputStream(filePath))){
                        while (count < fileLen){
                            int maxSendFileLen = MAX_FILE_SEND_DATA;
                            if(count + MAX_FILE_SEND_DATA >= fileLen){
                                maxSendFileLen = fileLen - count;
                            }
                            byte[] tempBytes = new byte[maxSendFileLen];
                            in.read(tempBytes);
                            sendBytes(os, tempBytes, maxSendFileLen, fileLen, count, callback);
                            count += maxSendFileLen;
                        }
                    }
                }
                file.len = fileLen;
                file.progress = 100;
                Thread.sleep(100);
                break;
            default:
                break;
        }
    }

    private void sendBytes(DataOutputStream os, byte[] bytes, int maxSendLen, int fileLen, int preSendLen, IProgressCallback callback) throws IOException {
        int start = 0;
        int end = 0;
        while (end < maxSendLen){
            end += MAX_SEND_DATA;
            if(end >= maxSendLen) end = maxSendLen;
            os.write(bytes, start, end - start);
            Log.d(TAG, "传送数据中，offet = " + (end - start) + ", 长度， len = " + maxSendLen);
            start = end;
            if(callback != null){
                double num = (preSendLen + start) / (fileLen * 1.0);
                int progress = (int) (num * 100);
                mHandler.post(() -> callback.onProgress(progress));
            }
        }
    }

    /**
     * 添加一个消息
     */
    public void addTempMessage(String targetIp, Mes<?> mes){
        List<Mes> list = mSaveMessages.get(targetIp);
        if(list == null){
            list = new ArrayList<>();
            mSaveMessages.put(targetIp, list);
        }
        list.add(mes);
    }

    /**
     * 获取暂存的消息
     */
    public List<Mes> getTempMessages(String targetIp){
        List<Mes> list = mSaveMessages.get(targetIp);
        if(list != null){
            List<Mes> mesList = new ArrayList<>(list);
            list.clear();
            return mesList;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 给指定客户端ip添加一个接受消息的回调
     * @param targetIp 客户端ip
     * @param callback 接受消息的回调
     */
    public void addReceiveMessageCallback(String targetIp, IReceiveMessageCallback callback){
        mReceiveCallbacks.put(targetIp, callback);
    }

    /**
     * 获得接受消息回调接口
     * @param targetIp 指定ip
     */
    public IReceiveMessageCallback getReceiveCallback(String targetIp) {
        return mReceiveCallbacks.get(targetIp);
    }


    /**
     * 给指定客户端ip添加一个接受图片消息的回调
     * @param targetIp 客户端ip
     * @param callback 接受图片消息的回调
     */
    public void addImageReceiveCallback(String targetIp, IImageReceiveCallback callback){
        mImageReceiveCallbacks.put(targetIp, callback);
    }

    /**
     * 获得接收图片消息的回调
     * @param targetIp 指定ip
     */
    public IImageReceiveCallback getImageReceiveCallback(String targetIp){
        return mImageReceiveCallbacks.get(targetIp);
    }


    /**
     * 设置发送消息的回调
     */
    public void setSendMessageCallback(ISendMessgeCallback callback){
        this.mSendMessageCallback = callback;
    }



    //接收消息的线程
    public static class ReceiveMessageThread implements Runnable{

        private static final String TAG = ReceiveMessageThread.class.getSimpleName();
        private static final int MAX_RECEIVE_DATA = 45000000;
        private static final int TYPE_RECEVICE_SUCCESS = 0x000;
        private static final int TYPE_SAVE_MESSAGES = 0x001;
        private static final int TYPE_RECEIVE_USER_IMAGE = 0x002;
        public static final int CLOSE = 0xfff;

        private String mClientIp;
        private volatile Socket mSocket;
        private volatile IReceiveMessageCallback mReceiveMessageCallback;
        private volatile IImageReceiveCallback mImageReceiveCallback;


        private Handler mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case TYPE_RECEVICE_SUCCESS:
                        mReceiveMessageCallback.onReceiveSuccess((Mes<?>) msg.obj);
                        break;
                    case TYPE_SAVE_MESSAGES:
                        MessageManager.get().addTempMessage(mClientIp, (Mes<?>) msg.obj);
                        break;
                    case TYPE_RECEIVE_USER_IMAGE:
                        Mes<?> mes = (Mes<?>) msg.obj;
                        Image image = (Image) mes.data;
                        mImageReceiveCallback.onReceive(image.imagePath);
                        break;
                    default:
                        break;
                }
            }
        };

        ReceiveMessageThread(Socket socket) {
            this.mSocket = socket;
            this.mClientIp = socket.getInetAddress().getHostAddress();
        }

        @Override
        public void run() {
            while (true){
                Mes mes;
                try{
                    InputStream in = mSocket.getInputStream();
                    mes = receiveMessageByType(in);
                    Log.d(TAG, "收到来自客户端的信息，message = " + mes);
                    if(mes.itemType == ItemType.OTHER){
                        if(hasImageReceviceCallback(mClientIp)){
                            mHandler.obtainMessage(TYPE_RECEIVE_USER_IMAGE, mes).sendToTarget();
                        }
                    }else if(mes.mesType == MesType.ERROR){
                        //两端的Socker连接都要关闭
                        try(DataOutputStream os = new DataOutputStream(mSocket.getOutputStream())){
                            os.writeInt(CLOSE);
                        }
                    }else {
                        if(!hasReceviceCallback(mClientIp)){//暂存消息
                            mHandler.obtainMessage(TYPE_SAVE_MESSAGES, mes).sendToTarget();
                        }else {
                            mHandler.obtainMessage(TYPE_RECEVICE_SUCCESS, mes).sendToTarget();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "获取客户端消息失败，e = " + e.getMessage());
                    ConnectManager.get().remove(mClientIp);
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
                    mes = new Mes<String>(ItemType.RECEIVE_TEXT, MesType.TEXT, mClientIp, text);
                    break;
                case 1:
                    int duration = in.readInt();
                    int audioLen = in.readInt();
                    byte[] audioBytes = new byte[audioLen];
                    in.readFully(audioBytes);
                    String audioPath = saveReceiveAudio(audioBytes);
                    Audio audio = new Audio(duration, audioPath);
                    mes = new Mes<Audio>(ItemType.RECEIVE_AUDIO, MesType.AUDIO, mClientIp,  audio);
                    break;
                case 2:
                    int imageLen = in.readInt();
                    int itemType = in.readInt();
                    byte[] imageBytes = receiveBytes(in, imageLen);
                    String imagePath = saveReceiveImage(imageBytes, itemType);
                    Image image = new Image(imagePath, imageLen);
                    mes = new Mes<Image>(itemType != ItemType.OTHER.ordinal() ? ItemType.RECEIVE_IMAGE : ItemType.OTHER, MesType.IMAGE, mClientIp, image);
                    break;
                case 3:
                    int fileLen = in.readInt();
                    String fileType = in.readUTF();
                    String fileSize = in.readUTF();
                    String fileName = in.readUTF();
                    String path = FileUtil.getFilePath(mClientIp, ItemType.RECEIVE_FILE) + fileName + "." + fileType;
                    File file = new File(path);
                    if(file.exists()){//重复文件删除
                        file.delete();
                    }
                    byte[] fileBytes;
                    String filePath = "";
                    if(fileLen < MAX_RECEIVE_DATA){
                        fileBytes = receiveBytes(in, fileLen);
                        filePath = saveReceiveFile(fileBytes, fileName, fileType);
                    }else {//文件太大，分段保存
                        int count = 0;
                        while (count < fileLen){
                            int maxReceiveLen = MAX_RECEIVE_DATA;
                            if(count + maxReceiveLen >= fileLen){
                                maxReceiveLen = fileLen - count;
                            }
                            fileBytes = receiveBytes(in, maxReceiveLen);
                            filePath = saveReceiveFile(fileBytes, fileName, fileType);
                            count += fileBytes.length;//,因为两端的接收速率不一样, 所以fileBytes的长度可能会比maxReceiveLen大一点
                        }
                    }
                    Document document = new Document(filePath, fileName, fileLen, fileSize, fileType);
                    mes = new Mes<Document>(ItemType.RECEIVE_FILE, MesType.FILE, mClientIp, document);
                    break;
                default:
                    ConnectManager.get().remove(mClientIp);
                    break;
            }
            return mes;
        }

        /**
         * 判断是否有设置消息回调
         * @param targetIp 客户端ip
         * @return true表示有，false表示没有
         */
        private boolean hasReceviceCallback(String targetIp){
            IReceiveMessageCallback receiveMessageCallback = MessageManager.get().getReceiveCallback(targetIp);
            if(receiveMessageCallback == null) return false;
            mReceiveMessageCallback = receiveMessageCallback;
            return true;
        }

        /**
         * 判断是否有设置图片消息回调
         * @param targetIp 客户端ip
         * @return true表示有，false表示没有
         */
        private boolean hasImageReceviceCallback(String targetIp){
            IImageReceiveCallback imageReceiveCallback = MessageManager.get().getImageReceiveCallback(targetIp);
            if(imageReceiveCallback == null) return false;
            mImageReceiveCallback = imageReceiveCallback;
            return true;
        }


        /**
         * 保存接收到的音频
         */
        private String saveReceiveAudio(byte[] audioBytes) throws IOException {
            String audioPath = FileUtil.getAudioPath(mClientIp, ItemType.RECEIVE_AUDIO);
            String path = audioPath + System.currentTimeMillis() + ".mp3";
            if(!FileUtils.saveFileBytes(audioBytes, path)) throw new IOException();
            return path;
        }

        /**
         * 保存接收到的图片
         */
        private String saveReceiveImage(byte[] imageBytes, int itemType) throws IOException {
            String path;
            if(itemType == ItemType.OTHER.ordinal()){
                String imagePath = Constant.FILE_PATH_ONLINE_USER + mClientIp + File.separator + "image" + File.separator;
                FileUtils.makeDirs(imagePath);
                path = imagePath + "onLineUserImage.png";
            }else {
                String imagePath = FileUtil.getImagePath(mClientIp, ItemType.RECEIVE_IMAGE);
                path = imagePath + System.currentTimeMillis() + ".png";
            }
            if(!FileUtils.saveFileBytes(imageBytes, path)) throw new IOException();
            return path;
        }

        /**
         * 保存接收到的文件
         */
        private String saveReceiveFile(byte[] fileBytes, String fileName, String fileType) throws IOException {
            String filePath = FileUtil.getFilePath(mClientIp, ItemType.RECEIVE_FILE);
            String path = filePath + fileName + "." + fileType;
            if(!FileUtils.saveFileBytes(fileBytes, path, true)) throw new IOException();
            return path;
        }

        /**
         * 接收字节流
         */
        private byte[] receiveBytes(DataInputStream in, int len) throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] bytes = os.toByteArray();
            while (bytes.length < len){
                byte[] tempBytes = new byte[in.available()];
                in.readFully(tempBytes);
                os.write(tempBytes, 0, tempBytes.length);
                bytes = os.toByteArray();
                Log.d(TAG, "接收中，目前长度 = " + bytes.length + ", len = " + len);
            }
            os.close();
            return bytes;
        }
    }


}
