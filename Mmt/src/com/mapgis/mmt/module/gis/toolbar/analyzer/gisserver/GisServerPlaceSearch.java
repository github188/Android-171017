package com.mapgis.mmt.module.gis.toolbar.analyzer.gisserver;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.toolbar.analyzer.PlaceSearch;

public class GisServerPlaceSearch extends PlaceSearch {
    private final String place;

    public GisServerPlaceSearch(String place) {
        super(place);

        this.place = place;
    }

    @Override
    public Object search(int page) {
        try {

            String name = MobileConfig.MapConfigInstance.VectorService;

            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/services/zondy_mapgiscitysvr_locator/rest/locatorrest.svc/" + name
                    + "/geocodeserver/findAddressCandidates?f=json&type=&date=&KeyField=" + place;

            String result = NetUtil.executeHttpGetAppointLastTime(30, url);

            return new Gson().fromJson(result, LocatorGeocodeResult.class);

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}
