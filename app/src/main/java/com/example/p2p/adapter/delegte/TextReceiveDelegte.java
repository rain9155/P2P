package com.example.p2p.adapter.delegte;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.baseadapter.BaseViewHolder;
import com.example.baseadapter.mutiple.MutiItemDelegate;
import com.example.p2p.R;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.User;
import com.example.p2p.core.OnlineUserManager;

/**
 * 接受消息的item
 * Created by 陈健宇 at 2019/6/10
 */
public class TextReceiveDelegte implements MutiItemDelegate<Mes>{

    private Bitmap mUserImage;

    @Override
    public boolean isForViewType(Mes items, int position) {
        return items.itemType == ItemType.RECEIVE_TEXT;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_receive_text, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindView(BaseViewHolder holder, Mes items, int position) {
        if(mUserImage == null){
            User user = OnlineUserManager.getInstance().getOnlineUser(items.userIp);
            mUserImage = BitmapFactory.decodeFile(user.getImagePath());
        }
        holder.setText(R.id.tv_message, (String) items.data)
                .setImageBitmap(R.id.iv_face, mUserImage);
    }
}
