package com.mapgis.mmt.engine;

import android.app.Activity;
import android.os.storage.StorageManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Comclay on 2016/12/15.
 * 获取sd卡的路径
 */

public class SDCardManager {
    private StorageManager mStorageManager;
    private Method mMethodGetPaths;

    public SDCardManager(Activity activity) {
        if (activity != null) {
            mStorageManager = (StorageManager) activity
                    .getSystemService(Activity.STORAGE_SERVICE);
            try {
                mMethodGetPaths = mStorageManager.getClass()
                        .getMethod("getVolumePaths");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] getVolumePaths() {
        String[] paths;
        try {
            paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            paths = new String[0];
        }
        return paths;
    }
}
