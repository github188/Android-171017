package com.mapgis.mmt.common.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mapgis.mmt.MyApplication;

/**
 * Created by Comclay on 2017/4/19.
 * 网络帮助类
 */

public class ConnectivityUtil {

    /**
     * 判断当前网络是否可用
     */
    public static boolean isNetworkUsable() {
        ConnectivityManager manager = getConnectivityManager();
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.isAvailable();
    }

    /**
     * 判断当前网络类型是不是WiFi
     */
    public static boolean isWiFi() {
        ConnectivityManager manager = getConnectivityManager();
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
        if (activeNetworkInfo == null){
            return false;
        }
        return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isNetworkTypeMobile(int networkType) {
        return ConnectivityManager.TYPE_MOBILE == networkType;
    }

    public static ConnectivityManager getConnectivityManager() {
        Context applicationContext = MyApplication.getInstance().getApplicationContext();
        return (ConnectivityManager) applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
    }

    public static NetworkInfo getActiveNetworkInfo() {
        return getConnectivityManager().getActiveNetworkInfo();
    }
}
