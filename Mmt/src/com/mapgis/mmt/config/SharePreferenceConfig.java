package com.mapgis.mmt.config;

import com.mapgis.mmt.MyApplication;

/**
 * Created by Comclay on 2017/6/5.
 * SharePreference配置的读取存储工具类
 */

public class SharePreferenceConfig {

    /**
     * 读取配置文件中的定位方式
     * @return  定位方式的文字描述
     */
    public static String getGpsProvider() {
        String key = MyApplication.getInstance().getSystemSharedPreferences().getString("GpsReceiver", "");
        String provider = "默认配置";
        switch (key) {
            case "BD":
                provider = "综合定位";
                break;
            case "Native":
                provider = "卫星定位";
                break;
            case "HC":
                provider = "华测定位";
                break;
            case "NC":
                provider = "南方定位";
                break;
            case "BT":
                provider = "蓝牙定位";
                break;
            case "GD":
                provider = "高德定位";
                break;
            case "RD":
                provider = "内测定位";
                break;
            case "NMEA":
            case "HZ":
                provider = "合众思壮定位";
                break;
            case "ZHD":
                provider = "中海达定位";
                break;
        }
        return provider;
    }
}
