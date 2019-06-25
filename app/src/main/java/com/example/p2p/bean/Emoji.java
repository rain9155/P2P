package com.example.p2p.bean;

/**
 * Describe: 表情的实体类
  */

public class Emoji {

    private int id;
    private int unicodeInt;

    public Emoji() {
    }

    public Emoji(int id, int unicodeInt) {
        this.id = id;
        this.unicodeInt = unicodeInt;
    }

    public String getEmojiString() {
        return  getEmojiStringByUnicode(unicodeInt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUnicodeInt() {
        return getEmojiStringByUnicode(unicodeInt);
    }

    public void setUnicodeInt(int unicodeInt) {
        this.unicodeInt = unicodeInt;
    }

    public static String getEmojiStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    @Override
    public String toString() {
        return "Emoji[" +
                "itemType=" + id +
                ", unicodeInt=" + unicodeInt +
                ']';
    }
}
