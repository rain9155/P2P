package com.example.p2p.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

/**
 * Created by 陈健宇 at 2019/6/20
 */
public class ImageUtils {

    /**
     * 压缩一张图片
     * @param bitmap 要压缩的图片
     * @param sWidth 原图的宽的压缩比
     * @param sHeight 原图的高的压缩比
     * @return 压缩后的图片
     */
    public static Bitmap compressBitmap(Bitmap bitmap, float sWidth, float sHeight){
        Matrix matrix = new Matrix();
        matrix.setScale(sWidth, sHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 根据图片路径创建图片Uri
     * @param path 图片的真实路径
     * @param name  图片名字
     * @return 图片Uri
     */
    public static Uri getImageUri(Context context, String path, String name) {
        Uri imageUrl;
        File fileOutPutImage = new File(path, name);
        if(!fileOutPutImage.exists()){
            try {
                fileOutPutImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(Build.VERSION.SDK_INT >= 24){
            //使用FileProvider内容提供器将封装过的Uri共享给外部
            imageUrl = FileProvider.getUriForFile(context, "com.example.p2p.fileprovider", fileOutPutImage);
        }else {
            //将File对象转换为Uri对象，表示这张图片的本地真实路径
            imageUrl = Uri.fromFile(new File(path));
        }
        return imageUrl;
    }


    /**
     * 根据uri获得图片的真实路径
     * @param data 通过intent.getData获得Uri
     * @return 图片的真实路径
     */
    @TargetApi(19)
    public static String getImagePathOnKitKat(Context context, @NonNull Intent data) {
        String path = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(context, uri)){
            //如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];//解出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" +id;
                path = getImagePath(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                path = getImagePath(context, contentUri, null);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())){
            //如果是普通类型的uri，则普通方式处理
            path = getImagePath(context, uri, null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())){
            //如果是file类型，直接获取路径
            path = uri.getPath();
        }
        return path;
    }

    /**
     * 通过Uri和selection来获取真实的图片路径
     */
    private static String getImagePath(Context context, Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

}
