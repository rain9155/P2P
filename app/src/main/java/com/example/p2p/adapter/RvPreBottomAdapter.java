package com.example.p2p.adapter;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.library.BaseAdapter;
import com.example.library.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.bean.Photo;

import java.util.List;

/**
 * 底部预览照片列表的Adapter
 * Created by 陈健宇 at 2019/9/30
 */
public class RvPreBottomAdapter extends BaseAdapter<Photo>{

    public RvPreBottomAdapter(List datas, int layoutId) {
        super(datas, layoutId);
        //清空所有选中照片的选择
        for(Photo image : mDatas){
            image.isSelect = false;
        }
    }

    @Override
    protected void onBindView(BaseViewHolder holder, Photo item) {
        Glide.with(holder.getItemView().getContext())
                .load(item.path)
                .into((ImageView) holder.getView(R.id.iv_photo));

        item.selectPos = holder.getAdapterPosition();

        View mark = holder.getView(R.id.iv_mark);
        if(item.isSelect){
            mark.setVisibility(View.VISIBLE);
        }else {
            mark.setVisibility(View.INVISIBLE);
        }
    }

    public void setSelectPhoto(Photo photo){
        for(Photo image : mDatas){
            image.isSelect = false;
            if(photo.equals(image)){
                image.isSelect = true;
            }
        }
        notifyDataSetChanged();
    }

    public void updateSelectPhoto(boolean isSelect, Photo photo){
        photo.isSelect = isSelect;
        if(isSelect){
            mDatas.add(photo);
        }else {
            mDatas.remove(photo);
        }
        notifyDataSetChanged();
    }

}
