package com.repair.gisdatagather.attrsedit;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.repair.gisdatagather.enn.GISDataGatherActivity;

/**
 * Created by liuyunfan on 2016/6/13.
 */
public class GisAttrsEditNavigationMenu extends BaseNavigationMenu {
    public GisAttrsEditNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, GISDataGatherActivity.class);
        intent.putExtra("isOnlyEdit", true);
        intent.putExtra("title",item.Function.Alias);
        navigationActivity.startActivity(intent);
    }

}
