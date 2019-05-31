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
    private BaseAdapter mAdapter;

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

    public BaseViewHolder setChildClickListener(int id){
        View view = getView(id);
        if(view == null) return this;
        if(!view.isClickable()) view.setClickable(true);
        view.setOnClickListener( v -> {
            if(mAdapter.getOnItemChildClickListener() != null){
                mAdapter.getOnItemChildClickListener().onItemChildClickListener(mAdapter, view, this.getLayoutPosition());
            }
        });
        return this;
    }

    public BaseViewHolder setChildClickListener(int id, View.OnClickListener listener){
        View view = getView(id);
        if(view == null) return this;
        view.setOnClickListener(listener);
        return this;
    }

    public BaseViewHolder setChildLongListener(int id){
        View view = getView(id);
        if(view == null) return this;
        if(!view.isClickable()) view.setClickable(true);
        view.setOnLongClickListener( v -> {
            if(mAdapter.getOnItemChildLongListener() != null){
               return mAdapter.getOnItemChildLongListener().onItemChildLongListener(mAdapter, view, this.getLayoutPosition());
            }
            return false;
        });
        return this;
    }

    public BaseViewHolder setChildLongListener(int id, View.OnLongClickListener listener){
        View view = getView(id);
        if(view == null) return this;
        view.setOnLongClickListener(listener);
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

    /**
     * 获得itemView实例
     * @return
     */
    public View getItemView() {
        return mItemView;
    }

    /**
     * 设置适配器实例给BaseHolder
     * @param adapter 适配器
     */
    public void setAdapter(BaseAdapter adapter){
        mAdapter = adapter;
    }


}
