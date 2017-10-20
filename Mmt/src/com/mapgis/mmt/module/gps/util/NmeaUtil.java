package com.mapgis.mmt.module.gps.util;

import com.mapgis.mmt.common.util.BaseClassUtil;

/**
 * Created by Comclay on 2017/2/24.
 * 抽象处理类
 */

public abstract class NmeaUtil implements INmeaUtils {

    /**
     * Nmea信息的统一处理入口
     * // 当前卫星信息
     * $GPGSA,A,3,21,31,20,25,32,12,,,,,,,4.91,2.67,4.12*0C
     * <p>
     * // 可见卫星详细信息
     * $GPGSV,3,1,10,	10,59,324,16	,12,35,103,23	,14,19,281,	,18,86,093,	*78
     * $GPGSV,3,2,10,	20,17,135,31	,21,28,204,27	,24,42,041,	,25,28,152,25	*7B
     * $GPGSV,3,3,10,	31,13,223,31	,32,37,299,16	*79
     * <p>
     * $GPGST,030342.00,47,,,,23,6.7,22*52
     * $GPZDA,030342.00,23,02,2017,00,00*67
     * <p>
     * // 地面速度信息
     * N后面跟的是地面速率（公里/小时）
     * $GPVTG,,T,,M,0.841,N,1.557,K,A*28
     * // GPS定位信息
     * $GPGGA,030343.00,3028.02861,N,11424.08411,E,1,06,2.67,29.6,M,-10.6,M,,*43
     */
    public void handleNmea(String nmea) {
        if (BaseClassUtil.isNullOrEmptyString(nmea)) {
            return;
        }
        // 1，获取数据协议
        String dataType = nmea.substring(3, nmea.indexOf(","));
        switch (dataType) {
            case "GGA":
                handleGpgga(nmea);
                break;
            case "GSV":
                handleGpgsv(nmea);
                break;
            case "GST":
                handleGpgst(nmea);
                break;
            case "VTG":
                handleGpvtg(nmea);
                break;
            case "ZDA":
                handleGpzda(nmea);
                break;
            case "GSA":
                handleGpgsa(nmea);
                break;
            default:
                break;
        }
    }

    /**
     * 卫星基本信息必须处理
     *
     * @param gpgga 原始数据
     */
    public abstract void handleGpgga(String gpgga);

    public void handleGpgsv(String gpgsv) {
    }

    public void handleGpgst(String gpgst) {
    }

    public void handleGpgsa(String nmea) {
    }

    public void handleGpzda(String nmea) {

    }

    public void handleGpvtg(String nmea) {

    }
}
