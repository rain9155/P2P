package com.example.p2p.adapter;

import com.example.baseadapter.BaseAdapter;
import com.example.p2p.adapter.delegte.AudioReceiveDelegte;
import com.example.p2p.adapter.delegte.AudioSendDelegte;
import com.example.p2p.adapter.delegte.TextReceiveDelegte;
import com.example.p2p.adapter.delegte.TextSendDelegte;
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
        adapterDelegateManager.addDelegte(Constant.TYPE_ITEM_SEND_TEXT, new TextSendDelegte())
                .addDelegte(Constant.TYPE_ITEM_RECEIVE_TEXT, new TextReceiveDelegte())
                .addDelegte(Constant.TYPE_ITEM_SEND_AUDIO, new AudioSendDelegte())
                .addDelegte(Constant.TYPE_ITEM_RECEIVE_AUDIO, new AudioReceiveDelegte());
    }
}
