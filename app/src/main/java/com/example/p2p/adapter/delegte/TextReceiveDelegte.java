package com.example.p2p.adapter.delegte;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.baseadapter.BaseViewHolder;
import com.example.baseadapter.mutiple.MutiItemDelegate;
import com.example.p2p.R;
import com.example.p2p.bean.Mes;
import com.example.p2p.config.Constant;

/**
 * 接受消息的item
 * Created by 陈健宇 at 2019/6/10
 */
public class TextReceiveDelegte extends MutiItemDelegate<Mes>{
    @Override
    protected boolean isForViewType(Mes items, int position) {
        return items.id == Constant.TYPE_ITEM_RECEIVE_TEXT;
    }

    @Override
    protected BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_receive_text, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    protected void onBindView(BaseViewHolder holder, Mes items, int position) {
        holder.setText(R.id.tv_message, (String) items.data)
                .setText(R.id.tv_name, items.name);
    }
}
