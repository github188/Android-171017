package com.mapgis.mmt.module.gis.toolbar.accident2.util;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultWithoutData;

/**
 * Created by Comclay on 2017/5/18.
 */

public class ConfigUtil {

    /**
     * http://localhost/CityInterface/Services/Zondy_MapGISCitySvr_MobileBusiness/SOAP
     * /MobileBusinessSOAP.svc/GetPipeBrokenConfig
     * <p>
     * 获取配置的导出字段
     */
    @NonNull
    public static String getBrokenOutFieldsConfig() {
        String config = "";
        try {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_MobileBusiness/SOAP/MobileBusinessSOAP.svc/GetPipeBrokenConfig";
            String result = NetUtil.executeHttpGet(url);

            if (!BaseClassUtil.isNullOrEmptyString(result)) {
                ResultWithoutData resultWithoutData = new Gson().fromJson(result, ResultWithoutData.class);
                if (resultWithoutData != null) {
                    config = resultWithoutData.ResultMessage;
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return config;
    }
}
