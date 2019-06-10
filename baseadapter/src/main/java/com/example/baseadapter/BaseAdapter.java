package com.example.baseadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baseadapter.mutiple.MutiItemDelegate;
import com.example.baseadapter.mutiple.MutiItemDelegateManager;

import java.util.List;

/**
 * Adapter基类
 * Created by 陈健宇 at 2019/5/30
 */
public class BaseAdapter<T> extends RecyclerView.Adapter<BaseViewHolder>{


    protected MutiItemDelegateManager<T> adapterDelegateManager;
    private static final int NO_DELEGTE = -1;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemChildClickListener mOnItemChildClickListener;
    private OnItemChildLongListener mOnItemChildLongListener;

    protected List<T> mDatas;
    protected int mLayoutId;

    public BaseAdapter(List<T> datas) {
        this(datas, -1);
    }

    public BaseAdapter(List<T> datas, int layoutId) {
        mDatas = datas;
        mLayoutId = layoutId;
        adapterDelegateManager = new MutiItemDelegateManager<>();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseViewHolder holder = null;
        if(viewType == NO_DELEGTE){
            holder = new BaseViewHolder(LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false));
        }else {
            holder = adapterDelegateManager.onCreateViewHolder(parent, viewType);
        }
        bindItemClickLisitener(holder);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        if(holder.getItemViewType() == NO_DELEGTE){
            onBindView(holder, mDatas.get(position));
        }else {
            adapterDelegateManager.onBindViewHolder(holder, mDatas.get(position), position);
        }
    }

    protected void onBindView(BaseViewHolder holder, T item){}

    @Override
    public int getItemViewType(int position) {
        if(adapterDelegateManager.getItemDelegateCount() == 0){
            return NO_DELEGTE;
        }else {
            return adapterDelegateManager.getItemViewType(mDatas.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    private void bindItemClickLisitener(final BaseViewHolder holder) {
        final View itemView = holder.getItemView();
        if(itemView == null) return;
        holder.setAdapter(this);
        if(mOnItemClickListener != null){
            itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(this, itemView, holder.getLayoutPosition()));
        }
        if(mOnItemLongClickListener != null){
            itemView.setOnLongClickListener(v -> mOnItemLongClickListener.onItemLongClick(this, itemView, holder.getLayoutPosition()));
        }
    }

    /**
     * 添加item的AdapterDelegte
     * @param delegate item的AdapterDelegte
     */
    public BaseAdapter<T> addItemAdapterDelegte(MutiItemDelegate<T> delegate){
        adapterDelegateManager.addDelegte(delegate);
        return this;
    }

    /**
     * 设置item的单击监听
     * @param onItemClickListener 单击监听接口实例
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * 设置item的长按监听
     * @param onItemLongClickListener 长按监听接口实例
     */
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener){
        mOnItemLongClickListener = onItemLongClickListener;
    }

    /**
     * 获得item子控件的单击监听接口实例
     */
    public OnItemChildClickListener getOnItemChildClickListener() {
        return mOnItemChildClickListener;
    }

    /**
     * 设置item的子控件单击监听
     * @param OnItemChildClickListener 单击监听接口实例
     */
    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        mOnItemChildClickListener = onItemChildClickListener;
    }

    /**
     * 获得item子控件的长按监听接口实例
     */
    public OnItemChildLongListener getOnItemChildLongListener() {
        return mOnItemChildLongListener;
    }

    /**
     * 设置item的子控件长按监听
     * @param OnItemChildLongListener 长按监听接口实例
     */
    public void setOnItemChildLongListener(OnItemChildLongListener onItemChildLongListener) {
        mOnItemChildLongListener = onItemChildLongListener;
    }

    /**
     * item单击事件监听接口
     */
    public interface OnItemClickListener{
        /**
         * item单击事件监听接口回调方法
         * @param adapter 适配器
         * @param view position对应的itemView
         * @param position itemView在源数据中的索引
         */
        void onItemClick(BaseAdapter adapter, View view, int position);
    }

    /**
     * item长按事件监听接口
     */
    public interface OnItemLongClickListener{
        /**
         * item长按事件监听接口回调方法
         * @param adapter 适配器
         * @param view position对应的itemView
         * @param position itemView在源数据中的索引
         * @return true表示itemView消费这个长按事件
         */
        boolean onItemLongClick(BaseAdapter adapter, View view, int position);
    }

    /**
     * item的子控件的单击接口
     */
    public interface OnItemChildClickListener{
        /**
         *  item的子控件的单击接口回调方法
         * @param adapter 适配器
         * @param view position对应的item的子控件
         * @param position itemView在源数据中的索引
         */
        void onItemChildClickListener(BaseAdapter adapter, View view, int position);
    }

    /**
     * item的子控件的长按接口
     */
    public interface OnItemChildLongListener{
        /**
         *  item的子控件的长按接口回调方法
         * @param adapter 适配器
         * @param view position对应的item的子控件
         * @param position itemView在源数据中的索引
         * @return true表示item的子控件消费这个长按事件
         */
        boolean onItemChildLongListener(BaseAdapter adapter, View view, int position);
    }

}
