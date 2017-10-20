package com.project.enn.navigation;

import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;


public class ENNBackNavigationMapMenu extends BaseMapMenu {
    public ENNBackNavigationMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        Intent intent = new Intent(mapGISFrame, EnnNavigationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        mapGISFrame.startActivity(intent);

        return true;
    }

    @Override
    public View initTitleView() {
        return null;
    }
}
