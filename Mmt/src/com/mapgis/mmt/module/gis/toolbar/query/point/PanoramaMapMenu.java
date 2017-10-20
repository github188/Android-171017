package com.mapgis.mmt.module.gis.toolbar.query.point;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;

/**
 * Created by cmios on 2017/3/9.
 */

public class PanoramaMapMenu extends BaseMapMenu {
    public PanoramaListener panoramaListener;


    public PanoramaMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }
        mapGISFrame.showToast("点击地图查看选中的全景地图");
        panoramaListener = new PanoramaListener(mapGISFrame,mapView);
        panoramaListener.setPanoramaAnnotationListener(new PanoramaAnnotationListener());
        mapView.setTapListener(panoramaListener);

        return true;
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.vague_point_query_titlebar, null);
        view.findViewById(R.id.tvPlanBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ((TextView) view.findViewById(R.id.tvPlanName)).setText("全景地图");

        view.findViewById(R.id.tvTaskState).setVisibility(View.GONE);



        return view;

    }
}
