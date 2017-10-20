package com.patrolproduct.module.nearbyquery;

import android.content.Intent;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * Created by Comclay on 2016/11/3.
 * 附近查询模块：主要是根据图层来查询用户当前位置周边的设备信息
 *      需要在模块名称后加上默认显示搜索的图层名称
 */

public class NearbyQueryMenu extends BaseNavigationMenu {
    public NearbyQueryMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity,NearbyQueryActivity.class);
        // 默认查询的图层
        intent.putExtra("layerNames",this.item.Function.ModuleParam);
        navigationActivity.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(navigationActivity);
    }
}
