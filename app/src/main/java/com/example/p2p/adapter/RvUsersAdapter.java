package com.example.p2p.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.library.BaseAdapter;
import com.example.library.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.bean.User;
import com.example.p2p.utils.FileUtil;
import com.example.p2p.utils.IpUtil;

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
        if(item.getIp().equals(IpUtil.getLocIpAddress())){
            end = " - 自己";
            bitmap = FileUtil.getUserBitmap();
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
