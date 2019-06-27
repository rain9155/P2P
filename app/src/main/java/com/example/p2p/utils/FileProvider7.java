package com.example.p2p.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;

/**
 * 适配Android 7.0的FileProvide，文件共享
 * Created by 陈健宇 at 2019/1/1
 */
public class FileProvider7 {

    /**
     * 适配获得url，7.0以上获得content://, 以下获得file://
     */
    public static Uri getUriForFile(Context context, File file) {
        Uri fileUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //使用FileProvider内容提供器将封装过的Uri共享给外部
            fileUri = getUriForFile24(context, file);
        } else {
            //将File对象转换为Uri对象，表示本地真实路径
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

    /**
     * 通过file获得content://
     */
    public static Uri getUriForFile24(Context context, File file) {
        Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file);
        return fileUri;
    }

}
