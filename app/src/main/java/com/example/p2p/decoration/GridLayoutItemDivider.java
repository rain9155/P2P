package com.example.p2p.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * GridLayoutManager的Divider
 * Created by 陈健宇 at 2019/9/30
 */
public class GridLayoutItemDivider extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private Drawable mDivider;

    public GridLayoutItemDivider(Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int itemPosition = parent.getChildViewHolder(view).getAdapterPosition();
        int spanCount = getSpanCount(parent);
        int itemCount = parent.getAdapter().getItemCount();
        if (isLastRaw(parent, itemPosition, spanCount, itemCount))// 如果是最后一行，则不需要绘制底部
        {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        } else if (isLastCol(parent, itemPosition, spanCount, itemCount))// 如果是最后一列，则不需要绘制右边
        {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else
        {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
        }
    }


    /**
     * 获得列数
     */
    private int getSpanCount(RecyclerView parent) {
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return spanCount;
    }

    /**
     * 是否是最后一列
     * @param parent recyclerView
     * @param pos item的位置
     * @param spanCount 列数
     * @param itemCount item的数量
     */
    private boolean isLastCol(RecyclerView parent, int pos, int spanCount, int itemCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            if ((pos + 1) % spanCount == 0){// 如果是最后一列，则不需要绘制右边
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0) {// 如果是最后一列，则不需要绘制右边
                    return true;
                }
            } else {
                itemCount = itemCount - itemCount % spanCount;
                if (pos >= itemCount)// 如果是最后一列，则不需要绘制右边
                    return true;
            }
        }
        return false;
    }

    /**
     * 是否是最后一行
     */
    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount, int itemCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            itemCount = itemCount - itemCount % spanCount;
            if (pos >= itemCount)// 如果是最后一行，则不需要绘制底部
                return true;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {// StaggeredGridLayoutManager 且纵向滚动
                itemCount = itemCount - itemCount % spanCount;
                // 如果是最后一行，则不需要绘制底部
                if (pos >= itemCount)
                    return true;
            } else {// StaggeredGridLayoutManager 且横向滚动
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            }
        }
        return false;
    }

}
