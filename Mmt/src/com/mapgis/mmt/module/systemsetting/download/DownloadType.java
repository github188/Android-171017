package com.mapgis.mmt.module.systemsetting.download;

import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MapLayerConfig;
import com.mapgis.mmt.config.MobileConfig;

import java.util.List;

/**
 * Created by Comclay on 2017/4/26.
 * 下载类型
 */

public class DownloadType {
    public final static String MOBILE_SLIB = "slib";
    public final static String MOBILE_CLIB = "clib";

    public static String typeToDescription(String type) {
        String desc = "未知";
        switch (type) {
            case MapConfig.MOBILE_EMS:
                desc = "离线矢量图";
                break;
            case MapConfig.MOBILE_EMS_DX:
                desc = "离线地形图";
                break;
            case MapConfig.MOBILE_TILED:
                desc = "在线瓦片图";
                break;
            case MapConfig.MOBILE_DYNAMIC:
                desc = "在线矢量图";
                break;
            case MapConfig.MOBILE_GOOGLE_LOCAL:
                desc = "离线外接图";
                break;
            case MOBILE_CLIB:
                desc = "字体库";
                break;
            case MOBILE_SLIB:
                desc = "符号库";
                break;
        }
        return desc;
    }

    public static String getSuffix(DownloadInfo info) {
        return "db";
    }

    /**
     * 是否配置有离线地图
     */
    public static boolean hasOfflineMap() {
        if (MobileConfig.MapConfigInstance == null) {
            return true;
        }

        List<MapLayerConfig> layers = MobileConfig.MapConfigInstance.Layers;
        for (MapLayerConfig layerConfig : layers) {
            if (MobileConfig.MapConfigInstance.isOfflineLayer(layerConfig.Type)){
                return true;
            }
        }
        return false;
    }
}
