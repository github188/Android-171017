package com.mapgis.mmt.common.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.mapgis.mmt.R;

import java.lang.reflect.Field;

public class ResourceUtil {

    /**
     * 应用程序版本名称
     */
    private static String versionName;

    /**
     * @param name drawable资源名称
     * @return 根据资源名称反射得到drawable资源ID
     */
    public static int getDrawableResourceId(String name) {
        try {
            if (BaseClassUtil.isNullOrEmptyString(name)) {
                return -1;
            }
            Field field = R.drawable.class.getField(name);

            int i = field.getInt(new R.drawable());

            return i;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 根据资源名称反射得到drawable资源ID
     *
     * @param name         资源名称
     * @param defaultValue 默认值
     * @return 资源ID
     */
    public static int getDrawableResourceId(String name, int defaultValue) {
        try {
            if (BaseClassUtil.isNullOrEmptyString(name)) {
                return defaultValue;
            }

            Field field = R.drawable.class.getField(name);

            int i = field.getInt(new R.drawable());

            return i;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int getDrawableResourceId(Context context, String name) {
        if (context == null) {
            return -1;
        }
        if (TextUtils.isEmpty(name)) {
            return -1;
        }
        try {
            return context.getResources().getIdentifier(name, "drawable", context.getPackageName());

        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * 得到资源id
     *
     * @param s1 资源类名
     * @param s2 类型
     * @param s3 名字
     * @return
     */
    public static int getResourceId(String s1, String s2, String s3) {
        if (s1 == null || s2 == null || s3 == null) {
            return -1;
        } else {
            Object obj;
            Class<?> class1;
            try {
                class1 = Class.forName((new StringBuilder(String.valueOf(s1))).append("$").append(s2).toString());
                Field field = class1.getField(s3);
                obj = field.get(class1.newInstance());
                return Integer.parseInt(obj.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return -1;
        }

    }

    /**
     * 获取应用程序版本名称
     */
    public static String getVersionName(Context context) {

        if (BaseClassUtil.isNullOrEmptyString(versionName)) {
            try {
                versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                versionName = "";
            }
        }
        return versionName;
    }

}
