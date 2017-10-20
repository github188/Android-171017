package com.mapgis.mmt.module.gis.toolbar.magnifier;

import android.view.View;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.zondy.mapgis.android.mapview.MagnifierOption;

public class MagnifierMapMenu extends BaseMapMenu {

	public MagnifierMapMenu(MapGISFrame mapGISFrame) {
		super(mapGISFrame);
	}

	@Override
	public boolean onOptionsItemSelected() {
		mapView.turnOnMagnifier(new MagnifierOption());
		return true;
	}

	@Override
	public View initTitleView() {
		return null;
	}
}
