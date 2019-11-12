package com.example.p2p.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * 修改StatusBar颜色的工具类
 * 参考：https://jimmysun.blog.csdn.net/article/details/100065336
 * Created by 陈健宇 at 2019/11/10
 */
public class StatusBarUtil {

    private static final int MIN_API = 19;

    public static void immersive(AppCompatActivity activity,  @ColorInt int color) {
        immersive(activity.getWindow(), color);
    }

    /**
     * 设置状态栏颜色
     */
    public static void immersive(Window window, @ColorInt int color) {
        if(Build.VERSION.SDK_INT < MIN_API) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatusColor(window, color);
        }else{
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            View decorView = window.getDecorView();
            ViewGroup contentView = decorView.findViewById(android.R.id.content);
            setPadding(window.getContext(), contentView);
            setTranslucentView((ViewGroup) decorView, color);
        }
    }

    public static void transparentAndDark(AppCompatActivity activity){
        Window window = activity.getWindow();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            transparent(window);
            darkModeForM(window, true);
        }else if(isFlyme4Later()){
            transparent(window);
            darkModeForFlyme4(window, true);
        }else if(isMIUI6Later()){
            transparent(window);
            darkModeForMIUI6(window, true);
        }else {
            transparent(window);
        }
    }

    /**
     * 设置状态栏透明并且设置字体变暗
     */
    public static void transparentAndDark(Window window){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            transparent(window);
            darkModeForM(window, true);
        }else if(isFlyme4Later()){
            transparent(window);
            darkModeForFlyme4(window, true);
        }else if(isMIUI6Later()){
            transparent(window);
            darkModeForMIUI6(window, true);
        }else {
            transparent(window);
        }
    }

    /**
     * 设置状态栏透明
     */
    public static void transparent(Window window){
        if(Build.VERSION.SDK_INT < MIN_API) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTransparent(window);
        } else{
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            View decorView = window.getDecorView();
            ViewGroup contentView = decorView.findViewById(android.R.id.content);
            contentView.setPadding(
                    contentView.getPaddingLeft(),
                    0,
                    contentView.getPaddingRight(),
                    contentView.getBottom()
            );
            View translucentView = window.getDecorView().findViewById(android.R.id.custom);
            if(translucentView != null){
                translucentView.setVisibility(View.GONE);
            }
        }
    }

    public static void immersiveAndDark(AppCompatActivity activity, @ColorInt int color){
        Window window = activity.getWindow();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            immersive(window, color);
            darkModeForM(window, true);
        }else if(isFlyme4Later()){
            immersive(window, color);
            darkModeForFlyme4(window, true);
        }else if(isMIUI6Later()){
            immersive(window, color);
            darkModeForMIUI6(window, true);
        }else {
            immersive(window, ColorUtils.blendARGB(Color.TRANSPARENT, color, 0.5f));
        }
    }

    /**
     * 设置状态栏颜色并且设置字体颜色变暗
     */
    public static void immersiveAndDark(Window window, @ColorInt int color){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            immersive(window, color);
            darkModeForM(window, true);
        }else if(isFlyme4Later()){
            immersive(window, color);
            darkModeForFlyme4(window, true);
        }else if(isMIUI6Later()){
            immersive(window, color);
            darkModeForMIUI6(window, true);
        }else {//6.0以下不能修改字体颜色，就把状态栏颜色降低一点，突出字体颜色
            immersive(window, ColorUtils.blendARGB(Color.TRANSPARENT, color, 0.5f));
        }
    }

    public static void darkMode(AppCompatActivity activity, boolean dark) {
        darkMode(activity.getWindow(), dark);
    }

    /**
     * 设置状态栏的字体颜色及icon变黑(目前支持MIUI6以上,Flyme4以上,Android M以上)
     */
    public static void darkMode(Window window, boolean isDark) {
        if (isFlyme4Later()) {
            darkModeForFlyme4(window, isDark);
        } else if (isMIUI6Later()) {
            darkModeForMIUI6(window, isDark);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            darkModeForM(window, isDark);
        }
    }


    /**
     * android 6.0设置字体颜色
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public static void darkModeForM(Window window, boolean dark) {
        int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
        if (dark) {
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        window.getDecorView().setSystemUiVisibility(systemUiVisibility);
    }

    /**
     * 设置Flyme4+的darkMode,darkMode时候字体颜色及icon变黑
     * http://open-wiki.flyme.cn/index.php?title=Flyme%E7%B3%BB%E7%BB%9FAPI
     */
    public static boolean darkModeForFlyme4(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams e = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(e);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }

                meizuFlags.setInt(e, value);
                window.setAttributes(e);
                result = true;
            } catch (Exception var8) {
                Log.e("StatusBar", "darkIcon: failed");
            }
        }

        return result;
    }

    /**
     * 设置MIUI6+的状态栏是否为darkMode,darkMode时候字体颜色及icon变黑
     * http://dev.xiaomi.com/doc/p=4769/
     */
    public static boolean darkModeForMIUI6(Window window, boolean darkmode) {
        Class<? extends Window> clazz = window.getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(window, darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 设置系统状态栏颜色(5.0以上)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusColor(Window window,  @ColorInt int color) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(color);
    }


    /**
     * 设置透明状态栏(5.0以上)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setTransparent(Window window){
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * 增加View的paddingTop,增加的值为状态栏高度
     */
    public static void setPadding(Context context, View view) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop() + getStatusBarHeight(context),
                    view.getPaddingRight(),
                    view.getPaddingBottom());
        }
    }
    /**
     *  增加View的高度和paddingTop,增加的值为状态栏高度，一般在沉浸式状态栏时给Toolbar使用
     */
    public static void setPaddingAndHeight(Context context, View view) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp != null && lp.height > 0) {
                lp.height += getStatusBarHeight(context);
            }
            setPadding(context, view);
        }
    }

    /**
     * 增加View的marginTop，一般是给高度为 WARP_CONTENT 的小控件使用
     */
    public static void setMargin(Context context, View view) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) lp).topMargin += getStatusBarHeight(context);
            }
            view.setLayoutParams(lp);
        }
    }

    /**
     * 创建假的透明栏(解决5.0以下不能设置直接设置状态栏颜色的问题)
     */
    private static void setTranslucentView(ViewGroup container, int color) {
        View translucentView = container.findViewById(android.R.id.custom);
        if (translucentView == null) {
            translucentView = new View(container.getContext());
            translucentView.setId(android.R.id.custom);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(container.getContext()));
            container.addView(translucentView, lp);
        }
        translucentView.setBackgroundColor(color);
        if(translucentView.getVisibility() == View.GONE){
            translucentView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取状态栏高度
     */
    private static int getStatusBarHeight(Context context) {
        int result = 24;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = context.getResources().getDimensionPixelSize(resId);
        } else {
            result = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    result, Resources.getSystem().getDisplayMetrics());
        }
        return result;
    }

    /**
     * 判断是否Flyme4以上
     */
    private static boolean isFlyme4Later() {
        return Build.FINGERPRINT.contains("Flyme_OS_4")
                || Build.VERSION.INCREMENTAL.contains("Flyme_OS_4")
                || Pattern.compile("Flyme OS [4|5]", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find();
    }

    /**
     * 判断是否为MIUI6以上
     */
    private static boolean isMIUI6Later() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method mtd = clz.getMethod("get", String.class);
            String val = (String) mtd.invoke(null, "ro.miui.ui.version.name");
            val = val.replaceAll("[vV]", "");
            int version = Integer.parseInt(val);
            return version >= 6;
        } catch (Exception e) {
            return false;
        }
    }

}
