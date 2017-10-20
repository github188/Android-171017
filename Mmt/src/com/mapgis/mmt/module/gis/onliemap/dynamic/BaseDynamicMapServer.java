package com.mapgis.mmt.module.gis.onliemap.dynamic;

import com.zondy.mapgis.map.MapServerBrowseType;
import com.zondy.mapgis.map.VectorMapServer;

public class BaseDynamicMapServer extends VectorMapServer {

    private static final long serialVersionUID = 1L;

    @Override
    public MapServerBrowseType getMapBrowseType() {
        return MapServerBrowseType.MapVector;
    }
}