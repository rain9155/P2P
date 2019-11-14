package com.example.myglide.cache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 图片缓存的唯一标识
 * Created by 陈健宇 at 2019/11/13
 */
public class Key {

    private final String mSignature;
    private final int mWidth;
    private final int mHeight;
    private int mHashCode = 0;

    public Key(String url, int width, int height) {
        mSignature = hashString(url);
        mWidth = width;
        mHeight = height;
    }

    @Override
    public int hashCode() {
        if(mHashCode == 0){
            mHashCode = 31 * mSignature.hashCode() + 31 * mWidth + 31 * mHeight;
        }
        return mHashCode;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Key){
            Key otherKey = (Key) obj;
            return this.mHeight == otherKey.mHeight
                    && this.mWidth == otherKey.mWidth
                    && this.mSignature.equals(otherKey.mSignature);
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "Key[signature = " + mSignature
                + ", width = " + mWidth
                + ", height = " + mHeight;
    }

    /**
     * 把给定的字符串进行MD5编码
     * @return 编码后的字符串
     */
    public String hashString(String str){
        String cacheKey;
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes());
            cacheKey = bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(str.hashCode());
        }
        return cacheKey;
    }

    /**
     * 把字节数组转成16进制字符串
     * @param bytes 字节数组
     * @return 16进制字符串
     */
    public String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
