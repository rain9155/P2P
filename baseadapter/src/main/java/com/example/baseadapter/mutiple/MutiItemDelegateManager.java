package com.example.baseadapter.mutiple;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.baseadapter.BaseViewHolder;

/**
 * 管理所有的item的AdapterDelegate的Manager
 * Created by 陈健宇 at 2019/5/30
 */
public class MutiItemDelegateManager<T>{

    private SparseArray<MutiItemDelegate<T>> mDelegates = new SparseArray<>();

    /**
     * 添加一个item的delegate, 默认以delegates的size作为viewType
     * @param delegate item的delegate
     */
    public MutiItemDelegateManager<T> addDelegte(@NonNull MutiItemDelegate<T> delegate){
        return addDelegte(mDelegates.size(), delegate);
    }

    /**
     * 根据给定的viewType，添加一个item的delegate
     * @param viewType 指定的viewType
     * @param delegate item的delegate
     */
    public MutiItemDelegateManager<T> addDelegte(int viewType, @NonNull MutiItemDelegate<T> delegate){
        if(delegate == null) throw new NullPointerException("MutiItemDelegate can't not be null");
        if(mDelegates.get(viewType) != null) throw new IllegalArgumentException("An MutiItemDelegate is already added for the viewType =" + viewType);
        mDelegates.put(viewType, delegate);
        return this;
    }

    /**
     * 根据给定的delegate，从Manager中移除它
     * @param delegate 给定的delegate
     */
    public MutiItemDelegateManager<T> removeDelegate(@NonNull MutiItemDelegate<T> delegate) {
        if (delegate == null) throw new NullPointerException("MutiItemDelegate is null");
        int indexToRemove = mDelegates.indexOfValue(delegate);
        if (indexToRemove >= 0) {
            mDelegates.removeAt(indexToRemove);
        }
        return this;

    }

    /**
     * 根据给定的viewType，从Manager中移除viewType对应的elegate
     * @param viewType 给定的viewType
     */
    public MutiItemDelegateManager<T> removeDelegate(int viewType) {
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
            MutiItemDelegate<T> delegate = mDelegates.valueAt(i);
            if (delegate.isForViewType(item, position)) {
                return mDelegates.keyAt(i);
            }
        }
        throw new NullPointerException("No MutiItemDelegate added that matches position = " + position + " in data source");
    }


    /**
     * 获得设置delegate的数量
     */
    public int getItemDelegateCount(){
        return mDelegates.size();
    }

    @NonNull
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MutiItemDelegate<T> delegate = mDelegates.get(viewType);
        BaseViewHolder viewHolder = delegate.onCreateViewHolder(parent);
        return viewHolder;
    }

    public void onBindViewHolder( @NonNull BaseViewHolder holder, @NonNull T items, int position) {
        MutiItemDelegate<T> delegate = mDelegates.get(holder.getItemViewType());
        delegate.onBindView(holder, items, position);
    }

}
