package com.mapgis.mmt.module.gis.toolbar.gps;

import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.LocationTimerCallback;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

/**
 * 我的位置
 *
 * @author Administrator
 */
public class MyLocationMapMenu extends BaseMapMenu {

    public MyLocationMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();
        LocationTimerCallback locationTimerCallback = new LocationTimerCallback(xyz, true);
        MyApplication.getInstance().sendToBaseMapHandle(locationTimerCallback);

        Intent intent = mapGISFrame.getIntent();
        if (intent == null || xyz == null) {
            return true;
        }
        if (!intent.hasExtra("fromMapScan")) {
            return true;
        }
        if (intent.getBooleanExtra("fromMapScan", false)) {

            mapGISFrame.getBaseTextView().setText(
                    Convert.FormatDouble(xyz.getX(), ".##") + ","
                            + Convert.FormatDouble(xyz.getY(), ".##"));
        }
        return true;
    }

    @Override
    public View initTitleView() {
        return null;
    }
}
