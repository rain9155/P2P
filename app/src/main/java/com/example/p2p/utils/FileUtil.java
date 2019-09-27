package com.example.p2p.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import com.example.p2p.bean.ItemType;
import com.example.p2p.config.Constant;
import com.example.utils.FileUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件操作类
 * Created by 陈健宇 at 2019/6/17
 */
public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    /**
     * 保存用户图片
     * @param bitmap 图片
     */
    public static String saveUserBitmap(Bitmap bitmap){
        FileUtils.makeDirs(Constant.FILE_PATH_USER);
        String imagePath = Constant.FILE_PATH_USER + "userImage.png";
        File file = new File(imagePath);
        saveBitmap(bitmap, file);
        return imagePath;
    }

    /**
     * 返回用户图片
     */
    public static Bitmap getUserBitmap(){
        String imagePath = Constant.FILE_USER_IMAGE;
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
        FileUtils.makeDirs(path);
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
        FileUtils.makeDirs(audioPath);
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
        FileUtils.makeDirs(imagePath);
        return imagePath;
    }

    /**
     * 获得相应用户存放文件的地方
     * @param ip 用户ip
     * @param type item类型
     * @return 存放文件的文件夹路径
     */
    public static String getFilePath(String ip, ItemType type){
        String filePath = "";
        if(type == ItemType.RECEIVE_FILE){
            filePath = Constant.FILE_PATH_ONLINE_USER + ip + File.separator + "receiveFile" + File.separator;
        }else {
            filePath = Constant.FILE_PATH_ONLINE_USER + ip + File.separator + "sendFile" + File.separator;
        }
        FileUtils.makeDirs(filePath);
        return filePath;
    }

    /**
     * 获得某个文件的大小
     * @param filePath 文件路径
     */
    public static String getFileSize(String filePath){
        File file = new File(filePath);
        if(!file.isFile()) return "0k";
        return FileUtils.getFormatSize(file.length());
    }

    /**
     * 获取文件扩展名
     * @param filename 文件路径或文件名
     */
    public static String getExtensionName(String filename) {
        if (filename != null && filename.length() > 0) {
            int dot = filename.lastIndexOf('.');
            if (dot > -1 && dot < filename.length() - 1) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

    /**
     * 根据文件路径，获取上一级文件夹名称
     * @param path 文件路径
     * @return 文件夹名称
     */
    public static String getParentFolder(String path) {
        if (!TextUtils.isEmpty(path)) {
            String[] strings = path.split(File.separator);
            if (strings.length >= 2) {
                return strings[strings.length - 2];
            }
        }
        return "";
    }

    /**
     * 根据uri获得文件的真实路径
     * 不支持从手机外部应用获取，最好直接用uri获取(官方推荐)
     * @param uri 文件的Uri
     * @return 文件的真实路径
     */
    @TargetApi(19)
    public static String getFilePathByUri(Context context, @NonNull Uri uri) {
        String path = null;
        try {
            //DocumentProvider
            if(DocumentsContract.isDocumentUri(context, uri)){
                //如果是document类型的Uri，则通过document id处理
                String docId = DocumentsContract.getDocumentId(uri);
                if("com.android.externalstorage.documents".equals(uri.getAuthority())){//ExternalStorageProvider
                    final String[] split = docId.split(":");
                    final String type = split[0];//类型
                    final String id = split[1];//解出数字格式的id
                    if("primary".equalsIgnoreCase(type)){
                        return Environment.getExternalStorageDirectory() + File.separator + id;
                    }
                }else if("com.android.providers.media.documents".equals(uri.getAuthority())){//MediaProvider
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    final String id = split[1];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=" + id;
                    path = getPath(context, contentUri, selection);
                }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){//DownloadsProvider
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    path = getPath(context, contentUri, null);
                }
            }else if ("content".equalsIgnoreCase(uri.getScheme())){
                //如果是普通类型的uri，则普通方式处理
                path = getPath(context, uri, null);
            }else if ("file".equalsIgnoreCase(uri.getScheme())){
                //如果是file类型，直接获取路径
                path = uri.getPath();
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.e(TAG, "无法获取文件真实路径，请使用url获取图片，e = " + e.getMessage());
            return null;
        }
        return path;
    }

    /**
     * 通过Uri和selection来获取真实的路径
     */
    private static String getPath(Context context, Uri uri, String selection) {
        String path = null;
        final String columnName = "_data";
        final String[] projection = {columnName};
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, null);
        if (cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndexOrThrow(columnName));
            }
            cursor.close();
        }
        return path;
    }

    private static void saveBitmap(Bitmap bitmap, File file) {
        FileOutputStream fileOutputStream;
        BufferedOutputStream bufferedOutputStream;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                LogUtil.e(TAG, "创建文件失败， e = " + e.getMessage());
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
