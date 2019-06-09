package com.example.p2p.adapter;

import com.example.baseadapter.BaseAdapter;
import com.example.baseadapter.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.bean.Emoji;

import java.util.List;

/**
 * 表情列表的adapter
 * Created by 陈健宇 at 2019/5/29
 */
public class RvEmojiAdapter extends BaseAdapter<Emoji> {

    public RvEmojiAdapter(List<Emoji> datas, int layoutId) {
        super(datas, layoutId);
    }

    @Override
    protected void onBindView(BaseViewHolder holder, Emoji item) {
        if(item.getId() == 0){
            holder.setBackgroundResource(R.id.tv_emoji, R.drawable.ic_delete);
        }else {
            holder.setText(R.id.tv_emoji, item.getUnicodeInt());
        }
    }
}
