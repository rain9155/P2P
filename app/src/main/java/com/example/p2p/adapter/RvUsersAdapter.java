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
import com.example.p2p.utils.IpUtils;

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
        String end;
        Bitmap bitmap;
        if(item.getIp().equals(IpUtils.getLocIpAddress())){
            end = " - 自己";
            bitmap = FileUtils.getUserBitmap();
        }else {
            end = "";
            bitmap = BitmapFactory.decodeFile(item.getImagePath());
            if(bitmap == null){
                bitmap = BitmapFactory.decodeResource(holder.getItemView().getContext().getResources(), R.drawable.ic_default_user_2);
            }
        }
        holder.setText(R.id.tv_name, item.getName() + end)
                .setImageBitmap(R.id.iv_face, bitmap);
    }

}
