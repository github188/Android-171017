package com.mapgis.mmt.module.gis.place;

import android.location.Location;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.toolbar.analyzer.PlaceSearch;
import com.mapgis.mmt.module.gis.toolbar.analyzer.gisserver.GisServerPlaceSearch;
import com.mapgis.mmt.module.gis.toolbar.analyzer.local.LocalPlaceSearch;
import com.zondy.mapgis.android.mapview.MapView;

public class BDGeocoder {
    public static BDGeocoderResult find(Location location) {
        try {
            if (location == null || location.getLatitude() <= 0 || location.getLongitude() <= 0) {
                return null;
            }

            String key = MyApplication.getInstance().getConfigValue(BDDirectionAPI.BD_DEV_KEY).length() == 0 ? MyApplication
                    .getInstance().getString(R.string.bd_dev_key) : MyApplication.getInstance().getConfigValue(
                    BDDirectionAPI.BD_DEV_KEY);

            String url = NetUtil.resolveRequestURL("http://api.map.baidu.com/geocoder/v2/", "ak", key, "coordtype", "wgs84ll",
                    "location", location.getLatitude() + "," + location.getLongitude(), "output", "json", "pois", "1");

            if (MyApplication.getInstance().getConfigValue("NetWay").equalsIgnoreCase("proxy"))
                url = ServerConnectConfig.getInstance().getHostPath() + "/OGCProxy/proxy.ashx?" + url;

            String result = NetUtil.executeHttpGet(url);

            if (BaseClassUtil.isNullOrEmptyString(result)) {
                return null;
            } else {
                return new Gson().fromJson(result, BDGeocoderResult.class);
            }
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * 根据地名关键字搜索
     *
     * @param mapView
     * @param address
     * @param page
     * @return
     */
    public static Object locFromAddressUtil(MapView mapView, String address, int page) {
        try {
            PlaceSearch search = null;

            if (MyApplication.getInstance().getConfigValue("PlaceSearch").equalsIgnoreCase("Native")) {
                String[] configParams = MyApplication.getInstance().getConfigValue("NativePlaceSearch").split("\\|");

                search = new LocalPlaceSearch(mapView, address, configParams[0], configParams[1], configParams[2], configParams[3], page,
                        Integer.parseInt(configParams[4]));
            } else if (MyApplication.getInstance().getConfigValue("PlaceSearch").equalsIgnoreCase("GisServer")) {
                search = new GisServerPlaceSearch(address);
            } else {
                search = new BDPlaceSearch(address);
            }

            return search.search(page);

//            String region = MyApplication.getInstance().getConfigValue("BdSearchCity");
//
//            String key = MyApplication.getInstance().getConfigValue(BDDirectionAPI.BD_DEV_KEY).length() == 0 ? MyApplication
//                    .getInstance().getString(R.string.bd_dev_key) : MyApplication.getInstance().getConfigValue(
//                    BDDirectionAPI.BD_DEV_KEY);
//
//            return NetUtil.executeHttpGet("http://api.map.baidu.com/place/v2/search", "q", address, "region", region, "output", "json", "ak", key);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Object locFromAddressUtil(String address, int page) {

        try {
            PlaceSearch search = null;

             if (MyApplication.getInstance().getConfigValue("PlaceSearch").equalsIgnoreCase("GisServer")) {
                search = new GisServerPlaceSearch(address);
            } else {
                search = new BDPlaceSearch(address);
            }

            return search.search(page);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
