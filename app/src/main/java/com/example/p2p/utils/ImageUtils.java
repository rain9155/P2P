package com.example.p2p.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/**
 * Created by 陈健宇 at 2019/6/20
 */
public class ImageUtils {

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

}
