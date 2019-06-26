package com.example.p2p.adapter.delegate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.baseadapter.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.base.delegate.BaseReceiveMutiItemDelegate;
import com.example.p2p.bean.Image;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;

import java.util.HashMap;
import java.util.Map;

/**
 * 接受图片的item
 * Created by 陈健宇 at 2019/6/24
 */
public class ImageReceiveDelegate extends BaseReceiveMutiItemDelegate {

    private Map<String, Bitmap> mMessageImages;

    public ImageReceiveDelegate() {
        mMessageImages = new HashMap<>();
    }

    @Override
    public boolean isForViewType(Mes items, int position) {
        return items.itemType == ItemType.RECEIVE_IMAGE;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_receive_image, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindView(BaseViewHolder holder, Mes items, int position) {
        super.onBindView(holder, items, position);
        Image image = (Image) items.data;
        if(!mMessageImages.containsKey(image.imagePath)){
            mMessageImages.put(image.imagePath,  BitmapFactory.decodeFile(image.imagePath));
        }
        holder.setImageBitmap(R.id.iv_message, mMessageImages.get(image.imagePath));
    }
}
