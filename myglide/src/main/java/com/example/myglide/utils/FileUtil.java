package com.example.myglide.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Created by 陈健宇 at 2019/11/4
 */
public class FileUtil {


    /**
     * 获得用户的硬盘可用空间
     * @param diskCacheDir 文件路径
     * @return 可用空间大小，单位KB
     */
    @SuppressLint("ObsoleteSdkInt")
    public static long getUsableSpace(File diskCacheDir) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final StatFs statFs = new StatFs(diskCacheDir.getPath());
            return statFs.getAvailableBytes();
        }else {
            return diskCacheDir.getUsableSpace();
        }
    }

    /**
     * 获得硬盘缓存路径
     * @param fileName 文件名
     */
    public static File getDiskCacheDir(Context context, String fileName) {
        //判断是否有外部储存，是否可读
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if(externalStorageAvailable){
            //外部缓存目录, /sdcard/Android/data/<application package>/cache
            cachePath = context.getExternalCacheDir().getPath();
        }else {
            //本地缓存目录,/data/data/<application package>/cache
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + fileName);
    }


    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
