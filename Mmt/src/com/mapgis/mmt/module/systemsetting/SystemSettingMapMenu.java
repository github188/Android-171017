package com.mapgis.mmt.module.systemsetting;

import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;

public class SystemSettingMapMenu extends BaseMapMenu {
    public SystemSettingMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        Intent intent = new Intent(mapGISFrame, SystemSettingActivity.class);

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
