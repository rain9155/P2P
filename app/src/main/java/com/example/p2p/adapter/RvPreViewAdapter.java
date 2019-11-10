package com.example.p2p.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.library.BaseAdapter;
import com.example.library.BaseViewHolder;
import com.example.myglide.MyGlide;
import com.example.p2p.R;
import com.example.p2p.bean.Photo;

import java.util.List;

/**
 * 预览照片列表的Adapter
 * Created by 陈健宇 at 2019/10/8
 */
public class RvPreViewAdapter extends BaseAdapter<Photo>{

    public RvPreViewAdapter(List<Photo> datas, int layoutId) {
        super(datas, layoutId);
    }

    @Override
    protected void onBindView(BaseViewHolder holder, Photo item) {
//        Glide.with(holder.getItemView().getContext())
//                .load(item.path)
//                .into((ImageView) holder.getView(R.id.iv_photo));
        MyGlide.with(holder.getItemView().getContext())
                .load(item.path)
                .into(holder.getView(R.id.iv_photo));
    }
}
