package com.mapgis.mmt.module.gis.onliemap.tile;

public class WMTSTileMapServer extends MmtTileMapServer {
    private static final long serialVersionUID = 1L;

    public WMTSTileMapServer(String name,String url) {
        try {
            mapName=name;

            if (url.endsWith("/"))
                url = url.substring(0, url.lastIndexOf("/"));

            this.url = url;

            setMapServiceInfo(url);

            initCacheDB(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
