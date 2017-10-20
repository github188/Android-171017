package com.mapgis.mmt.module.gis.toolbar;

import android.view.View;

import com.mapgis.mmt.module.gis.MapGISFrame;

public class ForMoreMapMenu extends BaseMapMenu {

	public ForMoreMapMenu(MapGISFrame mapGISFrame) {
		super(mapGISFrame);
	}

	@Override
	public boolean onOptionsItemSelected() {
		mapGISFrame.getFragment().showDrawer();
		return true;
	}

	@Override
	public View initTitleView() {
		return null;
	}

}
