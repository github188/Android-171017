package com.maintainproduct.module.casehandover;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 案件移交
 */
public class CasehandoverNavigationMenu extends BaseNavigationMenu {

    public CasehandoverNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, CasehandoverListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        navigationActivity.startActivityForResult(intent, 3);
    }

    @Override
    public int[] getIcons() {
       return getIcons("已办");
    }
}
