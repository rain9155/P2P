package com.example.p2p.adapter.delegte;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.baseadapter.BaseViewHolder;
import com.example.baseadapter.mutiple.MutiItemDelegate;
import com.example.p2p.R;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;
import com.example.p2p.config.Constant;

/**
 * 接受图片的item
 * Created by 陈健宇 at 2019/6/24
 */
public class ImageReceiveDelegte implements MutiItemDelegate<Mes> {
    @Override
    public boolean isForViewType(Mes items, int position) {
        return items.id == ItemType.RECEIVE_IMAGE;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_receive_image, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindView(BaseViewHolder holder, Mes items, int position) {

    }
}
