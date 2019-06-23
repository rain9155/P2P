package com.example.p2p.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;

import com.example.baseadapter.BaseAdapter;
import com.example.baseadapter.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.bean.User;
import com.example.p2p.core.OnlineUserManager;
import com.example.p2p.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 主界面用户列表的adapter
 * Created by 陈健宇 at 2019/6/7
 */
public class RvUsersAdapter extends BaseAdapter<User> {

    public RvUsersAdapter(List<User> datas, int layoutId) {
        super(datas, layoutId);
    }

    @Override
    protected void onBindView(BaseViewHolder holder, User item) {
        Bitmap bitmap = BitmapFactory.decodeFile(item.getImagePath());
        holder.setText(R.id.tv_name, item.getName() + " - " + item.getIp())
                .setImageBitmap(R.id.iv_face, bitmap);
    }

}
