package com.example.p2p.adapter.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.library.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.base.delegate.BaseSendMutiItemDelegate;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;

/**
 * 发送消息的item
 * Created by 陈健宇 at 2019/6/10
 */
public class TextSendDelegate extends BaseSendMutiItemDelegate {

    @Override
    public boolean isForViewType(Mes items, int position) {
        return items.itemType == ItemType.SEND_TEXT;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_send_text, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindView(BaseViewHolder holder, Mes items, int position) {
        super.onBindView(holder, items, position);
        holder.setText(R.id.tv_message, (String) items.data);
    }
}
