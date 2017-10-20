package com.mapgis.mmt.constant;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.config.ServerConnectConfig;

import java.io.File;

public class GlobalPathManager {
    public static final String VERSION_FILE = "version.json";
    public static final String MOBILE_CONFIG_FILE = "mobile_config.json";
    public static final String TRANS_PARAMS_FILE = "trans_params.json";
    public static final String PRODUCT_FILE = "product.json";

    public static synchronized String getLocalConfigPath() {

        String dir = Battle360Util.getFixedPath("Data") + MyApplication.getInstance().getPackageName() + "/";

        File file = new File(dir);

        if (!file.exists()) {
            file.mkdirs();
        }

        return dir;
    }

    public static String getNetConfigPath() {
        return ServerConnectConfig.getInstance().getCityServerMobileConfigPath() + "/";
    }
}
