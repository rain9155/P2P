package com.example.p2p.adapter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.library.BaseAdapter;
import com.example.library.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.bean.Folder;

import java.util.List;

/**
 * 文件夹的Adapter
 * Created by 陈健宇 at 2019/9/29
 */
public class RvFolderAdapter extends BaseAdapter<Folder>{

    private int mPrePosition;

    public RvFolderAdapter(List<Folder> datas, int layoutId) {
        super(datas, layoutId);
    }

    @Override
    protected void onBindView(BaseViewHolder holder, Folder item) {
        Glide.with(holder.getItemView())
                .load(item.coverPath)
                .into((ImageView) holder.getView(R.id.iv_photo));

        holder.setText(R.id.tv_count, String.valueOf(item.photos.size()))
                .setText(R.id.tv_title, item.name);

        ImageButton ibSelect = holder.getView(R.id.ib_select_folder);
        if(item.isSelect){
            ibSelect.setVisibility(View.VISIBLE);
        }else {
            ibSelect.setVisibility(View.INVISIBLE);
        }
    }

    public void updateFolderByPos(boolean isSelect, int pos){
        if(pos == mPrePosition) return;
        mDatas.get(mPrePosition).isSelect = !isSelect;
        this.notifyItemChanged(mPrePosition);
        mDatas.get(pos).isSelect = isSelect;
        this.notifyItemChanged(pos);
        mPrePosition = pos;
    }

    public int getPrePosition(){
        return mPrePosition;
    }

    public void setNewFolders(List<Folder> folders){
        mDatas.clear();
        mDatas.addAll(folders);
        notifyDataSetChanged();
    }

}
