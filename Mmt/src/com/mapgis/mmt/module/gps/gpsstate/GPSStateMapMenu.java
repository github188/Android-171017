package com.mapgis.mmt.module.gps.gpsstate;

import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;

public class GPSStateMapMenu extends BaseMapMenu {
    public GPSStateMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        Intent intent = new Intent(mapGISFrame, GpsStateActivity.class);

        mapGISFrame.startActivityForResult(intent, 0);

        return true;
    }

    @Override
    public View initTitleView() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
