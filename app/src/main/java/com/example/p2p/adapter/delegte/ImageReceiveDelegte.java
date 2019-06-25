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
import com.example.p2p.bean.User;
import com.example.p2p.core.OnlineUserManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 接受图片的item
 * Created by 陈健宇 at 2019/6/24
 */
public class ImageReceiveDelegte implements MutiItemDelegate<Mes> {

    private Bitmap mUserImage;
    private Map<String, Bitmap> mMessageImages;

    public ImageReceiveDelegte() {
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
        Image image = (Image) items.data;
        if(mUserImage == null){
            User user = OnlineUserManager.getInstance().getOnlineUser(items.userIp);
            mUserImage = BitmapFactory.decodeFile(user.getImagePath());
        }
        if(!mMessageImages.containsKey(image.imagePath)){
            mMessageImages.put(image.imagePath,  BitmapFactory.decodeFile(image.imagePath));
        }
        holder.setImageBitmap(R.id.iv_face, mUserImage)
                .setImageBitmap(R.id.iv_message, mMessageImages.get(image.imagePath));
    }
}
