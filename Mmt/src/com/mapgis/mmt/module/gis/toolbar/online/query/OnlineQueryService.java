package com.mapgis.mmt.module.gis.toolbar.online.query;

import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;

public class OnlineQueryService {
    /**
     * 在线点击查询服务地址
     */
    public static String getPointQueryService() {
        return ServerConnectConfig.getInstance().getBaseServerPath() + "/services/zondy_mapgiscitysvr_map/rest/maprest.svc/"
                + MobileConfig.MapConfigInstance.VectorService + "/mapserver/identify";
    }

    /**
     * 在线查询服务地址
     */
    public static String getOnlineQueryService(String layerId) {
        return ServerConnectConfig.getInstance().getBaseServerPath() + "/Services/Zondy_MapGISCitySvr_Map/REST/MapREST.svc/"
                + MobileConfig.MapConfigInstance.VectorService + "/MapServer/" + layerId + "/query";
    }

    /**
     * 在线图层属性字段服务地址
     */
    public static String getLayerAttributeService(String layerId) {
        return ServerConnectConfig.getInstance().getBaseServerPath() + "/Services/Zondy_MapGISCitySvr_Map/REST/MapREST.svc/"
                + MobileConfig.MapConfigInstance.VectorService + "/MapServer/" + layerId;
    }
}