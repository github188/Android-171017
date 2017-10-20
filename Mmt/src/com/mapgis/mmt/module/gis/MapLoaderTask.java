package com.mapgis.mmt.module.gis;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MapLayerConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.MapMenuRegistry;
import com.mapgis.mmt.module.gis.onliemap.Extent;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.onliemap.dynamic.EmsDBMapServer;
import com.mapgis.mmt.module.gis.onliemap.dynamic.MmtDynamicMapServer;
import com.mapgis.mmt.module.gis.onliemap.tile.BaseTileMapServer;
import com.mapgis.mmt.module.gis.onliemap.tile.MmtTileMapServer;
import com.mapgis.mmt.module.gis.onliemap.tile.LocalTileMapServer;
import com.mapgis.mmt.module.gis.onliemap.tile.WMTSTileMapServer;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.Document;
import com.zondy.mapgis.map.Map;
import com.zondy.mapgis.map.MapServerAccessMode;
import com.zondy.mapgis.map.ServerLayer;

import java.io.File;
import java.util.ArrayList;

public class MapLoaderTask extends AsyncTask<MapView, Integer, String> {
    MapView mapView;
    Document document = new Document();
    Map map = new Map();

    protected ArrayList<ServerLayer> referencedServerLayers = new ArrayList<>();

    @Override
    protected String doInBackground(MapView... params) {
        try {
            this.mapView = params[0];

            map.setName("地图文档");

            loadMobileMap();

            mapCorrectCheck();

            //放开限制，无论在线离线都从gis查一遍，方便获取图层ID
            String svcName = MobileConfig.MapConfigInstance.VectorService;

            if (TextUtils.isEmpty(svcName)) {
                svcName = MyApplication.getInstance().getConfigValue("svcName");
            }

            if (!TextUtils.isEmpty(svcName)) {
                MobileConfig.MapConfigInstance.VectorService = svcName;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_Map/REST/MapREST.svc/" + svcName + "/MapServer/"
                        + "?userid=" + MyApplication.getInstance().getUserId() + "&f=json&token=";

                MapServiceInfo.fromNetwork(url);// 缓存元数据
            }

            MapMenuRegistry.getInstance().registQueryMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void mapCorrectCheck() {
        Rect rect = map.getRange();

        boolean isNoMap = 0 == rect.getYMin() && 0 == rect.getYMax() && 0 == rect.getXMin() && 0 == rect.getXMax();
        if (isNoMap) {
            return;
        }

        if ((rect.getYMin() >= rect.getYMax() || (rect.getXMin() >= rect.getXMax()))) {
            MyApplication.getInstance().showMessageWithHandle("地图数据可能存在飞点，请修正飞点后重新剪裁");
        }
    }

    /**
     * 加载离线矢量EMS地图,仅加载遇到的首个存在的离线文档,返回加载的EMS地图的所在索引位置
     */
    private int loadEMSDocument() {
        for (int i = 0; i < MobileConfig.MapConfigInstance.Layers.size(); i++) {
            try {
                MapLayerConfig layer = MobileConfig.MapConfigInstance.Layers.get(i);

                if (!layer.Type.equals(MapConfig.MOBILE_EMS)) {
                    continue;
                }

                String path = MyApplication.getInstance().getMapFilePath() + layer.Name + "/" + layer.Name + ".mapx";


                if (new File(path).exists()) {
                    document.open(path);

                    map = document.getMaps().getMap(0);

                    return i;
                } else {
                    MyApplication.getInstance().showMessageWithHandle("指定路径未找到地图文档");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return -1;
    }

    /**
     * 加载手持地图配置
     */
    private void loadMobileMap() {
        int index = loadEMSDocument();

        for (int i = 0; i < MobileConfig.MapConfigInstance.Layers.size(); i++) {
            try {
                if (i == index) {
                    continue;
                }

                MapLayerConfig layer = MobileConfig.MapConfigInstance.Layers.get(i);

                ServerLayer serverLayer = loadNotEMSLayer(layer);

                if (serverLayer == null) {
                    continue;
                }

                serverLayer.setAccessMode(MapServerAccessMode.ServerOnly);

                if (!layer.Visible.equalsIgnoreCase("true")) {
                    serverLayer.setVisible(false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        for (int i = 0; i < referencedServerLayers.size(); i++) {
            ServerLayer layer = referencedServerLayers.get(i);

            //所有瓦片图层使用一致的自动缩放标志，解决高清普清图层混合导致显示比例错乱问题；存在高清图，统一不缩放
            if (layer.getMapServer() instanceof BaseTileMapServer)
                layer.setAutoScaleFlag(isScale);
            else
                layer.setAutoScaleFlag(false);//矢量地图不可开启自动缩放,否则图像错误

            if (index > -1) {
                map.insert(i, layer);
            } else {
                map.append(layer);
            }
        }
    }

    /**
     * 加载单个图层
     */
    protected ServerLayer loadNotEMSLayer(MapLayerConfig layer) {
        switch (layer.Type) {
            case MapConfig.MOBILE_DYNAMIC:
                return loadDynamicLayers(layer);
            case MapConfig.MOBILE_EMS_DX:
                return loadEmsDBLayers(layer);
            case MapConfig.MOBILE_GOOGLE_LOCAL:
            case MapConfig.MOBILE_TILED:
                return loadTileLayers(layer);
            default:
                return null;
        }
    }

    /**
     * 加载在线矢量地图
     */
    private ServerLayer loadDynamicLayers(MapLayerConfig layer) {
        try {
            String name = layer.Name;

            ServerLayer dynamicServerLayer = new MmtServerLayer();

            referencedServerLayers.add(dynamicServerLayer);

            String url = layer.Url;

            if (BaseClassUtil.isNullOrEmptyString(url)) {
                url = name;
            }

            MapServiceInfo info;

            if (!url.startsWith("http://") || !url.contains("{xmin}")) {// 仅仅传入矢量图的服务名l;或者传入cityinterface的矢量元数据地址
                if (!url.startsWith("http://")) {
                    url = ServerConnectConfig.getInstance().getBaseServerPath() + "/Services/Zondy_MapGISCitySvr_Map/REST/MapREST.svc/mobleAnn/"
                            + name + "/MapServer/";
                }

                info = MapServiceInfo.fromNetwork(url + (url.endsWith("/") ? "" : "/") + "?userid=" + MyApplication.getInstance().getUserId());

                if (info == null)
                    return null;

                url = url + (url.endsWith("/") ? "" : "/")
                        + "export?bbox={xmin}%2C{ymin}%2C{xmax}%2C{ymax}&bboxSR=1&layers={layers}&layerdefs=0:POP2000%3E1000000;5:AREA%3E100000"
                        + "&size={width}%2C{height}&imageSR=256&format=png&transparent=true&dpi=96&f=json&token=";

                info.setUrl(url);
            } else {// 通用的WMS服务，需要用占位符标示关键字，使用自定义配置范围或者全局范围
                info = new MapServiceInfo(url);

                info.setMapName(name);

                String extent = BaseClassUtil.isNullOrEmptyString(layer.Config) ? MobileConfig.MapConfigInstance.Fullextent : layer.Config;

                if (BaseClassUtil.isNullOrEmptyString(extent)) {
                    return null;
                }

                info.setFullExtent(new Extent(extent));
            }

            dynamicServerLayer.setName(name);

            if (BaseClassUtil.isNullOrEmptyString(MobileConfig.MapConfigInstance.VectorService)) {
                MobileConfig.MapConfigInstance.VectorService = layer.Name;
            }

            MmtDynamicMapServer mmtDynamicMapServer = new MmtDynamicMapServer(info);
            MyApplication.getInstance().putConfigValue("MmtDynamicMapServer", mmtDynamicMapServer);
            dynamicServerLayer.setMapServer(mmtDynamicMapServer);

            return dynamicServerLayer;
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }

    private ServerLayer loadEmsDBLayers(MapLayerConfig layer) {
        try {
            String name = layer.Name;

            String path = MyApplication.getInstance().getMapFilePath() + name + "/" + name + ".mapx";

            if (!new File(path).exists())
                return null;

            ServerLayer dynamicServerLayer = new MmtServerLayer();

            referencedServerLayers.add(dynamicServerLayer);

            dynamicServerLayer.setName(name);

            dynamicServerLayer.setMapServer(new EmsDBMapServer(name));

            return dynamicServerLayer;
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }

    private boolean isScale = true;

    /**
     * 加载外接瓦片数据
     */
    private ServerLayer loadTileLayers(MapLayerConfig layer) {
        try {
            BaseTileMapServer tileMapServer;
            String name = layer.Name;

            if (layer.Type.equals(MapConfig.MOBILE_GOOGLE_LOCAL)) {// 重裁剪的Google地图
                String path = MyApplication.getInstance().getMapFilePath() + name + "/" + name + ".xml";

                if (!new File(path).exists()) {
                    return null;
                }

                tileMapServer = new LocalTileMapServer(name);
            } else {// cityinterface发布的瓦片数据，无论IGServer的还是动态瓦片裁剪的
                if (!layer.Url.startsWith("http://") && !layer.Url.startsWith("https://"))
                    tileMapServer = new MmtTileMapServer(name);
                else
                    tileMapServer = new WMTSTileMapServer(name, layer.Url);
            }

            ServerLayer tileServerLayer = new MmtServerLayer();

            referencedServerLayers.add(tileServerLayer);

            tileServerLayer.setName(name);

            // 高清图关闭缩放；普清图开启缩放
            if (tileMapServer.getInfo().getHighresolution().equalsIgnoreCase("true"))
                isScale = false;

            tileServerLayer.setMapServer(tileMapServer);

            return tileServerLayer;
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        mapView.setMap(map);
    }
}
