package com.mapgis.mmt.module.gis.toolbar.query.point;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;

/**
 * 拉框查询
 * 
 * @author Administrator
 * 
 */
public class RectQueryMapMenu extends PointQueryMapMenu {
	public RectQueryMapMenu(MapGISFrame mapGISFrame) {
		super(mapGISFrame);
	}

	@Override
	public boolean onOptionsItemSelected() {
		if (mapView == null || mapView.getMap() == null) {
			mapGISFrame.showToast(mapGISFrame.getResources().getString(R.string.mapmenu_error));
			return false;
		}

		mapGISFrame.setTitleAndClear("正在进行 拉框查询");

		PointQueryListener listener = new PointQueryListener(mapGISFrame, mapView, null);
		mapView.setTapListener(listener);

		return true;
	}
}
