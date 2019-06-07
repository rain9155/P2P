package com.example.p2p.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.example.baseadapter.BaseAdapter;
import com.example.baseadapter.BaseViewHolder;
import com.example.p2p.R;

import java.util.List;

/**
 * 主界面用户列表的adapter
 * Created by 陈健宇 at 2019/6/7
 */
public class RvMainAdapter extends BaseAdapter<String> {

    public RvMainAdapter(List<String> datas, int layoutId) {
        super(datas, layoutId);
    }

    @Override
    protected void onBindView(BaseViewHolder holder, String item) {
        holder.setText(R.id.tv_name, item);
    }
}
