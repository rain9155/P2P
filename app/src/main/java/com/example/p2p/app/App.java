package com.example.p2p.app;

import android.app.Application;
import android.content.Context;

import com.example.p2p.core.OnlineUserManager;
import com.example.p2p.core.ConnectManager;
import com.example.p2p.db.EmojiDao;

/**
 * Created by 陈健宇 at 2019/5/29
 */
public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        EmojiDao.getInstance();
        OnlineUserManager.getInstance().initListener();
        ConnectManager.getInstance().initListener();
    }

    public static Context getContext(){
        return mContext;
    }
}
