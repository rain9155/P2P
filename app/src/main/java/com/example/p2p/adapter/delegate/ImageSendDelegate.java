package com.example.p2p.adapter.delegate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.baseadapter.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.base.delegate.BaseSendMutiItemDelegate;
import com.example.p2p.bean.Image;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;
import com.example.p2p.utils.ImageUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 发送图片的item
 * Created by 陈健宇 at 2019/6/24
 */
public class ImageSendDelegate extends BaseSendMutiItemDelegate {

    private Map<String, Bitmap> mMessageImages;//缓存一下，不然滑动卡顿

    public ImageSendDelegate() {
        mMessageImages = new HashMap<>();
    }

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
        if(!mMessageImages.containsKey(image.imagePath)){
            mMessageImages.put(image.imagePath, BitmapFactory.decodeFile(image.imagePath));
        }
        holder.setImageBitmap(R.id.iv_message, mMessageImages.get(image.imagePath));
        ImageView imageView = holder.getView(R.id.iv_message);
        if(image.progress < 100){
            imageView.getDrawable().mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            holder.setText(R.id.tv_progress, image.progress + "");
        }else {
            holder.setText(R.id.tv_progress, image.progress + "");
            imageView.clearColorFilter();
            holder.setVisibility(R.id.ll_sending, View.GONE);
        }
    }
}
