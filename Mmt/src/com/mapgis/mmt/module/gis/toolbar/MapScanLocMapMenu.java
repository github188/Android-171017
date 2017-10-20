package com.mapgis.mmt.module.gis.toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.geometry.Dot;

/**
 * 点击查询
 *
 * @author Administrator
 */
public class MapScanLocMapMenu extends BaseMapMenu {
    TextView titleTV;

    public MapScanLocMapMenu(MapGISFrame mapGISFrame) {

        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }
        if (titleTV == null) {
            return false;
        }

        Dot centerPoint = mapView.getCenterPoint();

        if (centerPoint == null) {
            return false;
        }

        double x = Convert.FormatDouble(centerPoint.x);
        double y = Convert.FormatDouble(centerPoint.y);

        titleTV.setText(String.valueOf(x) + "," + String.valueOf(y));
        return true;
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.mapscanloc_titlebar, null);
        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        titleTV = (TextView) view.findViewById(R.id.baseActionBarTextView);
        titleTV.setText("当前位置");

        return view;
    }
}
