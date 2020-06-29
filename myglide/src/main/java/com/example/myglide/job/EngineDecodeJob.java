package com.example.myglide.job;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.example.myglide.MyGlide;
import com.example.myglide.cache.DiskCache;
import com.example.myglide.cache.Key;
import com.example.myglide.callback.JobCallback;
import com.example.myglide.callback.ResourceCallback;
import com.example.myglide.utils.FileUtil;
import com.example.myglide.utils.Log;
import com.example.utils.FileProvider7;
import com.example.utils.ImageUtils;

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
    private MyGlide mImageLoader;
    private Key mKey;
    private int mWidth;
    private int mHeight;
    private String mUri;
    private Bitmap mBitmap;
    private ResourceCallback mCallback;
    private JobCallback mJobListener;
    private boolean isSkipMemory;
    private boolean isSkipDisk;
    private volatile boolean isCancel;//是否取消请求，当RequestManager进入pause状态时，isCancel = true

    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MES_LOAD_COMPLETE:
                    if(!isCancel){
                        mJobListener.onJobComplete(mKey, mBitmap, isSkipMemory);
                        mCallback.onResourceReady(mBitmap);
                    }else {
                        mJobListener.onJobCancelled(mKey);
                        isCancel = false;
                    }
                    break;
                case MES_LOAD_FILED:
                    if(!isCancel){
                        mJobListener.onJobComplete(mKey,  null, isSkipMemory);
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
            Key key,
            int width,
            int height,
            String uri,
            boolean isSkipMemory,
            boolean isSkipDisk,
            JobCallback jobListener)
    {
        this.mImageLoader = imageLoader;
        this.mDiskCache = imageLoader.getDiskCache();
        this.mKey = key;
        this.mJobListener = jobListener;
        this.mWidth = width;
        this.mHeight = height;
        this.mUri = uri;
        this.isSkipMemory = isSkipMemory;
        this.isSkipDisk = isSkipDisk;
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
        Bitmap bitmap = loadFromDiskCache(mKey);
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
                Uri uri = FileProvider7.getUriForFile(
                        mImageLoader.getContext(),
                        new File(mUri));
                inputStream = loadFromLocal(uri);
            }
        }
        if(inputStream != null && !isCancel){
            bitmap = cacheData(inputStream);
        }
        return bitmap;
    }


    /**
     * 把图片流先缓存到硬盘，然后读取硬盘缓存返回
     * 如果没有开启硬盘缓存，直接解码压缩返回
     */
    private Bitmap cacheData(InputStream inputStream) throws IOException {
        if(!isSkipDisk && mDiskCache != null){
            mDiskCache.put(mKey, inputStream);
            return loadFromDiskCache(mKey);
        }
        if(isCancel){
            return null;
        }
        if(inputStream instanceof FileInputStream){
            return decodeBitmapFromFileInputStream(
                    (FileInputStream) inputStream,
                    mWidth,
                    mHeight);
        }else {
            return decodeBitmapFromInputStream(
                    inputStream,
                    mWidth,
                    mHeight);
        }
    }

    /**
     * 从硬盘加载
     */
    private Bitmap loadFromDiskCache(Key key) throws IOException {
        if(isSkipDisk){
            return null;
        }
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
                    mWidth,
                    mHeight);
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
     * 通过流加载图片
     * @param inputStream 图片流
     */
    public  Bitmap decodeBitmapFromInputStream(InputStream inputStream, int reWidth, int reHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        calculateScaling(options, reWidth, reHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(inputStream, null, options);
    }


    /**
     * 从文件输入流解析图片
     * @param fileInputStream 文件输入流
     * @throws IOException
     */
    public Bitmap decodeBitmapFromFileInputStream(FileInputStream fileInputStream, int reWidth, int reHeight) throws IOException {
        return decodeBitmapFromFileDescriptor(fileInputStream.getFD(), reWidth, reHeight);
    }

    /**
     * 通过文件描述符加载图片
     * @param fileDescriptor 文件描述符
     */
    public Bitmap decodeBitmapFromFileDescriptor(FileDescriptor fileDescriptor, int reWidth, int reHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        calculateScaling(options, reWidth, reHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }


    /**
     * Bitmap的像素大小(占用内存大小)计算公式：((width * scale)/ inSampleSize) * ((height * scale)/ inSampleSize) * inPreferredConfig，其中scale = inTargetDensity / inDensity
     * inSampleSize：图片的像素点采用率，如果inSampleSize = 2，则像素点大小为原来的1/4
     * inTargetDensity：设备的像素密度，等于DisplayMetrics.densityDpi
     * inDensity: 图片的像素密度，等于图片对应drawable目录下的dpi大小
     * inPreferredConfig：图片的像素点大小，如ARGB_4444，一个像素点占用的16byte，即2b
     * 更多参考：https://www.cnblogs.com/nimorl/p/8065071.html 和 https://blog.csdn.net/haozipi/article/details/47185917
     *
     * calculateScaling方法用来计算图片的inSampleSize和图片的缩放比例scale和图片的像素点大小inPreferredConfig
     * 这样在加载图片进入内存时，可以减少图片的大小，防止出现OOM
     * @param options BitmapFactory.Options
     * @param reWidth 希望图片的宽
     * @param reHeight 希望图片的高
     */
    private void calculateScaling(BitmapFactory.Options options, int reWidth, int reHeight){
        if(reHeight <= 0 || reWidth <= 0){
            return;
        }

        //得到原始宽高
        final int originalWidth = options.outWidth;
        final int originalHeight = options.outHeight;

        /*  计算inSampleSize */

        //大于1图片才有缩放效果
        int inSampleSize = 1;
        //下面这个inSampleSize计算，是官方给出的模板，inSampleSize最终会取接近2的幂次方的数
        if(originalHeight > reHeight || originalWidth > reWidth){
            final int halfHeight = originalHeight / 2;
            final int halfWidth = originalWidth / 2;
            //计算出最大inSampleSize保证originalWidth和originalHeight都不会小于reWidth和reHeight
            while (halfWidth / inSampleSize >= reWidth && halfHeight / inSampleSize >= reHeight){
                inSampleSize *= 2;
            }
        }
        options.inSampleSize = inSampleSize;

        /* 计算Bitmap的inTargetDensity和inDensity */

        //计算出图片根据inSampleSize按比例减少后的宽高
        final int outWidth = round(originalWidth / (float)inSampleSize);
        final int outHeight = round(originalHeight / (float)inSampleSize);
        //计算出图片的缩放系数,即图片根据inSampleSize按比例减少后的宽高与request相差多少，大了还是小了
        final float widthScale = outWidth / (float)reWidth;
        final float heightScale = outHeight / (float)reHeight;
        //取最小的缩放系数，这样保证最终图片等比例缩放至（靠近）request大小
        final float scaleFactor = Math.min(widthScale, heightScale);
        int targetDensity = mImageLoader.getContext().getResources().getDisplayMetrics().densityDpi;
        //因为scale = inTargetDensity / inDensity = targetDensity / (targetDensity * scaleFactor)
        // 所以scale = 1 / scaleFactor
        options.inTargetDensity = targetDensity;
        options.inDensity = (int) (targetDensity * scaleFactor);
        if(isScaling(options)){
            options.inScaled = true;
        }else {
            options.inDensity = options.inTargetDensity = 0;
        }

        /* 设置Bitmap的像素点大小 */
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
    }

    /**
     * 判断Bitmap是否需要进行缩放
     */
    private boolean isScaling(BitmapFactory.Options options) {
        return options.inTargetDensity > 0 && options.inDensity > 0 && options.inTargetDensity != options.inDensity;
    }

    /**
     * 对传进来的数进行四舍五入返回
     */
    private int round(double value) {
        return (int) (value + 0.5d);
    }

}
