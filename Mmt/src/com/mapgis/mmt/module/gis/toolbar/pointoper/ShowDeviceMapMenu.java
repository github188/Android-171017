package com.mapgis.mmt.module.gis.toolbar.pointoper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.map.MapLayer;

import java.util.List;

/**
 * Created by liuyunfan on 2016/6/23.
 */
public class ShowDeviceMapMenu extends ShowPointMapMenu {

    public ShowDeviceMapMenu(MapGISFrame mapGISFrame, Context context, String loc, String title,
                             String text, int pos) {
        super(mapGISFrame, context, loc, title,
                text, pos);
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);

        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("设备选择");

        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity(false);
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected() {
        super.onOptionsItemSelected();

        onDeviceQuery();

        return true;
    }

    public void onDeviceQuery() {
        List<MapLayer> visibleVectorLayer = GisUtil.getPointQueryVectorLayer(mapView);
        if (visibleVectorLayer == null) {
            return;
        }
        Graphic graphic;
        for (int i = 0; i < visibleVectorLayer.size(); i++) {

            MapLayer mapLayer = visibleVectorLayer.get(i);

            graphic = GisUtil.pointQuerySingle(mapView, mapView.getCenterPoint(), mapLayer);
            mapView.setAnnotationListener(null);
            if (graphic != null) {

                mapView.setAnnotationListener(new MmtAnnotationListenerDialog());

                handler.setLayerName( mapLayer.getName());
                handler.obtainMessage(3, graphic).sendToTarget();

                break;
            }
        }
    }
}
