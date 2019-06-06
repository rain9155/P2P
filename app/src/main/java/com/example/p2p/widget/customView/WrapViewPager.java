package com.example.p2p.widget.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * 可以wrap_content的ViewPager
 * Created by 陈健宇 at 2019/5/29
 */
public class WrapViewPager extends ViewPager {

    public WrapViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST){
            int maxHeight = 0;
            for(int i = 0; i < getChildCount(); i++){
                View child = getChildAt(i);
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                if(h > maxHeight) maxHeight = h;
            }
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY));
        }else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
