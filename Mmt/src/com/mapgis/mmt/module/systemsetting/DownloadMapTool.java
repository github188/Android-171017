package com.mapgis.mmt.module.systemsetting;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MapLayerConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DownloadMapTool {

    public static List<DownloadMap> initMapUpdateInfos() {
        List<DownloadMap> downloadMaps = new ArrayList<>();

        if (MobileConfig.MapConfigInstance == null) {
            return downloadMaps;
        }

        for (String t : new String[]{MapConfig.MOBILE_GOOGLE_LOCAL, MapConfig.MOBILE_EMS_DX, MapConfig.MOBILE_EMS}) {
            MapLayerConfig mc = MobileConfig.MapConfigInstance.getMapType(t);

            if (mc != null && !BaseClassUtil.isNullOrEmptyString(mc.Name)) {
                downloadMaps.add(hasNew(mc.Name));
            }
        }

        return downloadMaps;
    }

    /**
     * 判断该地图是否有新文件需要更新<br>
     *
     * @param mapName 地图名称
     * @return 需下载地图
     */
    public static DownloadMap hasNew(String mapName) {

        DownloadMap downloadMap = new DownloadMap();
        downloadMap.hasNew = false;
        downloadMap.MapName = mapName;
        downloadMap.ServerTime = "1900-01-01 00:00:00";

        try {

            if (BaseClassUtil.isNullOrEmptyString(mapName) || mapName.startsWith("http://")) {
                return downloadMap;
            }

            String nativeTime = GisUtil.getMapLastUpdateTime(mapName);

            // 访问服务，获取服务端对应瓦片和矢量的最近修改时间
            String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/GetMapTime";

            String result = NetUtil.executeHttpGet(url, "mapFileName", mapName + ".zip");

            if (result == null || result.trim().length() == 0) {
                return downloadMap;
            }

            ResultData<String> data = new Gson().fromJson(result, new TypeToken<ResultData<String>>() {
            }.getType());

            if (data.ResultCode < 0) {
                return downloadMap;
            }

            downloadMap.ServerTime = data.DataList.get(0);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

            // 当本地更新的时间小于服务器该文件的最近更新时间，则进行更新
            if (format.parse(nativeTime).getTime() < format.parse(downloadMap.ServerTime).getTime()) {
                downloadMap.hasNew = true;
                return downloadMap;
            } else {
                return downloadMap;
            }
        } catch (Exception e) {
            return downloadMap;
        }
    }
}
