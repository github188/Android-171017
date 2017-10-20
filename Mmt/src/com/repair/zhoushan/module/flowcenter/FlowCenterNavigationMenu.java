package com.repair.zhoushan.module.flowcenter;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.repair.zhoushan.module.eventreport.ReportEventTask;

/**
 * 流程中心
 */
public class FlowCenterNavigationMenu extends BaseNavigationMenu {

    public FlowCenterNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        final String eventType = item.Function.getModuleParamValue("事件类型");
        final String eventName = item.Function.getModuleParamValue("事件名称");

        // 禁用上报界面的缓存功能，目前仅从 “App主导航界面”->“流程中心”->“事件上报” 才会缓存未上报的数据
        // 值为"是"或"否"
        String disableCache = item.Function.getModuleParamValue("禁用缓存");

        ReportEventTask task = new ReportEventTask(navigationActivity, eventType, eventName);
        task.setFromNavigationMenu(!"是".equals(disableCache));
        task.mmtExecute();
    }
}
