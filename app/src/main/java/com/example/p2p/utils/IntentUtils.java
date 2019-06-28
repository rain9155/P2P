package com.example.p2p.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.p2p.config.MimeType;

/**
 * 获取Intent工具类
 * Created by 陈健宇 at 2019/6/24
 */
public class IntentUtils {

    /**
     * 获得一个选择照片的Intent
     */
    public static Intent getChooseImageIntent(){
        Intent galleryintent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryintent.setType(MimeType.IMAGE);
        galleryintent.addCategory(Intent.CATEGORY_OPENABLE);
        return galleryintent;
    }

    /**
     * 获得一个选择文件的Intent
     */
    public static Intent getChooseFileIntent(){
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        String[] mineTypes = {
//                MimeType.DOC, MimeType.DOCX, MimeType.PDF, MimeType.PPT,
//                MimeType.PPTX, MimeType.XLS, MimeType.XLSX, MimeType.APK,
//                MimeType.TEXT, MimeType.Z, MimeType.ZIP, MimeType.TAR,
//                MimeType.TGZ
//        };
//        fileIntent.putExtra(Intent.EXTRA_MIME_TYPES, mineTypes);
//        fileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);//多选
        fileIntent.setType(MimeType.ALL);
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        return fileIntent;
    }

    /**
     * 获取一个用于打开任何文件的intent
     */
    public static Intent getAllIntent(Context context, String path) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = getUri(context, path);
        intent.setDataAndType(uri, MimeType.ALL);
        return intent;

    }

    /**
     * 获取一个用于打开APK文件的intent
     */
    public static Intent getApkFileIntent(Context context, String path) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = getUri(context, path);
        intent.setDataAndType(uri, MimeType.APK);
        return intent;
    }

    /**
     * 获取一个用于打开VIDEO文件的intent
     */
    public static Intent getVideoFileIntent(Context context, String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = getUri(context, path);
        intent.setDataAndType(uri, MimeType.VIDEO);
        return intent;
    }

    /**
     * 获取一个用于打开AUDIO文件的intent
     */
    public static Intent getAudioFileIntent(Context context, String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = getUri(context, path);
        intent.setDataAndType(uri, MimeType.AUDIO);
        return intent;
    }

    /**
     * 获取一个用于打开Html文件的intent
     */
    public static Intent getHtmlFileIntent(String param) {
        Uri uri = Uri.parse(param).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param).build();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, MimeType.HTML);
        return intent;
    }

    /**
     * 获取一个用于打开图片文件的intent
     */
    public static Intent getImageFileIntent(Context context, String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = getUri(context, path);
        intent.setDataAndType(uri, MimeType.IMAGE);
        return intent;
    }

    /**
     * 获取一个用于打开PPT文件的intent
     */
    public static Intent getPPtFileIntent(Context context, String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String[] mineTypes = {MimeType.PPT, MimeType.PPTX};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mineTypes);
        Uri uri = getUri(context, path);
        intent.setDataAndType(uri, MimeType.PPT);
        return intent;
    }

    /**
     * 获取一个用于打开Excel文件的intent
     */
    public static Intent getExcelFileIntent(Context context, String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String[] mineTypes = {MimeType.XLS, MimeType.XLSX};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mineTypes);
        Uri uri = getUri(context, path);
        intent.setDataAndType(uri, MimeType.XLS);
        return intent;
    }

    /**
     * 获取一个用于打开Word文件的intent
     */
    public static Intent getWordFileIntent(Context context, String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String[] mineTypes = {MimeType.DOC, MimeType.DOCX};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mineTypes);
        Uri uri = getUri(context, path);
        intent.setDataAndType(uri, MimeType.DOC);
        return intent;
    }

    /**
     * 获取一个用于打开txt文本文件的intent
     */
    public static Intent getTxtFileIntent(Context context, String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = getUri(context, path);
        intent.setDataAndType(uri, MimeType.TXT);
        return intent;
    }

    /**
     * 获取一个用于打开Zip文件的intent
     */
    public static Intent getZipFileIntent(Context context, String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = getUri(context, path);
        intent.setDataAndType(uri, MimeType.ZIP);
        return intent;
    }

    /**
     * 获取一个用于打开PDF文件的intent
     */
    public static Intent getPdfFileIntent(Context context, String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = getUri(context, path);
        intent.setDataAndType(uri, MimeType.PDF);
        return intent;
    }

    private static Uri getUri(Context context, String path) {
        java.io.File file = new java.io.File(path);
        return FileProvider7.getUriForFile(context, file);
    }
}
