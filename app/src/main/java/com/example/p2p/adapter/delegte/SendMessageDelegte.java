package com.example.p2p.adapter.delegte;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.baseadapter.BaseViewHolder;
import com.example.baseadapter.mutiple.MutiItemDelegate;
import com.example.p2p.R;
import com.example.p2p.bean.Message;
import com.example.p2p.config.Constant;

/**
 * 发送消息的item
 * Created by 陈健宇 at 2019/6/10
 */
public class SendMessageDelegte extends MutiItemDelegate<Message> {


    @Override
    protected boolean isForViewType(Message items, int position) {
        return items.getId() == Constant.TYPE_ITEM_SEND;
    }

    @Override
    protected BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_send, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    protected void onBindView(BaseViewHolder holder, Message items, int position) {
        holder.setText(R.id.tv_message, items.getText())
                .setText(R.id.tv_name, items.getName());
    }
}
