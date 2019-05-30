package com.example.baseadapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter基类
 * Created by 陈健宇 at 2019/5/30
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseViewHolder>{


    private AdapterDelegateManager<T> mAdapterDelegateManager;
    private static final int NO_DELEGTE = -1;

    protected List<T> mDatas;
    protected int mLayoutId;
    protected abstract void onBindView(BaseViewHolder holder, T item);

    public BaseAdapter(List<T> datas, int layoutId) {
        mDatas = datas;
        mLayoutId = layoutId;
        mAdapterDelegateManager = new AdapterDelegateManager<>();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == NO_DELEGTE){
            return new BaseViewHolder(LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false));
        }else {
            return mAdapterDelegateManager.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        if(holder.getItemViewType() == NO_DELEGTE){
            onBindView(holder, mDatas.get(position));
        }else {
            mAdapterDelegateManager.onBindViewHolder(holder, mDatas.get(position), position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mAdapterDelegateManager.getItemDelegateCount() == 0){
            return NO_DELEGTE;
        }else {
            return mAdapterDelegateManager.getItemViewType(mDatas.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public BaseAdapter<T> addItemAdapterDelegte(AdapterDelegate<T> delegate){
        mAdapterDelegateManager.addDelegte(delegate);
        return this;
    }
}
