package com.repair.mycase;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 工单任务
 */
public class MyCaseNavigationMenu extends BaseNavigationMenu {

	public MyCaseNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
		super(navigationActivity, item);
	}

	@Override
	public void onItemSelected() {
		Intent intent = new Intent(navigationActivity, MyCaseActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra("alias", item.Function.Alias);
		navigationActivity.startActivity(intent);
	}

	@Override
	public int[] getIcons() {
		return getIcons("在办");
	}
}
