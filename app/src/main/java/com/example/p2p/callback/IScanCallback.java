package com.example.p2p.callback;

import java.util.List;

/**
 * 扫描回调接口
 * Created by 陈健宇 at 2019/6/7
 */
public interface IScanCallback {
    void onScanSuccess(List<String> pingSuccessList);
    void onScanEmpty();
    void onScanError();
}
