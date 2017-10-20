package com.mapgis.mmt.module.gis.toolbar.gps;

import android.location.Location;
import android.util.Log;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.SettingsInputDialog;
import com.mapgis.mmt.common.widget.SettingsInputDialog.OnOkClickListener;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

public class Plane2WGSMapMenu extends BaseMapMenu {

    public Plane2WGSMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        String text = MyApplication.getInstance().getConfigValue("Plane2WGS");

        if (!BaseClassUtil.isNullOrEmptyString(text)) {
            converse(text);
        } else {
            SettingsInputDialog fragment = new SettingsInputDialog("待转换平面坐标", "mySQL");
            fragment.show(this.mapGISFrame.getSupportFragmentManager(), "1");

            fragment.setOnOkClickListener(new OnOkClickListener() {
                @Override
                public void onOkClick(View view, String text) {
                    converse(text);
                }
            });
        }

        return true;
    }

    private void converse(String text) {
        String[] xy = text.split(",");

        double x = Double.parseDouble(xy[0]), y = Double.parseDouble(xy[1]);

        Location location = GpsReceiver.getInstance().getLastLocationConverse(new GpsXYZ(x, y));

        Log.v("Plane2WGS", location.getLongitude() + "," + location.getLatitude());

        MyApplication.getInstance().showMessageWithHandle(location.getLongitude() + "," + location.getLatitude());
    }

    @Override
    public View initTitleView() {
        return null;
    }

}
