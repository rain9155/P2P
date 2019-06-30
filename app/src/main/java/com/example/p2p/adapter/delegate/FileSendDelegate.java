package com.example.p2p.adapter.delegate;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.baseadapter.BaseViewHolder;
import com.example.p2p.R;
import com.example.p2p.base.delegate.BaseSendMutiItemDelegate;
import com.example.p2p.bean.Document;
import com.example.p2p.bean.ItemType;
import com.example.p2p.bean.Mes;
import com.example.p2p.utils.ImageUtils;

/**
 * 文件发送item
 * Created by 陈健宇 at 2019/6/26
 */
public class FileSendDelegate extends BaseSendMutiItemDelegate {

    @Override
    public boolean isForViewType(Mes items, int position) {
        return items.itemType == ItemType.SEND_FILE;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_send_file, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindView(BaseViewHolder holder, Mes items, int position) {
        super.onBindView(holder, items, position);
        Document file = (Document) items.data;
        holder.setText(R.id.tv_message, file.fileName)
                .setText(R.id.tv_size, file.fileSize)
                .setImageResource(R.id.iv_file_icon, ImageUtils.getImageId(file.fileType));
        ConstraintLayout layout = holder.getView(R.id.cl_message);
        if(file.progress < 100){
            layout.getBackground().mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            holder.setText(R.id.tv_progress, file.progress + "");
        }else {
            holder.setText(R.id.tv_progress, file.progress + "");
            layout.getBackground().clearColorFilter();
            holder.setVisibility(R.id.group, View.GONE);
        }
    }
}
