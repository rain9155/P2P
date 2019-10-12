package com.example.p2p.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.example.p2p.bean.Folder;
import com.example.p2p.bean.Photo;
import com.example.p2p.callback.IPhotosCallback;
import com.example.p2p.core.ConnectManager;
import com.example.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从相册加载照片工具类
 * Created by 陈健宇 at 2019/9/27
 */
public class PhotoUtil {

    private static final String ALL_PHOTOS = "全部图片";

    /**
     * 从SDCard加载图片
     * @param context  context
     * @param callback 回调
     */
    public static void loadPhotosFromExternal(final Context context, final IPhotosCallback callback) {
        ConnectManager.getInstance().executeTast(() -> {
            if(callback == null) return;
            Uri photoUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(photoUri, new String[]{
                            MediaStore.Images.Media.DATA,
                            MediaStore.Images.Media.DISPLAY_NAME,
                            MediaStore.Images.Media.DATE_ADDED,
                            MediaStore.Images.Media._ID},
                    null,
                    null,
                   null);
            List<Photo> photos = new ArrayList<>();
            //读取扫描到的图片
            if (cursor != null && cursor.moveToLast()) {
                while (cursor.moveToPrevious()) {
                    // 获取图片的路径
                    String path = cursor.getString(
                            cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    //获取图片名称
                    String name = cursor.getString(
                            cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    //获取图片时间
                    long time = cursor.getLong(
                            cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                    //过滤未下载完成的文件
                    if (!"downloading".equals(FileUtil.getExtensionName(path))) {
                        photos.add(new Photo(name, path, time));
                    }
                }
                cursor.close();
            }
            splitPhotosByFolder(photos, callback);
        });
    }

    /**
     * 把图片按文件夹拆分，第一个文件夹保存所有的图片
     * @param photos 集合
     * @return 按文件拆分的图片集合
     */
    private static void splitPhotosByFolder(List<Photo> photos, IPhotosCallback callback) {
        if(CommonUtil.isEmptyList(photos)) return;
        Map<String, List<Photo>> cache = new HashMap<>();
        cache.put(ALL_PHOTOS, photos);
        int size = photos.size();
        for (int i = 0; i < size; i++) {
            //获得图片路径
            String path = photos.get(i).path;
            //获得图片所在文件夹
            String folderName = FileUtil.getParentFolder(path);
            if(TextUtils.isEmpty(folderName)) continue;
            //把图片按文件夹名分类
            if (!cache.containsKey(folderName)) {
                cache.put(folderName, new ArrayList<>());
            }
            cache.get(folderName).add(photos.get(i));
        }
        onPhotosCallback(cache, callback);
    }

    /**
     * 执行展示照片回调
     */
    private static void onPhotosCallback(Map<String, List<Photo>> cache, IPhotosCallback callback){
        List<Folder> folders = new ArrayList<>(cache.size());
        List<Photo> allPhotos = cache.get(ALL_PHOTOS);
        folders.add(new Folder(
                ALL_PHOTOS,
                allPhotos,
                allPhotos.get(0).path,
                true));
        cache.remove(ALL_PHOTOS);
        for(String folderName : cache.keySet()){
            Folder folder = new Folder(
                    folderName,
                    cache.get(folderName),
                    cache.get(folderName).get(0).path
            );
            folders.add(folder);
        }
        callback.onSuccess(folders, allPhotos);
        cache.clear();
    }


}
