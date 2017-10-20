package com.mapgis.mmt.config;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.DataDictNode;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.constant.GlobalPathManager;
import com.mapgis.mmt.entity.KeyValuePair;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务端自定义配置加载类
 *
 * @author Zoro
 */
public class MobileConfig {
    public static final String LOAD_FROM_SD_CARD_FAILED = "离线模式下加载SD卡下的配置文档失败，已转为加载内置文档";
    public static final String LOAD_FROM_WEB_FAILED = "在线模式下加载服务端的配置文档失败，已转为加载内置文档";
    public static final String LOAD_FAILED = "配置文档加载失败";
    public static final String LOAD_SUCCESS = "配置文档正常加载成功";

    public static MapConfig MapConfigInstance;

    public String loadByNetState(boolean isOffline) {
        try {
            String result;

            if (isOffline) {
                result = loadFromSDCard();
            } else {
                result = loadFromWeb();
            }

            if (pairs != null) {
                for (KeyValuePair kv : pairs) {
                    try {
                        if (TextUtils.isEmpty(kv.Key))
                            continue;

                        String val = TextUtils.isEmpty(kv.Value) ? "" : kv.Value;

                        //#通话录音路径# 类似这种格式的代码从数据字典取复杂构造的对象值
                        if (!isOffline && (val.length() > 2 && val.startsWith("#") && val.endsWith("#"))) {
                            val = val.substring(1, val.lastIndexOf("#"));

                            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                    + "/Services/MapgisCity_WorkFlow/REST/WorkFlowREST.svc/WorkFlow/0/";

                            String json = NetUtil.executeHttpGet(url, "nodeName", val);

                            DataDictNode[] nodes = new Gson().fromJson(json, DataDictNode[].class);

                            ArrayMap<String, String> map = new ArrayMap<>();

                            for (DataDictNode node : nodes) {
                                map.put(node.NODENAME, node.NODEVALUE);
                            }

                            MyApplication.getInstance().putConfigValue(kv.Key, map);
                        } else
                            MyApplication.getInstance().putConfigValue(kv.Key, val);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();

            return LOAD_FAILED;
        }
    }

    KeyValuePair[] pairs;

    private String loadFromWeb() throws Exception {
        // 从CityInterface获取MobileConfig的配置, 配置在SysDataDictionary表中
        List<KeyValuePair> mobileConfig = new ArrayList<>();

        String mobileConfigStr = loadMobileConfigFromCityInterface();

        if (!TextUtils.isEmpty(mobileConfigStr)) {

            ResultData<KeyValuePair> resultData = new Gson().fromJson(mobileConfigStr, new TypeToken<ResultData<KeyValuePair>>() {
            }.getType());

            if (resultData.ResultCode > 0 && resultData.DataList.size() > 0) {
                mobileConfig.addAll(resultData.DataList);
            }
        }

        // 从CityInterface获取MapConfig的配置，配置在\ConfCenter\OMS\MobileConfig.xml中
        String mapConfigStr = loadMapConfigFromCityInterface();

        if (!TextUtils.isEmpty(mapConfigStr)) {
            ResultData<MapConfig> resultData = new Gson().fromJson(mapConfigStr, new TypeToken<ResultData<MapConfig>>() {
            }.getType());

            if (resultData.ResultCode > 0) {
                MapConfigInstance = resultData.getSingleData();
            }
        }

        // 若从CityInterface没有更新到任一信息，则采用旧方式先获取到数据
        if (mobileConfig.size() == 0 || MapConfigInstance == null) {
            String result = NetUtil.downloadStringResource(GlobalPathManager.MOBILE_CONFIG_FILE);

            if (!BaseClassUtil.isNullOrEmptyString(result)) {
                pairs = new Gson().fromJson(result, KeyValuePair[].class);
            } else {
                return LOAD_FROM_WEB_FAILED;
            }
        }

        // 将原有JSON信息转换成MapConfig对象
        if (MapConfigInstance == null) {
            initMapConfig(pairs);
        }

        // 若读取的CityInterface手持配置有数据，则采用CityInterface获取的数据
        if (mobileConfig.size() != 0) {
            pairs = mobileConfig.toArray(new KeyValuePair[mobileConfig.size()]);
        }

        return LOAD_SUCCESS;
    }

    private String loadFromSDCard() throws Exception {
        String path = GlobalPathManager.getLocalConfigPath() + GlobalPathManager.MOBILE_CONFIG_FILE;

        File file = new File(path);

        //兼容配置文件在conf下的系统
        if (!file.exists()) {
            String srcPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Conf) + GlobalPathManager.MOBILE_CONFIG_FILE;
            File srcFile = new File(srcPath);
            if (srcFile.exists()) {
                FileUtil.copyFile(srcFile, file);
            }
        }

        if (!file.exists() && !FileUtil.copyAssetToSD("cfg/" + GlobalPathManager.MOBILE_CONFIG_FILE, path)) {
            return LOAD_FROM_SD_CARD_FAILED;
        }

        loadFromInputStream(new FileInputStream(file));

        return LOAD_SUCCESS;
    }

    private void loadFromInputStream(InputStream is) throws Exception {
        InputStreamReader reader = null;

        try {
            reader = new InputStreamReader(is, "utf-8");

            pairs = new Gson().fromJson(reader, KeyValuePair[].class);

            initMapConfig(pairs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * 当地图配置信息是从json文件获取时，调用此方法初始化地图配置实例
     */
    private void initMapConfig(KeyValuePair[] pairs) {
        MapConfigInstance = new MapConfig();

        List<MapLayerConfig> layerConfigs = new ArrayList<>();

        for (KeyValuePair kv : pairs) {
            String key = kv.Key;

            // 若没有设置值,则不向下执行
            if (BaseClassUtil.isNullOrEmptyString(kv.Value)) {
                continue;
            }

            if (key.equalsIgnoreCase("FullExtent")) {// 初始范围
                MapConfigInstance.Fullextent = kv.Value;
            } else if (key.equalsIgnoreCase("VectorService")) {// GIS服务器名称
                MapConfigInstance.VectorService = kv.Value;
            } else if (key.equalsIgnoreCase("isMapOnline")) {// 是否是在线地图对应的功能
                MapConfigInstance.IsVectorQueryOnline = !TextUtils.isEmpty(kv.Value) && kv.Value.equalsIgnoreCase("true");
            } else if (key.equalsIgnoreCase("OnlineDynamicName")) {// 在线矢量
                MapLayerConfig mapType = new MapLayerConfig(kv.Value, MapConfig.MOBILE_DYNAMIC, "true", "true");

                if (mapType.Name.startsWith("http://")) {// 在线矢量
                    mapType.Url = mapType.Name;
                    mapType.Name = "在线管网";
                }

                layerConfigs.add(mapType);
            } else if (key.equalsIgnoreCase("OnlineTileName")) {// 瓦片
                MapLayerConfig mapType = new MapLayerConfig(kv.Value, MapConfig.MOBILE_TILED, "true", "true");

                if (mapType.Name.startsWith("http://")) {// 在线瓦片
                    mapType.Url = mapType.Name;
                    mapType.Name = "在线瓦片";
                } else {// 离线瓦片
                    mapType.Type = MapConfig.MOBILE_GOOGLE_LOCAL;
                }

                layerConfigs.add(mapType);
            } else if (key.equalsIgnoreCase("地图文档名称")) {// 离线矢量
                MapLayerConfig mapType = new MapLayerConfig(kv.Value, MapConfig.MOBILE_EMS, "true", "true");
                layerConfigs.add(mapType);
            }
        }

        // 将地图按顺序压栈
        MapConfigInstance.Layers = new ArrayList<>();

        for (String mapType : MapConfig.MAP_TYPES) {
            for (MapLayerConfig config : layerConfigs) {
                if (config.Type.equals(mapType)) {
                    MapConfigInstance.Layers.add(config);
                }
            }
        }
    }

    /**
     * 调用CityInterface服务加载地图配置信息
     */
    private String loadMapConfigFromCityInterface() {
        try {
            UserBean bean = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);
            String url, json, mapRole = "";

            if (!TextUtils.isEmpty(bean.Role) && bean.Role.contains("地图_")) {
                for (String r : bean.Role.split(",")) {
                    if (r.startsWith("地图_")) {
                        mapRole = r;

                        break;
                    }
                }
            }

           // url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/GetMapConfig";
            url = "http://10.37.147.80/langfang/cityinterface/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/GetMapConfig";

            json = NetUtil.executeHttpGet(url, "mapRole", mapRole);

            return json;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * 调用CityInterface服务加载地图配置信息
     */
    private String loadMobileConfigFromCityInterface() {
        try {
            //String uri = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/GetMobileConfig";
            String uri = "http://10.37.147.80/langfang/cityinterface/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/GetMobileConfig";

            return NetUtil.executeHttpGet(uri, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
