package com.mapgis.mmt.common.util;

import com.mapgis.mmt.MyApplication;

/**
 * Created by Comclay on 2017/4/25.
 */

public class ResourseUtil {
    public static String getString(int srcId){
        return MyApplication.getInstance().getResources().getString(srcId);
    }
}
