package com.example.p2p.adapter.delegate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.library.BaseViewHolder;
import com.example.myglide.MyGlide;
import com.example.p2p.R;
import com.example.p2p.base.delegate.BaseSendMutiItemDelegate;
import com.example.p2p.bean.Image;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;

import java.util.HashMap;
import java.util.Map;

/**
 * 发送图片的item
 * Created by 陈健宇 at 2019/6/24
 */
public class ImageSendDelegate extends BaseSendMutiItemDelegate {


    @Override
    public boolean isForViewType(Mes items, int position) {
        return items.itemType == ItemType.SEND_IMAGE;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_send_image, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindView(BaseViewHolder holder, Mes items, int position) {
        super.onBindView(holder, items, position);
        Image image = (Image) items.data;
        MyGlide.with(holder.itemView.getContext())
                .load(image.imagePath)
                .into(holder.getView(R.id.iv_message));
        ImageView imageView = holder.getView(R.id.iv_message);
        if(image.progress < 100){
            imageView.getDrawable().mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            holder.setVisibility(R.id.tv_progress, View.VISIBLE)
                    .setText(R.id.tv_progress, image.progress + "");
        }else {
            imageView.clearColorFilter();
            holder.setText(R.id.tv_progress, image.progress + "")
                    .setVisibility(R.id.ll_sending, View.GONE);
        }
    }
}
