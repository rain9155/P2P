package com.example.p2p.utils;

import com.example.p2p.bean.Data;
import com.google.gson.Gson;

/**
 * 简单的json转换
 * Created by 陈健宇 at 2019/6/12
 */
public class JsonUtils {

    private static Gson sGson = new Gson();

    /**
     * Object类转成json
     */
    public static String toJson(Object object){
        return sGson.toJson(object);
    }

    /**
     * json转成Object类
     */
    public static <T> T toObject(String json, Class<T> object) {
        return sGson.fromJson(json, object);
    }

}
