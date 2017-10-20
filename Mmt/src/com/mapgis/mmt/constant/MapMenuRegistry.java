package com.mapgis.mmt.constant;

import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BackNavigationMapMenu;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.EmptyDefinedMapMenu;
import com.mapgis.mmt.module.gis.toolbar.ForMoreMapMenu;
import com.mapgis.mmt.module.gis.toolbar.MapScanLocMapMenu;
import com.mapgis.mmt.module.gis.toolbar.accident.PipeAccidentMenu;
import com.mapgis.mmt.module.gis.toolbar.accident.gas.BurstAnalysisMapMenu;
import com.mapgis.mmt.module.gis.toolbar.accident2.PipeBrokenAnalysisMenu;
import com.mapgis.mmt.module.gis.toolbar.analyzer.AddressSearchMenu;
import com.mapgis.mmt.module.gis.toolbar.clearmap.ClearCacheMapMenu;
import com.mapgis.mmt.module.gis.toolbar.errimgreport.ErrImgReportMapMenu;
import com.mapgis.mmt.module.gis.toolbar.gps.Plane2WGSMapMenu;
import com.mapgis.mmt.module.gis.toolbar.gps.WGS2PlaneMapMenu;
import com.mapgis.mmt.module.gis.toolbar.layercontrol.LayerControlMapMenu;
import com.mapgis.mmt.module.gis.toolbar.mapselect.MapSelectMenu;
import com.mapgis.mmt.module.gis.toolbar.maptest.RandomTestDBMap;
import com.mapgis.mmt.module.gis.toolbar.measure.MeasureAreaMapMenu;
import com.mapgis.mmt.module.gis.toolbar.measure.MeasureLengthMapMenu;
import com.mapgis.mmt.module.gis.toolbar.online.query.point.OnlinePointQueryMapMenu;
import com.mapgis.mmt.module.gis.toolbar.online.query.point.vague.OnlineVaguePointQueryMapMenu;
import com.mapgis.mmt.module.gis.toolbar.online.query.spatial.OnlineSpatialQueryMapMenu;
import com.mapgis.mmt.module.gis.toolbar.online.query.spatial.OnlineWhereQueryMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.module.AuxConditionQueryMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.module.CS_AuxConditionQueryMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.point.PointQueryMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.point.vague.VaguePointQueryMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.spatial.SpatialQueryMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.spatial.WhereQueryMapMenu;
import com.mapgis.mmt.module.gis.toolbar.trace.TodayTraceMenu;
import com.mapgis.mmt.module.gps.gpsstate.GPSStateMapMenu;
import com.mapgis.mmt.module.systemsetting.SystemSettingMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.point.PanoramaMapMenu;

import java.util.Hashtable;

public class MapMenuRegistry {

    private static MapMenuRegistry instance;
    private final Hashtable<String, String> registry;

    private MapMenuRegistry() {
        registry = new Hashtable<String, String>();

        registQueryMenu();

        // registry.put("拉框查询", RectQueryMapMenu.class.getName());

        registry.put("地名搜索", AddressSearchMenu.class.getName());

        registry.put("测量距离", MeasureLengthMapMenu.class.getName());
        registry.put("测量面积", MeasureAreaMapMenu.class.getName());

        registry.put("返回主页", BackNavigationMapMenu.class.getName());
        registry.put("更多菜单", ForMoreMapMenu.class.getName());

        // registry.put("放大镜", MagnifierMapMenu.class.getName());
        registry.put("图层控制", LayerControlMapMenu.class.getName());
        registry.put("打开地图", MapSelectMenu.class.getName());

        registry.put("随机地图", RandomTestDBMap.class.getName());

        registry.put("今日轨迹", TodayTraceMenu.class.getName());

        //自来水管道爆管分析
        registry.put("爆管分析", PipeAccidentMenu.class.getName());
        registry.put("爆管分析2", PipeBrokenAnalysisMenu.class.getName());
        //燃气管道爆管分析
        registry.put("燃气爆管分析", BurstAnalysisMapMenu.class.getName());

        registry.put("经纬转平", WGS2PlaneMapMenu.class.getName());
        registry.put("平转经纬", Plane2WGSMapMenu.class.getName());

        registry.put("系统设置", SystemSettingMapMenu.class.getName());

        registry.put("附属数据查询", AuxConditionQueryMapMenu.class.getName());

        registry.put("长沙附属数据查询", CS_AuxConditionQueryMapMenu.class.getName());

        registry.put("错误图形上报", ErrImgReportMapMenu.class.getName());

        registry.put("地图浏览定位", MapScanLocMapMenu.class.getName());

        registry.put("GPS状态", GPSStateMapMenu.class.getName());
        registry.put("全景地图",PanoramaMapMenu.class.getName());
        registry.put("清空缓存",ClearCacheMapMenu.class.getName());
        registry.put("全景地图",PanoramaMapMenu.class.getName());
    }

    public static MapMenuRegistry getInstance() {
        if (instance == null) {
            instance = new MapMenuRegistry();
        }

        return instance;
    }

    /**
     * 注册新导航功能模块
     *
     * @param key   模块名称
     * @param value 模块类名
     */
    public void regist(String key, String value) {
        registry.put(key, value);
    }

    /**
     * 注册新导航功能模块
     *
     * @param key   模块名称
     * @param value 模块类名
     */
    public void regist(String key, Class<?> value) {
        registry.put(key, value.getName());
    }

    public void registQueryMenu() {

        //MobileConfig.MapConfigInstance为null基本可以判定为在线地图
        //离线同步执行MobileConfig.MapConfigInstance不会为null
        //在线地图登陆时异步执行，执行到这里时才有可能为null

        if (MobileConfig.MapConfigInstance == null || !GisQueryUtil.isofflineMap()) {
            registry.put("点击查询", OnlinePointQueryMapMenu.class.getName());
            registry.put("模糊点击", OnlineVaguePointQueryMapMenu.class.getName());
            registry.put("空间查询", OnlineSpatialQueryMapMenu.class.getName());
            registry.put("高级查询", OnlineWhereQueryMapMenu.class.getName());
            // registry.put("附近查询", OnlineNearbyQueryMenu.class.getName());
        } else {
            registry.put("点击查询", PointQueryMapMenu.class.getName());
            registry.put("模糊点击", VaguePointQueryMapMenu.class.getName());
            registry.put("空间查询", SpatialQueryMapMenu.class.getName());
            registry.put("高级查询", WhereQueryMapMenu.class.getName());
            // registry.put("附近查询", NearbyQueryMenu.class.getName());
        }
    }

    public BaseMapMenu getMenuInstance(String menuName, MapGISFrame mapGISFrame) {
        try {
            if (registry.containsKey(menuName)) {
                return (BaseMapMenu) Class.forName(registry.get(menuName)).getConstructor(MapGISFrame.class).newInstance(mapGISFrame);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new EmptyDefinedMapMenu(mapGISFrame);
    }

    public boolean containMenu(String menuName) {
        return registry.containsKey(menuName);
    }
}
