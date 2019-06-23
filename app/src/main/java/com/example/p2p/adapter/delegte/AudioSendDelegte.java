package com.example.p2p.adapter.delegte;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.baseadapter.BaseViewHolder;
import com.example.baseadapter.mutiple.MutiItemDelegate;
import com.example.p2p.R;
import com.example.p2p.app.App;
import com.example.p2p.bean.Audio;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.User;
import com.example.p2p.config.Constant;
import com.example.p2p.core.MediaPlayerManager;
import com.example.p2p.utils.FileUtils;
import com.example.utils.FileUtil;

/**
 * 音频发送的item
 * Created by 陈健宇 at 2019/6/14
 */
public class AudioSendDelegte extends MutiItemDelegate<Mes> {

    private User mUser;

    @Override
    protected boolean isForViewType(Mes items, int position) {
        return items.id == Constant.TYPE_ITEM_SEND_AUDIO;
    }

    @Override
    protected BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_send_audio, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    protected void onBindView(BaseViewHolder holder, Mes items, int position) {
        Audio audio = (Audio) items.data;
        holder.setText(R.id.tv_duration, audio.duartion + "'")
                .setImageBitmap(R.id.iv_face, FileUtils.getUserBitmap());
    }
}
