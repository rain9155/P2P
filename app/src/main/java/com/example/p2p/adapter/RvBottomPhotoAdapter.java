package com.example.p2p.adapter;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.library.BaseAdapter;
import com.example.library.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.bean.Photo;
import com.example.utils.CommonUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * 底部预览照片列表的Adapter
 * Created by 陈健宇 at 2019/9/30
 */
public class RvBottomPhotoAdapter extends BaseAdapter<Photo>{

    private List<Photo> mUnSelectedPhotos;

    public RvBottomPhotoAdapter(List datas, int layoutId) {
        super(datas, layoutId);
        mUnSelectedPhotos = new LinkedList<>();
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

    /**
     * 设置选中的照片
     */
    public void setSelectPhoto(Photo photo){
        for(Photo image : mDatas){
            image.isSelect = false;
            if(photo.equals(image)){
                image.isSelect = true;
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 选中照片或移除选中照片，并从列表中添加或移除
     */
    public void updateSelectPhoto(boolean isSelect, Photo photo){
        photo.isSelect = isSelect;
        if(isSelect){
            //如果添加的photo是来自删除列表中的，直接把它放回选择列表的原位
            for(Photo image : mUnSelectedPhotos){
                if(image.equals(photo)){
                    mDatas.add(image.selectPos, photo);
                    mUnSelectedPhotos.remove(image);
                    notifyDataSetChanged();
                    return;
                }
            }
            mDatas.add(photo);
        }else {
            mDatas.remove(photo);
            mUnSelectedPhotos.add(new Photo(photo));
        }
        notifyDataSetChanged();
    }

    public List<Photo> getUnSelectedPhotos(){
        return mUnSelectedPhotos;
    }

}
