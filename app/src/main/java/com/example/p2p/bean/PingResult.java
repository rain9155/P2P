package com.example.p2p.bean;

/**
 * Ping的请求过程的状态
 * Created by 陈健宇 at 2019/6/7
 */
public class PingResult {

    public static int normal = 0;
    public static int empty = 1;
    public static int network = 2;
    public static int error = 3;

    private int status = normal;

    public void setStatus(int status){
        this.status = status;
    }

}
