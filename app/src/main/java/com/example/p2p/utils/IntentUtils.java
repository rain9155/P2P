package com.example.p2p.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取Intent工具类
 * Created by 陈健宇 at 2019/6/24
 */
public class IntentUtils {

    public static List<Intent> getGalleryIntents(@NonNull PackageManager packageManager, String action, boolean includeDocuments) {
        List<Intent> intents = new ArrayList<>();
        Intent galleryIntent =
                action == Intent.ACTION_GET_CONTENT
                        ? new Intent(action)
                        : new Intent(action, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            intents.add(intent);
        }

        // remove documents intent
        if (!includeDocuments) {
            for (Intent intent : intents) {
                if (intent
                        .getComponent()
                        .getClassName()
                        .equals("com.android.documentsui.DocumentsActivity")) {
                    intents.remove(intent);
                    break;
                }
            }
        }
        return intents;
    }

}
