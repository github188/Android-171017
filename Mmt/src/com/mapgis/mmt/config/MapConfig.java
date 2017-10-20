package com.mapgis.mmt.config;

import java.util.ArrayList;
import java.util.List;

/**
 * <p/>
 * 地图类型管理类
 * <p/>
 * <p/>
 * 离线矢量，离线瓦片，在线矢量，在线瓦片
 */
public class MapConfig {
    public String Fullextent;
    public String VectorService;
    public boolean IsVectorQueryOnline;
    public List<MapLayerConfig> Layers = new ArrayList<>();

    public static final String MOBILE_EMS = "ems";//离线
    public static final String MOBILE_EMS_DX = "emsdx";//离线
    public static final String MOBILE_TILED = "tiled";//在线
    public static final String MOBILE_DYNAMIC = "dynamic";//在线
    public static final String MOBILE_GOOGLE_LOCAL = "googlelocal";//离线

    public static String[] MAP_TYPES = new String[]{"emsdx", "googlelocal", "tiled", "ems", "dynamic"};


    public MapLayerConfig getMapLayerByName(String name) {
        if (Layers == null || Layers.size() == 0) {
            return null;
        }
        MapLayerConfig mapType = null;
        for (MapLayerConfig type : Layers) {
            if (type.Name.equalsIgnoreCase(name)) {
                mapType = type;
                break;
            }
        }

        return mapType;
    }


    /**
     * 获取指定的地图类型信息
     */
    public MapLayerConfig getMapType(String mapTypeStr) {
        MapLayerConfig mapType = new MapLayerConfig();
        mapType.Type = mapTypeStr;
        mapType.Visible = "false";
        if (Layers != null && Layers.size() > 0) {
            for (MapLayerConfig type : Layers) {
                if (type.Type.equalsIgnoreCase(mapTypeStr) && type.Visible.equalsIgnoreCase("true")) {
                    mapType = type;
                    return mapType;
                }
            }
        }

        return mapType;
    }

    /**
     * 获取指定的地图类型信息
     */
    public int getMapTypeIndex(String mapTypeStr) {

        if (Layers == null || Layers.size() == 0) {
            return -1;
        }

        for (int i = 0; i < Layers.size(); i++) {
            MapLayerConfig type = Layers.get(i);
            if (type.Type.equalsIgnoreCase(mapTypeStr) && type.Visible.equalsIgnoreCase("true")) {

                return i;
            }
        }

        return -1;
    }

    public boolean isOfflineLayer(String type) {
        if (MOBILE_EMS.equalsIgnoreCase(type)) {
            return true;
        }
        if (MOBILE_EMS_DX.equalsIgnoreCase(type)) {
            return true;
        }
        if (MOBILE_GOOGLE_LOCAL.equalsIgnoreCase(type)) {
            return true;
        }
        return false;
    }

    public boolean isGISGW(String type) {
        if (MOBILE_EMS.equalsIgnoreCase(type)) {
            return true;
        }
        if (MOBILE_DYNAMIC.equalsIgnoreCase(type)) {
            return true;
        }
        return false;
    }
}
