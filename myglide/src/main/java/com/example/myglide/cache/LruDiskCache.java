package com.example.myglide.cache;

import android.content.Context;

import com.example.myglide.utils.FileUtil;
import com.example.myglide.utils.Log;
import com.example.myglide.utils.Util;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 实现硬盘缓存：
 * 用于缓存下载好的图片，在硬盘缓存达到设定值时会将最近最久未使用的图片从硬盘中移除
 * Created by 陈健宇 at 2018/9/19
 */
public class LruDiskCache implements DiskCache{

    private final static String TAG = "LruDiskCache";

    private DiskLruCache mDiskLruCache;
    private static final int IO_BUFFER_SIZE = 1024 * 8;//文件流的缓冲区大小
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 100;//硬盘缓存的最大值，50MB
    private static final int DISK_CACHE_INDEX = 0;
    private static final String DISK_FILE_NAME = "diskBitmap";
    private boolean isDiskLruCacheCreated;
    private Context mContext;

    public LruDiskCache(Context context) {
        mContext = context;
        File diskCacheDir = FileUtil.getDiskCacheDir(mContext, DISK_FILE_NAME);
        if(!diskCacheDir.exists()){
            diskCacheDir.mkdirs();
        }
        long usableSpace = FileUtil.getUsableSpace(diskCacheDir);

        Log.d(TAG, "LruDiskCache, diskCacheDir = " + diskCacheDir
                + ", usableSpace = " + (usableSpace / 1024 / 1024) + "MB");

        //硬盘可用空间得大于硬盘缓存值
        if(usableSpace > DISK_CACHE_SIZE){
            try {
                mDiskLruCache = DiskLruCache.open(
                        diskCacheDir,
                        Util.getAppVersion(mContext),
                        1,
                        DISK_CACHE_SIZE);
                isDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isDisCacheCreated() {
        return isDiskLruCacheCreated;
    }

    @Override
    public void put(Key key, InputStream inputStream) {
        BufferedInputStream in = null;
        BufferedOutputStream os = null;
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(getSafeKey(key));
            if(editor != null){
                in = new BufferedInputStream(inputStream, IO_BUFFER_SIZE);
                os = new BufferedOutputStream(editor.newOutputStream(DISK_CACHE_INDEX));
                int b;
                while ((b = in.read()) != -1){
                    os.write(b);
                }
                editor.commit();
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "LruDiskCache, put, e = " + e.getMessage());
        }finally {
            FileUtil.close(in);
            FileUtil.close(os);
        }
    }

    @Override
    public InputStream get(Key key) {
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(getSafeKey(key));
                if(snapshot != null){
                return snapshot.getInputStream(DISK_CACHE_INDEX);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "LruDiskCache, getInstance, e = " + e.getMessage());
        }
        return null;
    }

    @Override
    public void remove(Key key) {
        try {
            mDiskLruCache.remove(getSafeKey(key));
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "LruDiskCache, remove, e = " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        try {
            mDiskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "LruDiskCache, clear, e = " + e.getMessage());
        }
    }

    private String getSafeKey(Key key){
        return key.hashString(key.toString());
    }
}
