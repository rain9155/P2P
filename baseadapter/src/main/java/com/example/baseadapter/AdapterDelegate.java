package com.example.baseadapter;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 每个item的AdapterDelegate
 * Created by 陈健宇 at 2019/5/30
 */
public abstract class AdapterDelegate<T>{


    /**
     * 判断给定的item是否属于当前AdapterDelegate
     * @param items position对应的item
     * @return true表示item是属于当前AdapterDelegate的实例，false表示不是
     */
    abstract boolean isForViewType(T items, int position);

     abstract BaseViewHolder onCreateViewHolder(ViewGroup parent);

     abstract void onBindView(BaseViewHolder holder, T items, int position);
}
