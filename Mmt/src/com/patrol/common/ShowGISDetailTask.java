package com.patrol.common;

import android.content.Context;
import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.patrol.entity.KeyPoint;
import com.patrol.module.KeyPoint.PointDetailFragment;
import com.mapgis.mmt.global.MmtBaseTask;
import com.zondy.mapgis.android.mapview.MapView;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ShowGISDetailTask extends MmtBaseTask<KeyPoint, Integer, HashMap> {
    KeyPoint kp;
    String[] names;

    public ShowGISDetailTask(Context context) {
        super(context);
    }

    @Override
    protected HashMap doInBackground(KeyPoint... params) {
        try {
            kp = params[0];

            MapView mapView;

            if (context instanceof MapGISFrame)
                mapView = ((MapGISFrame) context).getMapView();
            else
                mapView = MyApplication.getInstance().mapGISFrame.getMapView();

            LinkedHashMap<String, String> attr = GisUtil.offlinePipeQuery(mapView, kp.GisLayer, kp.FieldName, kp.FieldValue);

            names = GisUtil.getGISFields(kp.GisLayer);

            return attr;
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }

    @Override
    protected void onSuccess(HashMap attr) {
        try {
            super.onSuccess(attr);

            if (attr == null)
                return;

            PointDetailFragment fragment = new PointDetailFragment();

            Bundle args = new Bundle();

            args.putParcelable("kp", kp);
            args.putSerializable("attr", attr);
            args.putStringArray("names", names);

            fragment.setArguments(args);

            fragment.show(((BaseActivity) context).getSupportFragmentManager(), "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
