package com.mapgis.mmt.module.gis.toolbar.errimgreport;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.repair.errimgreport.ErrImgReportActivity;

/**
 * Created by liuyunfan on 2016/7/7.
 */
public class ErrImgReportMapMenu extends BaseMapMenu {
    public ErrImgReportMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);
        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("错误图形上报");
        view.findViewById(R.id.baseActionBarImageView).setVisibility(View.INVISIBLE);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.showToast(mapGISFrame.getResources().getString(R.string.mapmenu_error));
            return false;
        }

        Intent intent = new Intent(mapGISFrame, ErrImgReportActivity.class);
        mapGISFrame.startActivity(intent);

        return true;
    }
}