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
import android.provider.DocumentsContract;
import android.provider.MediaStore;

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

    @TargetApi(19)
    public static String getImagePathOnKitKat(Context context, Intent data) {
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
