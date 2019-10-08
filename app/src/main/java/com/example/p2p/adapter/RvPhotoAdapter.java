package com.example.p2p.adapter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.library.BaseAdapter;
import com.example.library.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.app.App;
import com.example.p2p.bean.Photo;
import com.example.p2p.config.Constant;
import com.example.utils.ToastUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by 陈健宇 at 2019/9/27
 */
public class RvPhotoAdapter extends BaseAdapter<Photo> {

    private List<Photo> mSelectPhotos;


    public RvPhotoAdapter(List<Photo> datas, int layoutId) {
        super(datas, layoutId);
        mSelectPhotos = new LinkedList<>();
    }

    @Override
    protected void onBindView(BaseViewHolder holder, Photo item) {
        Glide.with(holder.getItemView())
                .load(item.path)
                .into((ImageView) holder.getView(R.id.iv_photo));

        holder.setChildClickListener(R.id.ib_select_photo);
        int pos = holder.getAdapterPosition();
        mDatas.get(pos).position = pos + 1;

        ImageButton ibSelect = holder.getItemView().findViewById(R.id.ib_select_photo);
        View mark = holder.getItemView().findViewById(R.id.mark);
        if(item.isSelect){
            ibSelect.setSelected(true);
            mark.setVisibility(View.VISIBLE);
        }else {
            ibSelect.setSelected(false);
            mark.setVisibility(View.INVISIBLE);
        }
    }

    public int getSelectPhotoCount(){
        return mSelectPhotos.size();
    }

    public List<Photo> getSelectPhotos(){
        return new LinkedList<>(mSelectPhotos);
    }

    public void updatePhotoByPos(boolean isSelect, int pos, Photo photo){
        if(isSelect){
            mSelectPhotos.add(photo);
        }else {
            mSelectPhotos.remove(photo);
        }
        mDatas.get(pos).isSelect = isSelect;
        this.notifyItemChanged(pos);
    }

    public void setNewPhotos(List<Photo> photos){
        mDatas.clear();
        mDatas.addAll(photos);
        notifyDataSetChanged();
    }

}
