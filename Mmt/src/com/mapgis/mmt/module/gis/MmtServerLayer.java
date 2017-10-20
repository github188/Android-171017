package com.mapgis.mmt.module.gis;

import com.zondy.mapgis.map.MapServer;
import com.zondy.mapgis.map.ServerLayer;

/**
 * Created by zoro at 2017/9/15.
 */
public class MmtServerLayer extends ServerLayer {
    private MapServer mapServer;

    @Override
    public void setMapServer(MapServer mapServer) {
        super.setMapServer(mapServer);

        this.mapServer = mapServer;
    }

    @Override
    public MapServer getMapServer() {
        return mapServer;
    }
}
