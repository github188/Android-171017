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

public class WGS2PlaneMapMenu extends BaseMapMenu {

    public WGS2PlaneMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        String text = MyApplication.getInstance().getConfigValue("WGS2Plane");

        if (!BaseClassUtil.isNullOrEmptyString(text)) {
            converse(text);
        } else {
            SettingsInputDialog fragment = new SettingsInputDialog("待转换WGS84坐标", "mySQL");
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

        Location location = new Location("");

        location.setLongitude(Double.parseDouble(xy[0]));
        location.setLatitude(Double.parseDouble(xy[1]));

        GpsXYZ xyz = GpsReceiver.getInstance().getLastLocation(location);

        Log.v("WGS2Plane", xyz.toString());

        MyApplication.getInstance().showMessageWithHandle(xyz.toString());
    }

    @Override
    public View initTitleView() {
        return null;
    }

}
