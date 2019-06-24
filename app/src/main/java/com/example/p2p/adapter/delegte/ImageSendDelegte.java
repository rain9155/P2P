package com.example.p2p.adapter.delegte;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.baseadapter.BaseViewHolder;
import com.example.baseadapter.mutiple.MutiItemDelegate;
import com.example.p2p.R;
import com.example.p2p.bean.Image;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;
import com.example.p2p.config.Constant;
import com.example.p2p.utils.FileUtils;
import com.example.p2p.utils.LogUtils;

/**
 * 发送图片的item
 * Created by 陈健宇 at 2019/6/24
 */
public class ImageSendDelegte implements MutiItemDelegate<Mes> {

    @Override
    public boolean isForViewType(Mes items, int position) {
        return items.id == ItemType.SEND_IMAGE;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_send_image, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindView(BaseViewHolder holder, Mes items, int position) {
        Image image = (Image) items.data;
        Bitmap bitmap = BitmapFactory.decodeFile(image.imagePath);
        holder.setImageBitmap(R.id.iv_message, bitmap)
                .setImageBitmap(R.id.iv_face, FileUtils.getUserBitmap());
    }
}
