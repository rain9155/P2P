package com.example.p2p.adapter.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.library.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.base.delegate.BaseSendMutiItemDelegate;
import com.example.p2p.bean.Audio;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;

/**
 * 音频发送的item
 * Created by 陈健宇 at 2019/6/14
 */
public class AudioSendDelegate extends BaseSendMutiItemDelegate {

    @Override
    public boolean isForViewType(Mes items, int position) {
        return items.itemType == ItemType.SEND_AUDIO;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_send_audio, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindView(BaseViewHolder holder, Mes items, int position) {
        super.onBindView(holder, items, position);
        Audio audio = (Audio) items.data;
        holder.setText(R.id.tv_duration, audio.duartion + "'");
    }
}
