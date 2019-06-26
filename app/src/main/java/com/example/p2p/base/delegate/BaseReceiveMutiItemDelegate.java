package com.example.p2p.base.delegate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.baseadapter.BaseViewHolder;
import com.example.baseadapter.mutiple.MutiItemDelegate;
import com.example.p2p.R;
import com.example.p2p.bean.Mes;
import com.example.p2p.bean.User;
import com.example.p2p.core.OnlineUserManager;

/**
 * Created by 陈健宇 at 2019/6/26
 */
public abstract class BaseReceiveMutiItemDelegate implements MutiItemDelegate<Mes> {

    private Bitmap mUserImage;

    @Override
    public void onBindView(BaseViewHolder holder, Mes items, int position) {
        if(mUserImage == null){
            User user = OnlineUserManager.getInstance().getOnlineUser(items.userIp);
            mUserImage = BitmapFactory.decodeFile(user.getImagePath());
        }
        holder.setImageBitmap(R.id.iv_face, mUserImage);
    }
}
