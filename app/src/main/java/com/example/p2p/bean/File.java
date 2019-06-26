package com.example.p2p.bean;

import androidx.annotation.NonNull;

/**
 * 文件数据类
 * Created by 陈健宇 at 2019/6/26
 */
public class File {

    public String filePath;
    public String fileName;
    public int len;//字节流长度
    public String fileSize;//文件长度，单位KB或M
    public int progress;
    public String fileType;

    public File(String path, String name, String size, String type) {
        this(path, name, 0, size,  0, type);
    }

    public File(String path, String name, int len, String size, String type) {
        this(path, name, len, size,0, type);
    }

    public File(String path, String name, int len, String size, int progress, String type) {
        this.filePath = path;
        this.fileName = name;
        this.len = len;
        this.fileSize = size;
        this.progress = progress;
        this.fileType = type;
    }

    @NonNull
    @Override
    public String toString() {
        return "File[filePath = " + filePath
                + ", fileSize = " + fileSize
                + ", len = " + len
                + ", fileType = " + fileType
                + "]";
    }

}
