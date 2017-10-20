package com.mapgis.mmt.constant;

import com.mapgis.mmt.module.flowreport.FlowReportNavigationMenu;
import com.mapgis.mmt.module.flowreport.history.FlowReportHistoryNavigationMenu;
import com.mapgis.mmt.module.gatherdata.GatherDataNavigationMenu;
import com.mapgis.mmt.module.gis.MapBrowseNavigationMenu;
import com.mapgis.mmt.module.gps.gpsstate.GpsStateNavigationMenu;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.EmptyDefinedNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.mapgis.mmt.module.relevance.RelevanceMediaNavigationMenu;
import com.mapgis.mmt.module.shortmessage.ShortMessageNavigationMenu;
import com.mapgis.mmt.module.systemsetting.SystemSettingNavigationMenu;
import com.mapgis.mmt.module.taskcontrol.TaskControlNavigationMenu;

import java.util.Hashtable;

public class NavigationMenuRegistry {

    private static NavigationMenuRegistry instance;
    private final Hashtable<String, String> registry;

    private NavigationMenuRegistry() {
        registry = new Hashtable<>();

        registry.put("即时信息", ShortMessageNavigationMenu.class.getName());
        registry.put("临时事件", FlowReportNavigationMenu.class.getName());
        registry.put("地图浏览", MapBrowseNavigationMenu.class.getName());
        registry.put("历史事件", FlowReportHistoryNavigationMenu.class.getName());
        registry.put("GPS状态", GpsStateNavigationMenu.class.getName());
        registry.put("系统设置", SystemSettingNavigationMenu.class.getName());

        registry.put("设备拍照", RelevanceMediaNavigationMenu.class.getName());
        registry.put("任务监控", TaskControlNavigationMenu.class.getName());

        registry.put("管网普查", GatherDataNavigationMenu.class.getName());
    }

    public static NavigationMenuRegistry getInstance() {
        if (instance == null) {
            instance = new NavigationMenuRegistry();
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
        regist(key, value, "main_menu_my_plan");
    }

    /**
     * 注册新导航功能模块
     *
     * @param key   模块名称
     * @param value 模块类名
     */
    public void regist(String key, Class<?> value) {
        regist(key, value.getName());
    }

    public void regist(String key, String value, String... icon) {
        registry.put(key, value);
    }

    public BaseNavigationMenu getMenuInstance(NavigationActivity navigationActivity, NavigationItem item) {
        try {

            String menuName = item.Function.Name;

            if (registry.containsKey(menuName)) {

                return (BaseNavigationMenu) Class.forName(registry.get(menuName))
                        .getConstructor(NavigationActivity.class, NavigationItem.class).newInstance(navigationActivity, item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new EmptyDefinedNavigationMenu(navigationActivity, item);
    }

    public boolean containMenu(String name) {
        return registry.containsKey(name);
    }
}
