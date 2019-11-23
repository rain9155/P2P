package com.example.p2p.base.delegate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.library.BaseViewHolder;
import com.example.library.multiple.IMultiItemDelegate;
import com.example.p2p.R;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.User;
import com.example.p2p.core.OnlineUserManager;

/**
 * Created by 陈健宇 at 2019/6/26
 */
public abstract class BaseReceiveMutiItemDelegate implements IMultiItemDelegate<Mes> {

    private Bitmap mUserImage;

    @Override
    public void onBindView(BaseViewHolder holder, Mes items, int position) {
        if(mUserImage == null){
            User user = OnlineUserManager.get().getOnlineUser(items.userIp);
            mUserImage = BitmapFactory.decodeFile(user.getImagePath());
            if(mUserImage == null)
                mUserImage = BitmapFactory.decodeResource(holder.getItemView().getContext().getResources(), R.drawable.ic_default_user_2);
        }
        holder.setImageBitmap(R.id.iv_face, mUserImage);
    }
}
