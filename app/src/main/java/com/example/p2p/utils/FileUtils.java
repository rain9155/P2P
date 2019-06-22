package com.example.p2p.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

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
    public static void saveUserBitmap(Bitmap bitmap){
        makeDirs(Constant.FILE_PATH_USER_IMAGE);
        File file = new File(Constant.FILE_USER_IMAGE);
        saveBitmap(bitmap, file);
    }

    /**
     * 获得用户图片
     */
    public static Bitmap getUserBitmap(){
        return BitmapFactory.decodeFile(Constant.FILE_USER_IMAGE);
    }

    /**
     * 获得用户图片流
     */
    public static byte[] getImageBytes(String imagePath){
        byte[] imageBytes = new byte[0];
        try(InputStream in = new FileInputStream(imagePath)){
            imageBytes = new byte[in.available()];
            in.read(imageBytes);
            return imageBytes;
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "获取头像图片失败， e = " + e.getMessage());
        }
        return imageBytes;
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
     * 获得保存的在线用户图片
     * @param userName 用户名
     * @return 在线用户图片
     */
    public static Bitmap getOnlineUserBitmap(String userName){
        String fileName = Constant.FILE_PATH_ONLINE_USER + userName + File.separator + "image" + File.separator + "onLineUserImage.png";
        return BitmapFactory.decodeFile(fileName);
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
