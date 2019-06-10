package com.example.p2p.bean;

import androidx.annotation.NonNull;

/**
 * 聊天内容
 * Created by 陈健宇 at 2019/6/10
 */
public class Message {

    private int id;
    private String text;

    public Message(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return "Message[id = " + id + ", text = " + text + "]";
    }
}
