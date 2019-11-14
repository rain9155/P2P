package com.example.p2p.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import com.example.p2p.R;
import com.example.p2p.bean.ItemType;
import com.example.p2p.config.FileType;
import com.example.utils.FileProvider7;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 图片工具类
 * Created by 陈健宇 at 2019/6/20
 */
public class ImageUtil {

    private static final String TAG = ImageUtil.class.getSimpleName();

    /**
     * 根据图片Uri保存图片
     * @param imageUri 图片Uri
     * @param ip 目标用户ip
     * @return 保存图片的路径
     */
    public static String saveImageByUri(Context context, Uri imageUri, String ip){
        String path = FileUtil.getImagePath(ip, ItemType.SEND_IMAGE);
        String name = System.currentTimeMillis() + ".png";
        File file = new File(path + name);
        try(
                InputStream in = context.getContentResolver().openInputStream(imageUri);
                OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
        ) {
            if(!file.exists()) file.createNewFile();
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            os.write(bytes);
            return file.getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "保存图片失败");
        return "";
    }

    /**
     * 根据图片路径创建图片Uri
     * @param path 图片的真实路径
     * @param name  图片名字
     * @return 图片Uri
     */
    public static Uri getImageUri(Context context, String path, String name) {
        File fileOutPutImage = new File(path, name);
        if(!fileOutPutImage.exists()){
            try {
                fileOutPutImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return FileProvider7.getUriForFile(context, fileOutPutImage);
    }

    /**
     * 根据文件类型，获得相应的icon
     * @param fileType 文件类型
     * @return icon
     */
    public static int getImageId(String fileType) {
        int id;
        switch (fileType){
            case FileType.PDF:
                id = R.drawable.ic_pdf;
                break;
            case FileType.PPT:
            case FileType.PPTX:
                id = R.drawable.ic_ppt;
                break;
            case FileType.XLS:
            case FileType.XLSX:
                id = R.drawable.ic_excel;
                break;
            case FileType.DOC:
            case FileType.DOCX:
                id = R.drawable.ic_word;
                break;
            case FileType.TXT:
                id = R.drawable.ic_txt;
                break;
            case FileType.ZIP:
            case FileType.RAR:
                id = R.drawable.ic_zip;
                break;
            default:
                id = R.drawable.ic_unknow_file;
                break;
        }
        return id;
    }

}
