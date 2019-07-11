package com.example.p2p.base.delegate;

import android.graphics.Bitmap;

import com.example.baseadapter.BaseViewHolder;
import com.example.baseadapter.mutiple.MutiItemDelegate;
import com.example.p2p.R;
import com.example.p2p.bean.Mes;
import com.example.p2p.utils.FileUtil;

/**
 * Created by 陈健宇 at 2019/6/26
 */
public abstract  class BaseSendMutiItemDelegate implements MutiItemDelegate<Mes>{

    private Bitmap mUserBitmap;

    public BaseSendMutiItemDelegate() {
        mUserBitmap = FileUtil.getUserBitmap();
    }

    @Override
    public void onBindView(BaseViewHolder holder, Mes items, int position) {
        holder.setImageBitmap(R.id.iv_face, mUserBitmap);
    }
}
