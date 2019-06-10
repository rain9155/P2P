package com.example.p2p.adapter;

import com.example.baseadapter.BaseAdapter;
import com.example.p2p.adapter.delegte.ReceiveMessageDelegte;
import com.example.p2p.adapter.delegte.SendMessageDelegte;
import com.example.p2p.bean.Message;
import com.example.p2p.config.Constant;

import java.util.List;

/**
 * 聊天界面列表的adapter
 * Created by 陈健宇 at 2019/6/10
 */
public class RvChatAdapter extends BaseAdapter<Message> {

    public RvChatAdapter(List<Message> datas) {
        super(datas);
        adapterDelegateManager.addDelegte(Constant.TYPE_ITEM_SEND, new SendMessageDelegte())
                .addDelegte(Constant.TYPE_ITEM_RECEIVE, new ReceiveMessageDelegte());
    }
}
