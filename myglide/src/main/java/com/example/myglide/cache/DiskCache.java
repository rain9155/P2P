package com.example.myglide.cache;

import java.io.InputStream;

/**
 * 硬盘缓存
 * Created by 陈健宇 at 2019/11/4
 */
public interface DiskCache {

    /**
     * 将一张图片存储到硬盘缓存中
     * @param key 图片的唯一标识
     * @param inputStream 要写入硬盘的图片流
     */
    void put(String key, InputStream inputStream);

    /**
     * 从硬盘缓存中获取一张图片，如果不存在就返回null
     * @param key 图片的唯一标识
     * @return 如果找到就返回图片，否则返回null
     */
    InputStream get(String key);

    /**
     * 从硬盘缓存中删除一张图片
     * @param key 图片的唯一标识
     */
    void remove(String key);

    /**
     * 用于清理所有硬盘缓存
     */
    void clear();

}
