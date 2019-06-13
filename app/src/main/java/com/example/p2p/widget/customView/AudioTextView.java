package com.example.p2p.widget.customView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.example.p2p.R;
import com.example.p2p.utils.LogUtils;
import com.example.p2p.utils.VibrateUtils;
import com.example.utils.DisplayUtil;

/**
 * 录制音频的按钮
 * Created by 陈健宇 at 2019/6/13
 */
public class AudioTextView extends AppCompatTextView {

    private static final String TAG = AudioTextView.class.getSimpleName();
    private Dialog mDialog;
    private ImageView mAudioImage;
    private TextView mAudioTextView;
    private int mBoundary;
    private Drawable mPressBg;
    private Drawable mNormalBg;
    private int[] mAudiosResId;

    public AudioTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        this.setOnLongClickListener(v -> {
            VibrateUtils.Vibrate(getContext(), 100);
            mDialog.show();
            mAudioTextView.setText(getContext().getString(R.string.dialog_audio_undo));
            mAudioImage.setImageResource(R.drawable.ic_volume_1);
            LogUtils.d(TAG, "长按");
            return false;
        });
    }

    private void init() {
         View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_audio, null);
         mDialog = new AlertDialog.Builder(getContext(), R.style.dialog_audio_style)
                .setView(view)
                .setCancelable(false)
                .create();
        WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
        lp.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        lp.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        mDialog.getWindow().setAttributes(lp);
        mAudioImage = view.findViewById(R.id.iv_audio);
        mAudioTextView = view.findViewById(R.id.tv_toast);
        int screenHeight = DisplayUtil.getScreenHeight(getContext());
        mBoundary =  screenHeight - screenHeight / 6;
        mPressBg = ContextCompat.getDrawable(getContext(), R.drawable.bg_chat_audio_selected);
        mNormalBg = ContextCompat.getDrawable(getContext(), R.drawable.bg_chat_audio);
        mAudiosResId = new int[]{
                R.drawable.ic_volume_1, R.drawable.ic_volume_2, R.drawable.ic_volume_3, R.drawable.ic_volume_4,
                R.drawable.ic_volume_5, R.drawable.ic_volume_6, R.drawable.ic_volume_7, R.drawable.ic_volume_8,
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtils.d(TAG, "onTouchEvent: event = " + event.getAction());
        LogUtils.d(TAG, "onTouchEvent: y = " + event.getRawY());
        float curY = event.getRawY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                this.setText(getContext().getString(R.string.chat_tvAudio_undo));
                this.setBackground(mPressBg);
                break;
            case MotionEvent.ACTION_MOVE:
                if(curY < mBoundary){
                    this.setText(getContext().getString(R.string.chat_tvAudio_cancel));
                    mAudioTextView.setText(getContext().getString(R.string.dialog_audio_cancel));
                    mAudioTextView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAudioCancel));
                    mAudioImage.setImageResource(R.drawable.ic_volume_cancel);
                }else {
                    this.setText(getContext().getString(R.string.chat_tvAudio_undo));
                    mAudioTextView.setBackgroundColor(Color.TRANSPARENT);
                    mAudioTextView.setText(getContext().getString(R.string.dialog_audio_undo));
                    mAudioImage.setImageResource(R.drawable.ic_volume_1);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.setPressed(false);
                mDialog.dismiss();
                this.setText(getContext().getString(R.string.chat_tvAudio_press));
                this.setBackground(mNormalBg);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
}
