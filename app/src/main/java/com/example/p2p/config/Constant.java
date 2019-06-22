package com.example.p2p.config;

import com.example.p2p.app.App;
import com.example.p2p.utils.FileUtils;

/**
 * 常量
 * Created by 陈健宇 at 2019/6/9
 */
public class Constant {

    public static final String EXTRA_TARGET_USER = "targetUser";

    public static final String FILE_NAME_USER = "user";
    public static final String FILE_NAME_USER_IMAGE = "userImage.png";
    public static final String FILE_PATH_USER_IMAGE = FileUtils.getFilePath(App.getContext(), "user/");
    public static final String FILE_USER_IMAGE = FILE_PATH_USER_IMAGE + FILE_NAME_USER_IMAGE;
    public static final String FILE_PATH_ONLINE_USER = FileUtils.getFilePath(App.getContext(), "onlineUser/");
    public static final String FILE_PATH_SEND_AUDIO = FileUtils.getFilePath(App.getContext(), "sendAudio/");
    public static final String FILE_PATH_RECEIVE_AUDIO = FileUtils.getFilePath(App.getContext(), "receiveAudio/");

    public static final int TYPE_ITEM_SEND_TEXT = 0x000;
    public static final int TYPE_ITEM_RECEIVE_TEXT = 0x001;
    public static final int TYPE_ITEM_SEND_AUDIO = 0x002;
    public static final int TYPE_ITEM_RECEIVE_AUDIO = 0x003;

}
