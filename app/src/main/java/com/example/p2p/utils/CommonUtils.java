package com.example.p2p.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.renderscript.ScriptGroup;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import com.example.p2p.R;

/**
 * Created by 陈健宇 at 2019/5/28
 */
public class CommonUtils {

    /**
     * 设置状态栏字体颜色
     * @param activity 上下文
     * @param dark 是否深色
     */
    public static void darkMode(Activity activity, boolean dark) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int systemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            if (dark) {
                systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(Color.GRAY);
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
