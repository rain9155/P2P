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

import com.example.p2p.BuildConfig;
import com.example.p2p.R;
import com.example.p2p.bean.ItemType;
import com.example.p2p.config.FileType;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
     * 根据图片Uri保存图片
     * @param imageUri 图片Uri
     * @param ip 目标用户ip
     * @return 保存图片的路径
     */
    public static String saveImageByUri(Context context, Uri imageUri, String ip){
        String path = FileUtils.getImagePath(ip, ItemType.SEND_IMAGE);
        String name = System.currentTimeMillis() + ".png";
        File file = new File(path + name);
        try(
                InputStream in = context.getContentResolver().openInputStream(imageUri);
                OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
        ) {
            if(!file.exists()) file.createNewFile();
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            os.write(bytes);
            return file.getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.e(TAG, "保存图片失败");
        return "";
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
        File fileOutPutImage = new File(path, name);
        if(!fileOutPutImage.exists()){
            try {
                fileOutPutImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return FileProvider7.getUriForFile(context, fileOutPutImage);
    }

    /**
     * 根据文件类型，获得相应的icon
     * @param fileType 文件类型
     * @return icon
     */
    public static int getImageId(String fileType) {
        int id;
        switch (fileType){
            case FileType.PDF:
                id = R.drawable.ic_pdf;
                break;
            case FileType.PPT:
            case FileType.PPTX:
                id = R.drawable.ic_ppt;
                break;
            case FileType.XLS:
            case FileType.XLSX:
                id = R.drawable.ic_excel;
                break;
            case FileType.DOC:
            case FileType.DOCX:
                id = R.drawable.ic_word;
                break;
            case FileType.TXT:
                id = R.drawable.ic_txt;
                break;
            case FileType.ZIP:
            case FileType.RAR:
                id = R.drawable.ic_zip;
                break;
            default:
                id = R.drawable.ic_unknow_file;
                break;
        }
        return id;
    }

}
