package com.example.p2p.widget.customView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.example.p2p.R;


/**
 * 可以带有圆形和圆角现状的ImageView
 * Created by 陈健宇 at 2019/6/30
 */
public class ShapeImageView extends AppCompatImageView {

    private Bitmap mBitmap;
    private Paint mPaint;

    private int mFormat;
    private int mCorners;
    private int mRadius;

    public ShapeImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeImageView);
        mFormat = typedArray.getInt(R.styleable.ShapeImageView_format, 1);
        mRadius = typedArray.getDimensionPixelSize(R.styleable.ShapeImageView_radius, 0);
        mCorners = typedArray.getDimensionPixelSize(R.styleable.ShapeImageView_corners, 0);
        typedArray.recycle();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        setPaintShader();
        if(mFormat == 0){
            if(mRadius == 0) mRadius = getWidth() / 2;
            canvas.drawCircle(getWidth() >> 1, getHeight() >> 1, mRadius, mPaint);
            mPaint.setColor(Color.RED);
            mPaint.setShader(null);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(getWidth() >> 1, getHeight() >> 1, mRadius, mPaint);
        }else {
            canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), mCorners, mCorners, mPaint);
        }
    }

    private void setPaintShader() {
        Drawable drawable = this.getDrawable();
        if(drawable == null) throw new NullPointerException("the src image can't be null!");
        mBitmap = drawableToBitmap(drawable);
        BitmapShader bitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Matrix matrix = new Matrix();
        float scaleX = (float) getWidth() / mBitmap.getWidth();
        float scaleY = (float) getHeight() / mBitmap.getHeight();
        matrix.setScale(scaleX, scaleY);
        bitmapShader.setLocalMatrix(matrix);
        mPaint.setShader(bitmapShader);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
