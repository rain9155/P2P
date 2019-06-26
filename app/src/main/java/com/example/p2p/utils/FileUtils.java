package com.example.p2p.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.example.p2p.bean.ItemType;
import com.example.p2p.config.Constant;
import com.example.p2p.config.MimeType;
import com.example.utils.FileUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static com.example.p2p.utils.IntentUtils.*;

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
        makeDirs(filePath);
        return filePath;
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

    /**
     * 找到可以打开文件的所有应用
     * @param filePath 文件的真实路径
     */
    public static void openFile(Context context, String filePath) {
        String end = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase(Locale.getDefault());
        Intent intent = null;
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            intent = getAudioFileIntent(context, filePath);
        } else if (end.equals("3gp") || end.equals("mp4")) {
            intent = getVideoFileIntent(context, filePath);
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
           // intent = getImageFileIntent(filePath);
        } else if (end.equals("apk")) {
            intent = getApkFileIntent(context, filePath);
        } else if (end.equals("ppt") || end.equals("pptx")) {
            intent = getPPtFileIntent(context, filePath);
        } else if (end.equals("xls") || end.equals("xlsx")) {
            intent = getExcelFileIntent(context, filePath);
        } else if (end.equals("doc") || end.equals("docx")) {
            intent = getWordFileIntent(context, filePath);
        } else if (end.equals("pdf")) {
            intent = getPdfFileIntent(context, filePath);
        } else if (end.equals("txt")) {
            intent = getTxtFileIntent(context, filePath);
        } else if(end.equals("zip")){
            intent = getZipFileIntent(context, filePath);
        }else {
            intent = getAllIntent(context, filePath);
        }
        context.startActivity(intent);
    }

    /**
     * 根据文件路径的后缀名获取文件类型
     * @return MimeType
     */
    public static String getMimeType(String filePath){
        String end = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase(Locale.getDefault());
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            return MimeType.AUDIO;
        } else if (end.equals("3gp") || end.equals("mp4")) {
            return MimeType.VIDEO;
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
            return MimeType.IMAGE;
        } else if (end.equals("apk")) {
            return MimeType.APK;
        } else if (end.equals("ppt") || end.equals("pptx")) {
            return MimeType.PPT;
        } else if (end.equals("xls") || end.equals("xlsx")) {
            return MimeType.XLS;
        } else if (end.equals("doc") || end.equals("docx")) {
            return MimeType.DOC;
        } else if (end.equals("pdf")) {
            return MimeType.PDF;
        }else if (end.equals("txt")) {
            return MimeType.TXT;
        } else if(end.equals("zip")){
            return MimeType.ZIP;
        }else {
            return MimeType.UNKOWN;
        }
    }


    /**
     * 获得某个文件的大小
     * @param filePath 文件路径
     */
    public static String getFileSize(String filePath){
        File file = new File(filePath);
        if(!file.isFile()) return "0k";
        return FileUtil.getFormatSize(file.length());
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
            LogUtils.e(TAG, "无法获取文件真实路径，请使用url获取图片，e = " + e.getMessage());
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
