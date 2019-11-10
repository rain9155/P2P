package com.example.myglide.job;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.example.myglide.MyGlide;
import com.example.myglide.cache.DiskCache;
import com.example.myglide.cache.MemoryCache;
import com.example.myglide.callback.JobListener;
import com.example.myglide.callback.ResourceCallback;
import com.example.myglide.utils.FileUtil;
import com.example.myglide.utils.Log;
import com.example.utils.FileProvider7;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by 陈健宇 at 2019/11/7
 */
public class EngineDecodeJob implements Runnable{


    private static final String TAG = EngineDecodeJob.class.getSimpleName();

    private static final Set<String> HTTP_SCHEMES =  new HashSet<String>(){{
        add("http");
        add("https");
    }};
    private static final Set<String> LOCAL_SCHEMES =  new HashSet<String>(){{
        add(ContentResolver.SCHEME_FILE);
        add(ContentResolver.SCHEME_ANDROID_RESOURCE);
        add(ContentResolver.SCHEME_CONTENT);
    }};

    private static final int MAX_REDIRECT = 5;//最大重定向数
    private static final int TIME_OUT = 2500;
    private static final int MES_LOAD_COMPLETE = 0;
    private static final int MES_LOAD_FILED = 1;
    private static final int MES_LOAD_CANCEL = 2;
    private DiskCache mDiskCache;
    private MemoryCache mMemoryCache;
    private MyGlide mImageLoader;
    private String mKey;
    private int mWidth;
    private int mHeight;
    private String mUri;
    private Bitmap mBitmap;
    private ResourceCallback mCallback;
    private JobListener mJobListener;
    private volatile boolean isCancel;//是否取消请求，当RequestManager进入pause状态时，isCancel = true

    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MES_LOAD_COMPLETE:
                    if(!isCancel){
                        mJobListener.onJobComplete(mKey,  mBitmap);
                        mCallback.onResourceReady(mBitmap);
                    }else {
                        mJobListener.onJobCancelled(mKey);
                        isCancel = false;
                    }
                    break;
                case MES_LOAD_FILED:
                    if(!isCancel){
                        mJobListener.onJobComplete(mKey,  null);
                        mCallback.onLoadFailed();
                    }else {
                        mJobListener.onJobCancelled(mKey);
                        isCancel = false;
                    }
                    break;
                case MES_LOAD_CANCEL:
                    mJobListener.onJobCancelled(mKey);
                    isCancel = false;
                    break;
            }

        }
    };

    void init(
            MyGlide imageLoader,
            String key,
            int width,
            int height,
            String uri,
            JobListener jobListener)
    {
        this.mImageLoader = imageLoader;
        this.mDiskCache = imageLoader.getDiskCache();
        this.mMemoryCache = imageLoader.getMemoryCache();
        this.mKey = key;
        this.mJobListener = jobListener;
        this.mWidth = width;
        this.mHeight = height;
        this.mUri = uri;
    }

    void start(){
        mImageLoader.getExecutor()
                .execute(this);
    }

    public void setCallback(ResourceCallback callback){
        this.mCallback = callback;
    }

    public void cancel(){
        this.isCancel = true;
        this.mCallback = null;
    }

    public boolean isCancel(){
        return isCancel;
    }

    @Override
    public void run() {
        try {
            if(isCancel){
                mMainHandler.obtainMessage(MES_LOAD_CANCEL).sendToTarget();
                return;
            }
            mBitmap = loadSync();
            if(mBitmap != null){
                mMainHandler.obtainMessage(MES_LOAD_COMPLETE).sendToTarget();
            }else {
                mMainHandler.obtainMessage(MES_LOAD_FILED).sendToTarget();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "run(), error, e = " + e.getMessage());
            mMainHandler.obtainMessage(MES_LOAD_FILED).sendToTarget();
        }
    }

    private Bitmap loadSync() throws IOException {
        Bitmap bitmap = loadFromDiskCache(mKey, mWidth, mHeight);
        if (bitmap != null) {
            Log.d(TAG, "loadSync(), loadBitmapFromDisk, url = " + mUri);
            return bitmap;
        }
        InputStream inputStream = null;
        Uri unKnowUri = Uri.parse(mUri);
        if(!isCancel){
            if(HTTP_SCHEMES.contains(unKnowUri.getScheme())){
                Log.d(TAG, "loadSync(), loadBitmapFromHttp, uri = " + unKnowUri);
                URL url = new URL(mUri);
                inputStream = downloadFromServer(url, 0);
            }else if(LOCAL_SCHEMES.contains(unKnowUri.getScheme())){
                Log.d(TAG, "loadSync(), loadBitmapFromLocal, uri = " + unKnowUri);
                inputStream = loadFromLocal(unKnowUri);
            }else if(unKnowUri.getScheme() == null){
                Log.d(TAG, "loadSync(), loadBitmapFromLocal, uri = " + unKnowUri);
                Uri uri = FileProvider7.getUriForFile(mImageLoader.getContext(), new File(mUri));
                inputStream = loadFromLocal(uri);
            }
        }
        if(inputStream != null && !isCancel){
            bitmap = cacheData(inputStream);
        }
        return bitmap;
    }


    /**
     * 把图片流缓存到硬盘然后解码返回
     */
    private Bitmap cacheData(InputStream inputStream){
        if(mDiskCache != null){
            mDiskCache.put(mKey, inputStream);
        }
        if(isCancel){
            return null;
        }
        Bitmap bitmap = compressBitmap(
                BitmapFactory.decodeStream(inputStream),
                mWidth,
                mHeight);
        return bitmap;
    }


    /**
     * 从硬盘加载
     */
    private Bitmap loadFromDiskCache(String key, int reWidth, int reHeight) throws IOException {
        if(mDiskCache == null){
            return null;
        }
        if(isCancel){
            return null;
        }
        InputStream inputStream = mDiskCache.get(key);
        Bitmap bitmap = null;
        if(inputStream != null && !isCancel){
            bitmap = decodeBitmapFromFileInputStream(
                    (FileInputStream)inputStream,
                    reWidth,
                    reHeight);
            if(mMemoryCache != null && bitmap != null){
                mMemoryCache.put(key, bitmap);
            }
        }
        return bitmap;
    }

    /**
     * 从本地加载图片
     * @param uri 图片的uri
     */
    private InputStream loadFromLocal(Uri uri) throws FileNotFoundException {
        InputStream inputStream = mImageLoader.getContext()
                .getContentResolver().openInputStream(uri);
        if(isCancel) {
            return null;
        }
        return inputStream;
    }

    /**
     * 从网络加载图片
     * @param url 图片url
     * @param redirect 第几次请求网络
     */
    private InputStream downloadFromServer(URL url, int redirect) throws IOException {
        if(redirect > MAX_REDIRECT){
           return null;
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(TIME_OUT);
        connection.setReadTimeout(TIME_OUT);
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        if(isCancel){
            return null;
        }
        int statusCode = connection.getResponseCode();
        if(isHttpOk(statusCode)){
            return inputStream;
        }else if(isHttpRedirect(statusCode)){
            String redirectUrl = connection.getHeaderField("Location");
            if(TextUtils.isEmpty(redirectUrl)){
                throw new IOException("redirectUrl is null");
            }
            FileUtil.close(inputStream);
            connection.disconnect();
            return downloadFromServer(new URL(url, redirectUrl), redirect + 1);
        }else {
            throw new IOException(connection.getResponseMessage() + ", statuscode = " + statusCode);
        }
    }

    private boolean isHttpOk(int statusCode) {
        return statusCode / 100 == 2;
    }

    private boolean isHttpRedirect(int statusCode){
        return statusCode / 100 == 3;
    }


    /**
     * 从资源文件解析图片
     * @param resources 资源文件
     * @param resId 资源文件id
     * @param reWidth 图片希望的宽
     * @param reHeight 图片希望的高
     * @return 返回压缩后的图片
     */
    public static Bitmap decodeBitmapFromResources(Resources resources, int resId, int reWidth, int reHeight){
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);
        //计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reWidth, reHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resId, options);
    }

    /**
     * 从文件加载图片
     * @param pathName 文件路径
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    /**
     * 从文件输入流解析图片
     * @param fileInputStream 文件输入流
     * @throws IOException
     */
    public static Bitmap decodeBitmapFromFileInputStream(FileInputStream fileInputStream, int reWidth, int reHeight) throws IOException {
        return decodeBitmapFromFileDescriptor(fileInputStream.getFD(), reWidth, reHeight);
    }

    /**
     * 通过文件描述符加载图片
     * @param fileDescriptor 文件描述符
     */
    public static Bitmap decodeBitmapFromFileDescriptor(FileDescriptor fileDescriptor, int reWidth, int reHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        options.inSampleSize = calculateInSampleSize(options, reWidth, reHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    /**
     * 从文件输入流解析图片，不要求宽高
     * @param fileInputStream 文件输入流
     * @throws IOException
     */
    public static Bitmap decodeBitmapFromFileInputStream(FileInputStream fileInputStream) throws IOException {
        return decodeBitmapFromFileDescriptor(fileInputStream.getFD());
    }

    /**
     * 通过文件描述符加载图片，不要求宽高
     * @param fileDescriptor 文件描述符
     */
    public static Bitmap decodeBitmapFromFileDescriptor(FileDescriptor fileDescriptor){
        return BitmapFactory.decodeFileDescriptor(fileDescriptor);
    }

    /**
     * 计算图片的inSampleSize
     * @param options BitmapFactory.Options
     * @param reWidth 希望图片的宽
     * @param reHeight 希望图片的高
     * @return 计算后的inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reWidth, int reHeight){
        if(reHeight <= 0 || reWidth <= 0){
            return 1;
        }
        //得到原始宽高
        final int originalWidth = options.outWidth;
        final int originalHeight = options.outHeight;
        int inSampleSize = 1;//大于1图片才有缩放效果
        if(originalHeight > reHeight || originalWidth > reWidth){
            final int halfHeight = originalHeight / 2;
            final int halfWidth = originalWidth / 2;
            //计算出最大inSampleSize保证originalWidth和originalHeight都不会小于reWidth和reHeight
            while (halfWidth / inSampleSize >= reWidth && halfHeight / inSampleSize >= reHeight){
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 缩放一张图片,也有压缩的效果
     * @param origin 原始图片
     * @param  newWidth 期待的宽
     * @param newHeight 期待的高
     */
    public static Bitmap compressBitmap(Bitmap origin, int newWidth, int newHeight){
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        float ratioX = (float) newWidth / width;
        float ratioY = (float) newHeight / height;
        matrix.setScale(ratioX, ratioY);
        Bitmap newBitmap = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, true);
        if (newBitmap.equals(origin)) {
            return newBitmap;
        }
        origin.recycle();
        return newBitmap;

    }

}
