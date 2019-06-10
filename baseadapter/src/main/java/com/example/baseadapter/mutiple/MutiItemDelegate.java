package com.example.baseadapter.mutiple;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.baseadapter.BaseViewHolder;

/**
 * 每个item的Delegate
 * Created by 陈健宇 at 2019/5/30
 */
public abstract class MutiItemDelegate<T>{


    /**
     * 判断给定的item是否要使用position位置的ViewType
     * @param items position对应的item
     * @param position item在数据源中的索引
     * @return true表示item要使用该position位置处的ViewType
     */
    protected abstract boolean isForViewType(T items, int position);

    protected abstract BaseViewHolder onCreateViewHolder(ViewGroup parent);

    protected abstract void onBindView(BaseViewHolder holder, T items, int position);
}
