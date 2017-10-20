package com.mapgis.mmt.module.gis.toolbar.query.point;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;

/**
 * 点击查询
 *
 * @author Administrator
 */
public class PointQueryMapMenu extends BaseMapMenu {

    public PointQueryListener pointQueryListener;
    public ImageView queryResultListImg;

    public PointQueryMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }

        pointQueryListener = new PointQueryListener(mapGISFrame, mapView, queryResultListImg);
        pointQueryListener.setPointQueryAnnotationListener(new MmtAnnotationListener());
        mapView.setTapListener(pointQueryListener);

        mapGISFrame.showToast("点击设备点进行查询");

        return true;
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.vague_point_query_titlebar, null);
        view.findViewById(R.id.tvPlanBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ((TextView) view.findViewById(R.id.tvPlanName)).setText("点击查询");

        view.findViewById(R.id.tvTaskState).setVisibility(View.GONE);

        queryResultListImg = (ImageView) view.findViewById(R.id.ivPlanDetail);
        queryResultListImg.setVisibility(View.INVISIBLE);

        view.findViewById(R.id.ivPlanDetail).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pointQueryListener.showResultListFragment();
            }
        });

        return view;
    }
}
