package com.mapgis.mmt.module.gis.toolbar;

import android.view.View;

import com.mapgis.mmt.module.gis.MapGISFrame;

public class EmptyDefinedMapMenu extends BaseMapMenu {

	public EmptyDefinedMapMenu(MapGISFrame mapGISFrame) {
		super(mapGISFrame);
	}

	@Override
	public boolean onOptionsItemSelected() {
		return false;
	}

	@Override
	public View initTitleView() {
		return null;
	}

}
