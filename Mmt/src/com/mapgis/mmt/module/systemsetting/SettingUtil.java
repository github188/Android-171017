package com.mapgis.mmt.module.systemsetting;

import android.content.SharedPreferences;

import com.mapgis.mmt.MyApplication;

/**
 * Created by Comclay on 2017/4/24.
 * 读取和存储配置数据的工具类
 * <p>
 * 所有配置集中管理，以后更改键值或存储方式都方便
 */

public class SettingUtil {

    public static class Config {
        // 自动下载
        public final static String AUTO_DOWNLOAD_ON_WIFI = "autoDownloadOnWiFi";
        // 显示网络延迟悬浮球
        public final static String PING_NET_DELAY = "MapPingNetDelay";
        // 大华视频的输出率
        public final static String DAHUA_VIEDEO_OUT_RATE = "DHBitRate";
        // 实时
        public final static String CONFIG_REALTIME_LOCATE = "isRealtimeLocate";
        //是否显示瓦片网格信息
        public final static String SHOW_TILE_GRID = "ShowTileGrid";
    }

    private static SharedPreferences getSharePreferences() {
        return MyApplication.getInstance().getSystemSharedPreferences();
    }

    public static void saveAutoDowloadSetting(boolean isAutoDownloadOnWiFi) {
        getSharePreferences()
                .edit()
                .putBoolean(Config.AUTO_DOWNLOAD_ON_WIFI, isAutoDownloadOnWiFi)
                .apply();
    }

    public static boolean loadAutoDownloadSetting() {
        return getSharePreferences().getBoolean(Config.AUTO_DOWNLOAD_ON_WIFI, false);
    }

    public static void saveConfig(String configKey, boolean configValue) {
        getSharePreferences().edit().putBoolean(configKey, configValue).apply();
    }

    public static void saveConfig(String configKey, String configValue) {
        getSharePreferences().edit().putString(configKey, configValue).apply();
    }

    public static void saveConfig(String configKey, int configValue) {
        getSharePreferences().edit().putInt(configKey, configValue).apply();
    }

    public static void saveConfig(String configKey, long configValue) {
        getSharePreferences().edit().putLong(configKey, configValue).apply();
    }

    public static void saveConfig(String configKey, float configValue) {
        getSharePreferences().edit().putFloat(configKey, configValue).apply();
    }

    public static boolean getConfig(String configKey, boolean defaultValue) {
        return getSharePreferences().getBoolean(configKey, defaultValue);
    }

    public static int getConfig(String configKey, int defaultValue) {
        return getSharePreferences().getInt(configKey, defaultValue);
    }

    public static long getConfig(String configKey, long defaultValue) {
        return getSharePreferences().getLong(configKey, defaultValue);
    }

    public static float getConfig(String configKey, float defaultValue) {
        return getSharePreferences().getFloat(configKey, defaultValue);
    }

    public static String getConfig(String configKey, String defaultValue) {
        return getSharePreferences().getString(configKey, defaultValue);
    }
}
