package com.repair.zhoushan.module.projectmanage.projectsitereport;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 工程现场上报
 */
public class ProjectSiteReportNavigationMenu extends BaseNavigationMenu {

    public ProjectSiteReportNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {

        Intent intent = new Intent(navigationActivity, ProjectSiteReportListActivity.class);
        intent.putExtra("Title", item.Function.Alias);
        navigationActivity.startActivity(intent);
    }
}
