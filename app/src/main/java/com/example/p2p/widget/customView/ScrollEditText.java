package com.example.p2p.widget.customView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.example.p2p.R;

/**
 * 解决与Scroll嵌套滑动的EditText
 * Created by 陈健宇 at 2018/11/8
 */
@SuppressLint("AppCompatCustomView")
public class ScrollEditText extends EditText implements
        TextWatcher, View.OnFocusChangeListener, View.OnTouchListener {

    private Drawable mClearDrawable;
    private boolean hasFocus;



    public ScrollEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        this.setOnTouchListener(this);
        this.setOnFocusChangeListener(this);
        this.addTextChangedListener(this);
        //获得右边的Drawable
        mClearDrawable = getCompoundDrawables()[2];
        //没有就用默认的
        if(mClearDrawable == null) mClearDrawable = getResources().getDrawable(R.drawable.ic_cancel);
        //设置大小
        mClearDrawable.setBounds(0, 0, 80, 80);
        //默认设置隐藏图标
        setClearIconVisible(false);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     * 当输入框里面内容发生变化的时候回调的方法
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(hasFocus) setClearIconVisible(true);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.toString().trim().isEmpty()) setClearIconVisible(false);
    }

    /**
     * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFocus = hasFocus;
        if(hasFocus){
            setClearIconVisible(getText().toString().trim().length() > 0);
        }else {
            setClearIconVisible(false);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //但手指按下的x坐标在clear图标的范围就清空文字
        if(event.getAction() == MotionEvent.ACTION_UP){
            if(getCompoundDrawables()[2] != null){
                boolean isTouch =
                        event.getX() > getWidth() - getPaddingRight() - mClearDrawable.getIntrinsicWidth() &&
                        event.getX() < getWidth() - getPaddingRight() &&
                        event.getY() > getHeight() - getHeight() / 2  - mClearDrawable.getIntrinsicHeight() / 2 &&
                        event.getY() < getHeight() - getHeight() / 2 + mClearDrawable.getIntrinsicHeight() / 2;
                if(isTouch){
                    this.setText("");
                    setClearIconVisible(false);
                }
            }
        }
        //
        if(canVerticalScroll(this)){
            this.getParent().requestDisallowInterceptTouchEvent(true);
            if(event.getAction() == MotionEvent.ACTION_UP)
                this.getParent().requestDisallowInterceptTouchEvent(false);
        }
        return false;
    }


    /**
     * 控制Drawable的显隐性
     */
    private void setClearIconVisible(boolean visible) {
        Drawable clearDrawable = visible ? mClearDrawable : null;
        setCompoundDrawables(
                getCompoundDrawables()[0],
                getCompoundDrawables()[1],
                clearDrawable,
                getCompoundDrawables()[3]);
    }

    /**
     * EditText竖直方向是否可以滚动
     * @return true：可以滚动  false：不可以滚动
     */
    private boolean canVerticalScroll(EditText editText){
        int scrollY = editText.getScrollY();//滑动的距离
        int scrollRange = editText.getLayout().getHeight();//控件内容的总高度
        int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() -editText.getCompoundPaddingBottom();//控件实际显示的高度
        int scrollDifference = scrollRange - scrollExtent; //控件内容总高度与实际显示高度的差值
        if(scrollDifference == 0)
            return false;
        return scrollY > 0 || (scrollY < scrollDifference - 1);
    }


}
