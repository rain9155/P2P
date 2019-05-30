package com.example.baseadapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ViewHolder基类
 * Created by 陈健宇 at 2019/5/30
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {

    private SparseArrayCompat<View> mViews;//缓存itemView中所有的子View
    private View mItemView;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        mItemView = itemView;
        mViews = new SparseArrayCompat<>();
    }


    public BaseViewHolder setText(int id, String text){
        TextView textView = getView(id);
        textView.setText(text);
        return this;
    }

    public BaseViewHolder setImageResource(int id, int imageId){
        ImageView imageView = getView(id);
        imageView.setImageResource(imageId);
        return this;
    }

    public BaseViewHolder setImageBitmap(int id, Bitmap bitmap){
        ImageView imageView = getView(id);
        imageView.setImageBitmap(bitmap);
        return this;
    }

    public BaseViewHolder setImageDrawable(int id, Drawable drawable){
        ImageView imageView = getView(id);
        imageView.setImageDrawable(drawable);
        return this;
    }

    public BaseViewHolder setBackgroundResource(int id, int backgroundResId){
        View view = getView(id);
        view.setBackgroundResource(backgroundResId);
        return this;
    }

    public BaseViewHolder setBackgroundDrawable(int id, Drawable drawable){
        View view = getView(id);
        view.setBackground(drawable);
        return this;
    }

    public BaseViewHolder setBackgroundColor(int id, @ColorInt int color){
        View view = getView(id);
        view.setBackgroundColor(color);
        return this;
    }

    /**
     * 通过id从缓存中获取view实例，如果缓存没有，就从itemView中获取
     * @param id 要获取的view的id
     * @return 获取到的view
     */
    public <T extends View> T getView(int id){
        View view = mViews.get(id);
        if(view == null){
            view = mItemView.findViewById(id);
            mViews.put(id, view);
        }
        return (T) view;
    }

}
