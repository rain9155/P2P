package com.example.p2p.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.User;
import com.example.p2p.config.Constant;
import com.example.utils.FileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件操作类
 * Created by 陈健宇 at 2019/6/17
 */
public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    /**
     * 获得应用关联文件路径
     */
    public static String getFilePath(Context context, String name){
        String filePath;
        if (!"mounted".equals(Environment.getExternalStorageState()) && Environment.isExternalStorageRemovable()) {
            filePath = context.getFilesDir().getPath();
        } else {
            filePath = context.getExternalFilesDir(null).getPath();
        }
        return filePath + File.separator + name;
    }

    /**
     * 根据给定的path建立文件夹
     * @param path 文件夹路径
     * @return true表示建立成功，false反之
     */
    public static boolean makeDirs(String path){
        char separator = path.charAt(path.length() - 1);
        if(!String.valueOf(separator).equals(File.separator)){
            StringBuilder builder = new StringBuilder(path);
            builder.append(File.separator);
            path = builder.toString();
        }
        File dir = new File(path);
        if(!dir.isDirectory()){
            dir.mkdirs();
        }
        return dir.isDirectory();
    }

    /**
     * 保存用户图片
     * @param bitmap 图片
     */
    public static String saveUserBitmap(Bitmap bitmap){
        makeDirs(Constant.FILE_PATH_USER);
        String imagePath = Constant.FILE_PATH_USER + "userImage.png";
        File file = new File(imagePath);
        saveBitmap(bitmap, file);
        return imagePath;
    }

    /**
     * 返回用户图片
     */
    public static Bitmap getUserBitmap(){
        String imagePath = Constant.FILE_PATH_USER + "userImage.png";
        return BitmapFactory.decodeFile(imagePath);
    }

    /**
     * 保存在线用户图片
     * @param bitmap 图片
     * @param name 用户名
     * @return 文件路径
     */
    public static String saveOnlineUserBitmap(Bitmap bitmap, String name){
        String path = Constant.FILE_PATH_ONLINE_USER + name + File.separator + "image" + File.separator;
        makeDirs(path);
        String fileName = path + "onLineUserImage.png";
        File file = new File(fileName);
        saveBitmap(bitmap, file);
        return fileName;
    }

    /**
     * 获得相应用户存放音频的地方
     * @param ip 用户ip
     * @param type item类型
     * @return 存放音频文件夹路径
     */
    public static String getAudioPath(String ip, ItemType type){
        String audioPath;
        if(type == ItemType.RECEIVE_AUDIO){
            audioPath =  Constant.FILE_PATH_ONLINE_USER + ip + File.separator + "receiveAudio" + File.separator;
        }else {
            audioPath =  Constant.FILE_PATH_ONLINE_USER + ip + File.separator + "sendAudio" + File.separator;
        }
        makeDirs(audioPath);
        return audioPath;
    }

    /**
     * 获得相应用户存放图片的地方
     * @param ip 用户ip
     * @param type item类型
     * @return 存放图片文件夹路径
     */
    public static String getImagePath(String ip, ItemType type){
        String imagePath = "";
        if(type == ItemType.RECEIVE_IMAGE){
            imagePath = Constant.FILE_PATH_ONLINE_USER + ip + File.separator + "receiveImage" + File.separator;
        }else {
            imagePath = Constant.FILE_PATH_ONLINE_USER + ip + File.separator + "sendImage" + File.separator;
        }
        makeDirs(imagePath);
        return imagePath;
    }

    /**
     * 根据路径获得字节流
     * @return 字节流
     */
    public static byte[] getFileBytes(String path){
        byte[] bytes = new byte[0];
        try(InputStream in = new FileInputStream(path)){
            bytes = new byte[in.available()];
            in.read(bytes);
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "获取字节流失败， e = " + e.getMessage());
        }
        return bytes;
    }

    /**
     * 根据路径存放字节流
     * @return false表示失败，反之成功
     */
    public static boolean saveFileBytes(byte[] bytes, String path){
        File file = new File(path);
        try(
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)
        ){
            if(!file.exists()){
                file.createNewFile();
            }
            bufferedOutputStream.write(bytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "保存字节流失败");
        }
        return false;
    }

    private static void saveBitmap(Bitmap bitmap, File file) {
        FileOutputStream fileOutputStream;
        BufferedOutputStream bufferedOutputStream;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "创建文件失败， e = " + e.getMessage());
            }
        }
        try {
            fileOutputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
