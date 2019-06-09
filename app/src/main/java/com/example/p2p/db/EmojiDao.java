package com.example.p2p.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.p2p.app.App;
import com.example.p2p.bean.Emoji;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * 表情数据库操作
 */
public class EmojiDao {

    private final String TAG = this.getClass().getSimpleName();

    private final static String FILE_DIR = "data/data/" + App.getContext().getPackageName() + "/databases";
    private String mPath;
    private static EmojiDao sDao;

    public static EmojiDao getInstance(){
        if (sDao == null){
            synchronized (EmojiDao.class){
                if (sDao == null){
                    sDao = new EmojiDao();
                }
            }
        }
        return sDao;
    }
    private EmojiDao(){
        try {
            mPath = CopyRawFromAssetsToDatabases("emoji.db");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将assets目录下的文件拷贝到database中
     * @return 存储数据库的地址
     */
    public static String CopyRawFromAssetsToDatabases(String SqliteFileName) throws IOException {
        // 第一次运行应用程序时，加载数据库到data/data/当前包的名称/database/<db_name>
        File dir = new File(FILE_DIR);

        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        File file= new File(dir, SqliteFileName);

        //通过IO流的方式，将assets目录下的数据库文件，写入到SD卡中。
        if (!file.exists()) {
            try(
                    InputStream inputStream = App.getContext().getAssets().open(SqliteFileName);
                    OutputStream outputStream = new FileOutputStream(file)
            ){
                file.createNewFile();
                //inputStream = App.getContext().getClass().getClassLoader().getResourceAsStream("assets/" + SqliteFileName);
                byte[] buffer = new byte[1024];
                int len ;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer,0,len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.getPath();
    }

    /**
     * 获得表情包列表
     */
    public List<Emoji> getEmojiBeanList(){
        List<Emoji> emojiBeanList = new ArrayList<>();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.query("emoji", new String[]{"unicodeInt","_id"}, null, null, null, null, null);
        while (cursor.moveToNext()){
            Emoji bean = new Emoji();
            int unicodeInt = cursor.getInt(0);
            int id = cursor.getInt(1);
            bean.setUnicodeInt(unicodeInt);
            bean.setId(id);
            emojiBeanList.add(bean);
        }
        return emojiBeanList;
    }
}
