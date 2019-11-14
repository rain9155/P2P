package com.example.myglide.request;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.myglide.R;
import com.example.myglide.callback.ResourceCallback;
import com.example.myglide.job.Engine;
import com.example.myglide.job.EngineDecodeJob;
import com.example.myglide.utils.Log;


/**
 * Created by 陈健宇 at 2019/11/5
 */
public class Request implements ResourceCallback {

    private static final String TAG = Request.class.getSimpleName();
    private static final int TAG_KEY = R.id.image_view;
    private int mOvrrideWidth;
    private int mOvrrideHeight;
    private int mWidth;
    private int mHeight;
    private ImageView mImageView;
    private String mModel;
    private Engine mEngine;
    private Context mContext;
    private Status mStatus = Status.PENDING;
    private Drawable mPlaceHolderDrawable;
    private Drawable mErrorDrawable;
    private boolean isSkipMemory;
    private boolean isSkipDisk;
    private EngineDecodeJob mEngineDecodeJob;

    private void init(
            Engine engine,
            Context context,
            int ovrrideWidth,
            int ovrrideHeight,
            int placeHolderId,
            int errorHolderId,
            boolean isSkipMemory,
            boolean isSkipDisk,
            ImageView imageView,
            String model
    ){
        this.mOvrrideWidth = ovrrideWidth;
        this.mOvrrideHeight = ovrrideHeight;
        this.mEngine = engine;
        this.mModel = model;
        this.mImageView = imageView;
        this.mContext = context;
        if(placeHolderId > 0){
            this.mPlaceHolderDrawable = ContextCompat.getDrawable(mContext, placeHolderId);
        }
        if(errorHolderId > 0){
            this.mErrorDrawable = ContextCompat.getDrawable(mContext, errorHolderId);
        }
        this.isSkipDisk = isSkipDisk;
        this.isSkipMemory = isSkipMemory;
    }

    public void begin(){
        if(mStatus == Status.RUNNING){
            throw new IllegalArgumentException("Cannot restart a running request");
        }
        if(mStatus == Status.COMPLETE){
            return;
        }
        mStatus = Status.RUNNING;
        onLoadStarted();
        if(!isSizeValid(mOvrrideWidth, mOvrrideHeight)){
            mWidth = mOvrrideWidth;
            mHeight = mOvrrideHeight;
        }else {
            mWidth = getImageViewWidth();
            mHeight = getImageViewHeight();
        }
        if(!isSizeValid(mWidth, mHeight)){
            getSize();
        }else {
            onSizeReady();
        }
    }

    private void getSize() {
        mImageView.post(new Runnable() {
            @Override
            public void run() {
                mWidth = getImageViewWidth();
                mHeight = getImageViewHeight();
                if(!isSizeValid(mWidth, mHeight)){
                    onLoadFailed();
                    return;
                }
                onSizeReady();
            }
        });
    }

    private boolean isSizeValid(int width, int height) {
        return width > 0 && height > 0;
    }

    private int getImageViewWidth(){
        return mImageView.getWidth() - mImageView.getPaddingLeft() - mImageView.getPaddingRight();
    }

    private int getImageViewHeight(){
        return mImageView.getHeight() - mImageView.getPaddingTop() - mImageView.getPaddingBottom();
    }



    @Override
    public void onResourceReady(Bitmap bitmap) {
        mStatus = Status.COMPLETE;
        String uri = (String) mImageView.getTag(TAG_KEY);
        if(uri.equals(mModel)){
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoadFailed() {
        mStatus = Status.FAILED;
        if(mErrorDrawable != null){
            mImageView.setImageDrawable(mErrorDrawable);
        }
    }

    private void onLoadStarted() {
        if(mPlaceHolderDrawable != null){
            mImageView.setImageDrawable(mPlaceHolderDrawable);
        }
    }

    private void onSizeReady() {
        mImageView.setTag(TAG_KEY, mModel);
        mEngineDecodeJob = mEngine.load(
                mModel,
                mWidth,
                mHeight,
                isSkipMemory,
                isSkipDisk,
                this);
    }

    public void cancel(){
        mStatus = Status.CANCEL;
        mImageView.setImageBitmap(null);
        if(mEngineDecodeJob != null){
            mEngineDecodeJob.cancel();
        }
    }

    public boolean isCancel(){
        return mStatus == Status.CANCEL;
    }

    public boolean isRunning(){
        return mStatus == Status.RUNNING;
    }

    public static class Builder{

        private Engine mEngine;
        private Context mContext;
        private RequestManager mRequestManager;
        private boolean isModelSet;
        private ImageView mImageView;
        private String mModel;
        private int mOvrrideWidth;
        private int mOvrrideHeight;
        private int mPlaceHolderId;
        private int mErrorHolderId;
        private boolean isSkipMemory;
        private boolean isSkipDisk;


        Builder(Engine engine, RequestManager requestManager, Context context){
            this.mEngine = engine;
            this.mContext = context;
            this.mRequestManager = requestManager;
        }

        public Builder ovrride(int width, int height){
            this.mOvrrideWidth = width;
            this.mOvrrideHeight = height;
            return this;
        }

        public Builder placeholder(@DrawableRes int placeholderId){
            this.mPlaceHolderId = placeholderId;
            return this;
        }

        public Builder error(@DrawableRes int errorId){
            this.mErrorHolderId = errorId;
            return this;
        }

        public Builder load(String model){
            this.mModel = model;
            isModelSet = true;
            return this;
        }

        public Builder skipMemoryCache(boolean isSkipMemory){
            this.isSkipMemory = isSkipMemory;
            return this;
        }

        public Builder skipDiskCache(boolean isSkipDisk){
            this.isSkipDisk = isSkipDisk;
            return this;
        }

        public void into(ImageView imageView){
            if(!isModelSet){
                throw new IllegalArgumentException("url cannot be null, you must call load()!");
            }
            this.mImageView = imageView;
            Request request = build();
            this.mRequestManager.track(request);
        }

        Request build(){
            Request request = new Request();
            request.init(
                    mEngine,
                    mContext,
                    mOvrrideWidth,
                    mOvrrideHeight,
                    mPlaceHolderId,
                    mErrorHolderId,
                    isSkipMemory,
                    isSkipDisk,
                    mImageView,
                    mModel
            );
            return request;
        }
    }

    private enum Status {

        //等待执行
        PENDING,

        //正在执行中
        RUNNING,

        //执行完成
        COMPLETE,

        //由于状态变化，取消请求
        CANCEL,

        //出现异常结束
        FAILED,

    }

    @NonNull
    @Override
    public String toString() {
        return "Request[model = " + mModel + "]";
    }
}

