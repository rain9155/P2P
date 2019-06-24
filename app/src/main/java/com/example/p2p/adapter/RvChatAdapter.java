package com.example.p2p.adapter;

import com.example.baseadapter.BaseAdapter;
import com.example.p2p.adapter.delegte.AudioReceiveDelegte;
import com.example.p2p.adapter.delegte.AudioSendDelegte;
import com.example.p2p.adapter.delegte.ImageReceiveDelegte;
import com.example.p2p.adapter.delegte.ImageSendDelegte;
import com.example.p2p.adapter.delegte.TextReceiveDelegte;
import com.example.p2p.adapter.delegte.TextSendDelegte;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;
import com.example.p2p.config.Constant;

import java.util.List;

/**
 * 聊天界面列表的adapter
 * Created by 陈健宇 at 2019/6/10
 */
public class RvChatAdapter extends BaseAdapter<Mes> {

    public RvChatAdapter(List<Mes> datas) {
        super(datas);
        adapterDelegateManager.addDelegte(ItemType.SEND_TEXT.ordinal(), new TextSendDelegte())
                .addDelegte(ItemType.RECEIVE_TEXT.ordinal(), new TextReceiveDelegte())
                .addDelegte(ItemType.SEND_AUDIO.ordinal(), new AudioSendDelegte())
                .addDelegte(ItemType.RECEIVE_AUDIO.ordinal(), new AudioReceiveDelegte())
                .addDelegte(ItemType.SEND_IMAGE.ordinal(), new ImageSendDelegte())
                .addDelegte(ItemType.RECEIVE_IMAGE.ordinal(), new ImageReceiveDelegte());
    }
}
