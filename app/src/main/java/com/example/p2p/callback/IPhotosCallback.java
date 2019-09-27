package com.example.p2p.callback;

import com.example.p2p.bean.Folder;

import java.util.ArrayList;
import java.util.List;

/**
 * 从应用外部加载图片成功回调
 * Created by 陈健宇 at 2019/9/27
 */
public interface IPhotosCallback {

    void onSuccess(List<Folder> folders);


}
