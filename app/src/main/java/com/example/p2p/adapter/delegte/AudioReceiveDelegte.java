package com.example.p2p.adapter.delegte;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.baseadapter.BaseViewHolder;
import com.example.baseadapter.mutiple.MutiItemDelegate;
import com.example.p2p.R;
import com.example.p2p.bean.Audio;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.User;
import com.example.p2p.config.Constant;
import com.example.p2p.core.OnlineUserManager;
import com.example.p2p.utils.FileUtils;

/**
 * 接收音频的item
 * Created by 陈健宇 at 2019/6/14
 */
public class AudioReceiveDelegte extends MutiItemDelegate<Mes> {



    @Override
    protected boolean isForViewType(Mes items, int position) {
        return items.id == Constant.TYPE_ITEM_RECEIVE_AUDIO;
    }

    @Override
    protected BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_receive_audio, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    protected void onBindView(BaseViewHolder holder, Mes items, int position) {
        Audio audio = (Audio) items.data;
        User user = OnlineUserManager.getInstance().getOnlineUser(items.userIp);
        Bitmap bitmap = BitmapFactory.decodeFile(user.getImagePath());
        holder.setText(R.id.tv_duration, audio.duartion + "'")
                .setImageBitmap(R.id.iv_face, bitmap);

    }
}
