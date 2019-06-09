package com.example.p2p.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.example.baseadapter.BaseAdapter;
import com.example.baseadapter.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.bean.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面用户列表的adapter
 * Created by 陈健宇 at 2019/6/7
 */
public class RvMainAdapter extends BaseAdapter<User> {

    public RvMainAdapter(List<User> datas, int layoutId) {
        super(datas, layoutId);
    }

    @Override
    protected void onBindView(BaseViewHolder holder, User item) {
        holder.setText(R.id.tv_name, item.getName() + " - " + item.getIp());
    }

    public List<User> wrap(List<String> list){
        List<User> userList = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            String name = "用户" + (i + 1);
            User user = new User(name, list.get(i));
            userList.add(user);
        }
        return userList;
    }

}
