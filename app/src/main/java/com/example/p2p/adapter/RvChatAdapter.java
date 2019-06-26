package com.example.p2p.adapter;

import com.example.baseadapter.BaseAdapter;
import com.example.p2p.adapter.delegate.AudioReceiveDelegate;
import com.example.p2p.adapter.delegate.AudioSendDelegate;
import com.example.p2p.adapter.delegate.FileReceiveDelegate;
import com.example.p2p.adapter.delegate.FileSendDelegate;
import com.example.p2p.adapter.delegate.ImageReceiveDelegate;
import com.example.p2p.adapter.delegate.ImageSendDelegate;
import com.example.p2p.adapter.delegate.TextReceiveDelegate;
import com.example.p2p.adapter.delegate.TextSendDelegate;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;

import java.util.List;

/**
 * 聊天界面列表的adapter
 * Created by 陈健宇 at 2019/6/10
 */
public class RvChatAdapter extends BaseAdapter<Mes> {

    public RvChatAdapter(List<Mes> datas) {
        super(datas);
        adapterDelegateManager.addDelegte(ItemType.SEND_TEXT.ordinal(), new TextSendDelegate())
                .addDelegte(ItemType.RECEIVE_TEXT.ordinal(), new TextReceiveDelegate())
                .addDelegte(ItemType.SEND_AUDIO.ordinal(), new AudioSendDelegate())
                .addDelegte(ItemType.RECEIVE_AUDIO.ordinal(), new AudioReceiveDelegate())
                .addDelegte(ItemType.SEND_IMAGE.ordinal(), new ImageSendDelegate())
                .addDelegte(ItemType.RECEIVE_IMAGE.ordinal(), new ImageReceiveDelegate())
                .addDelegte(ItemType.SEND_FILE.ordinal(), new FileSendDelegate())
                .addDelegte(ItemType.RECEIVE_FILE.ordinal(), new FileReceiveDelegate());
    }
}
