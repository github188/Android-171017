package com.mapgis.mmt.config;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.constant.GlobalPathManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

/**
 * 矢量图层配置和高亮字段配置加载类
 *
 * @author Zoro
 */
public class LayerConfig {
    private static LayerConfig instance = null;

    public static LayerConfig getInstance() {
        if (instance == null) {
            instance = new LayerConfig();
        }

        return instance;
    }

    private final String LOAD_FROM_SD_CARD_FAILED = "离线模式下加载SD卡下的图层配置文档失败，已转为加载内置文档";
    private final String LOAD_FROM_WEB_FAILED = "在线模式下加载服务端的图层配置文档失败，已转为加载内置文档";
    private final String LOAD_FAILED = "图层配置文档加载失败";
    private final String LOAD_SUCCESS = "图层配置文档正常加载成功";

    private String path;

    private final Hashtable<String, LayerConfigInfo> layerConfigMap = new Hashtable<String, LayerConfigInfo>();

    public LayerConfigInfo getConfigInfo(String layerName) {
        if (layerConfigMap.containsKey(layerName)) {
            return layerConfigMap.get(layerName);
        } else {
            return new EmptyLayerConfigInfo(layerName, infos == null);
        }
    }

    public String loadByNetState(boolean isOffline) {
        try {

            if (MobileConfig.MapConfigInstance == null) {
                return LOAD_FAILED;
            }

            String name;

            if (MobileConfig.MapConfigInstance.IsVectorQueryOnline)
                name = MobileConfig.MapConfigInstance.VectorService;
            else
                name = MobileConfig.MapConfigInstance.getMapType(MapConfig.MOBILE_EMS).Name;

            if (TextUtils.isEmpty(name))
                return LOAD_FAILED;

            path = "Layers/" + name + "_config.json";

            String result;

            if (isOffline) {
                result = loadFromSDCard();
            } else {
                result = loadFromWeb();
            }

            if (infos != null && infos.length > 0) {
                for (LayerConfigInfo pair : infos) {
                    layerConfigMap.put(pair.LayerName, pair);
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();

            return LOAD_FAILED;
        }
    }

    LayerConfigInfo[] infos;

    private String loadFromWeb() throws Exception {
        String result = NetUtil.downloadStringResource(path);

        if (!BaseClassUtil.isNullOrEmptyString(result)) {
            infos = new Gson().fromJson(result, LayerConfigInfo[].class);

            return LOAD_SUCCESS;
        } else {
            return LOAD_FROM_WEB_FAILED;
        }
    }

    private String loadFromSDCard() throws Exception {
        File file = new File(GlobalPathManager.getLocalConfigPath() + path);

        if (file.exists()) {
            loadFromInputStream(new FileInputStream(file));

            return LOAD_SUCCESS;
        } else {
            return LOAD_FROM_SD_CARD_FAILED;
        }
    }

    private void loadFromInputStream(InputStream is) throws Exception {
        InputStreamReader reader = null;

        try {
            reader = new InputStreamReader(is, "gb2312");

            infos = new Gson().fromJson(reader, LayerConfigInfo[].class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
