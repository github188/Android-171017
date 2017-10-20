package com.mapgis.mmt.module.gis.toolbar;

import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.module.gis.MapGISFrame;

/**
 * 返回九宫格主界面
 * 
 * @author Administrator
 * 
 */
public class BackNavigationMapMenu extends BaseMapMenu {

	public BackNavigationMapMenu(MapGISFrame mapGISFrame) {
		super(mapGISFrame);
	}

	@Override
	public boolean onOptionsItemSelected() {
		// if (AppManager.getActivityListCount() == 1) {
		// Intent intent = new Intent(mapGISFrame, NavigationActivity.class);
		// mapGISFrame.startActivity(intent);
		// } else {
		// AppManager.finishToNavigation(mapGISFrame);
		// mapGISFrame.getBaseLeftImageView().setVisibility(View.GONE);
		// mapGISFrame.getBaseRightImageView().setVisibility(View.GONE);
		// }

		Class<?> navigationActivityClass = ActivityClassRegistry.getInstance().getActivityClass("主界面");
		Intent intent = new Intent(mapGISFrame, navigationActivityClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		mapGISFrame.startActivity(intent);

		return true;
	}

	@Override
	public View initTitleView() {
		return null;
	}
}
