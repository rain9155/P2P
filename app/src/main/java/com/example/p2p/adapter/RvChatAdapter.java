package com.example.p2p.adapter;

import com.example.library.BaseAdapter;
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
        addItemAdapterDelegte(ItemType.SEND_TEXT.ordinal(), new TextSendDelegate())
                .addItemAdapterDelegte(ItemType.RECEIVE_TEXT.ordinal(), new TextReceiveDelegate())
                .addItemAdapterDelegte(ItemType.SEND_AUDIO.ordinal(), new AudioSendDelegate())
                .addItemAdapterDelegte(ItemType.RECEIVE_AUDIO.ordinal(), new AudioReceiveDelegate())
                .addItemAdapterDelegte(ItemType.SEND_IMAGE.ordinal(), new ImageSendDelegate())
                .addItemAdapterDelegte(ItemType.RECEIVE_IMAGE.ordinal(), new ImageReceiveDelegate())
                .addItemAdapterDelegte(ItemType.SEND_FILE.ordinal(), new FileSendDelegate())
                .addItemAdapterDelegte(ItemType.RECEIVE_FILE.ordinal(), new FileReceiveDelegate());
    }
}
