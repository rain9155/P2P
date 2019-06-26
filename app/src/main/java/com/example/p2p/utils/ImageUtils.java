package com.example.p2p.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.example.p2p.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.IntStream;

/**
 * Created by 陈健宇 at 2019/6/20
 */
public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();

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
     * 根据Uri获取图片
     * @param imageUri 图片uri
     * @return bitmap图片
     */
    public static Bitmap getImageByUri(Context context, Uri imageUri){
        try(
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri)
            ){

            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "解析图片失败, e = " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "解析图片输入流失败, e = " + e.getMessage());
        }
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_user_image);
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
            imageUrl = Uri.fromFile(fileOutPutImage);
        }
        return imageUrl;
    }

}
