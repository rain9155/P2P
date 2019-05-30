package com.example.baseadapter;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

/**
 * 管理所有的item的AdapterDelegate的Manager
 * Created by 陈健宇 at 2019/5/30
 */
public class AdapterDelegateManager<T>{


    private SparseArray<AdapterDelegate<T>> mDelegates = new SparseArray<>();

    /**
     * 添加一个item的AdapterDelegate, 默认以delegates的size作为viewType
     * @param delegate item的AdapterDelegate
     */
    public AdapterDelegateManager<T> addDelegte(@NonNull AdapterDelegate<T> delegate){
        return addDelegte(mDelegates.size(), delegate);
    }

    /**
     * 根据给定的viewType，添加一个item的AdapterDelegate
     * @param viewType 指定的viewType
     * @param delegate item的AdapterDelegate
     */
    public AdapterDelegateManager<T> addDelegte(int viewType, @NonNull AdapterDelegate<T> delegate){
        if(delegate == null) throw new NullPointerException("AdapterDelegate can't not be null");
        if(mDelegates.get(viewType) != null) throw new IllegalArgumentException("An AdapterDelegate is already added for the viewType =" + viewType);
        mDelegates.put(viewType, delegate);
        return this;
    }

    /**
     * 根据给定的AdapterDelegate，从Manager中移除它
     * @param delegate 给定的AdapterDelegate
     */
    public AdapterDelegateManager<T> removeDelegate(@NonNull AdapterDelegate<T> delegate) {
        if (delegate == null) throw new NullPointerException("AdapterDelegate is null");
        int indexToRemove = mDelegates.indexOfValue(delegate);
        if (indexToRemove >= 0) {
            mDelegates.removeAt(indexToRemove);
        }
        return this;

    }

    /**
     * 根据给定的viewType，从Manager中移除viewType对应的AdapterDelegate
     * @param viewType 给定的viewType
     */
    public AdapterDelegateManager<T> removeDelegate(int viewType) {
        mDelegates.remove(viewType);
        return this;
    }

    /**
     * 根据给定的item得到对应的viewType
     * @param item position对应的item
     * @param position item在数据源中的索引
     * @return item对应的viewType
     */
    public int getItemViewType(@NonNull T item, int position) {
        if (item == null) throw new NullPointerException("Item is null!");
        int delegatesCount = mDelegates.size();
        for (int i = 0; i < delegatesCount; i++) {
            AdapterDelegate<T> delegate = mDelegates.valueAt(i);
            if (delegate.isForViewType(item, position)) {
                return mDelegates.keyAt(i);
            }
        }
        throw new NullPointerException("No AdapterDelegate added that matches position=" + position + " in data source");
    }


    /**
     * 获得设置delegate的数量
     */
    public int getItemDelegateCount(){
        return mDelegates.size();
    }

    public @NonNull BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdapterDelegate<T> delegate = mDelegates.get(viewType);
        BaseViewHolder viewHolder = delegate.onCreateViewHolder(parent);
        return viewHolder;
    }

    public void onBindViewHolder( @NonNull BaseViewHolder holder, @NonNull T items, int position) {
        AdapterDelegate<T> delegate = mDelegates.get(holder.getItemViewType());
        delegate.onBindView(holder, items, position);
    }
}
