package com.mapgis.mmt.module.gps.util;

/**
 * Created by Comclay on 2017/2/24.
 * 用于处理NAME协议的卫星数据的工具类
 */

public interface INmeaUtils {
    /**
     * 处理GPGGA协议的GPS定位信息
     */
    void handleGpgga(String gpgga);

    /**
     * 处理GPGSV协议的可见卫星信息
     */
    void handleGpgsv(String gpgsv);

    /**
     * 处理GPGGA协议的卫星伪距统计信息
     */
    void handleGpgst(String gpgst);

    /**
     * 处理GPGSA协议的当前卫星信息
     */
    void handleGpgsa(String nmea);

    /**
     * 处理GPZDA协议的全球时间信息
     */
    void handleGpzda(String nmea);

    /**
     * 处理GPVTG协议的速度和航向信息
     */
    void handleGpvtg(String nmea);
}
