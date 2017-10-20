package com.mapgis.mmt.module.gis.place;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.toolbar.analyzer.PlaceSearch;

public class BDPlaceSearch extends PlaceSearch {

    private final String place;
    private final String city;
    private final String key;

    public BDPlaceSearch(String place) {
        super(place);

        this.place = place;

        this.city = MyApplication.getInstance().getConfigValue(BDDirectionAPI.BD_SEARCH_CITY).length() == 0 ? MyApplication.getInstance()
                .getString(R.string.bd_search_city) : MyApplication.getInstance().getConfigValue(BDDirectionAPI.BD_SEARCH_CITY);

        this.key = MyApplication.getInstance().getConfigValue(BDDirectionAPI.BD_DEV_KEY).length() == 0 ? MyApplication.getInstance()
                .getString(R.string.bd_dev_key) : MyApplication.getInstance().getConfigValue(BDDirectionAPI.BD_DEV_KEY);
    }

    @Override
    public BDPlaceSearchResult search(int page) {
        try {
            String url = NetUtil.resolveRequestURL("http://api.map.baidu.com/place/v2/search", "q", place, "region", city, "page_num",
                    String.valueOf(page), "output", "json", "ak", key);

            if (MyApplication.getInstance().getConfigValue("NetWay").equalsIgnoreCase("proxy"))
                url = ServerConnectConfig.getInstance().getHostPath() + "/OGCProxy/proxy.ashx?" + url;

            String result = NetUtil.executeHttpGet(url);

            if (BaseClassUtil.isNullOrEmptyString(result)) {
                return null;
            } else {
                return new Gson().fromJson(result, BDPlaceSearchResult.class);
            }
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}
