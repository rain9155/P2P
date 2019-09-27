package com.example.p2p.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.library.BaseAdapter;
import com.example.library.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.bean.Photo;

import java.util.List;

/**
 * Created by 陈健宇 at 2019/9/27
 */
public class RvPhotoAdapter extends BaseAdapter<Photo> {

    public RvPhotoAdapter(List<Photo> datas, int layoutId) {
        super(datas, layoutId);
    }

    @Override
    protected void onBindView(BaseViewHolder holder, Photo item) {
        Glide.with(holder.getItemView())
                .load(item.path)
                .into((ImageView) holder.getView(R.id.iv_photo));
    }
}
