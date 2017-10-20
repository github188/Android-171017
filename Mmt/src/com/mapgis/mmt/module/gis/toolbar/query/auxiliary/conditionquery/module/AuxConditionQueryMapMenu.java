package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.module;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.repair.auxiliary.conditionquery.module.ConditionPanelActivity;

/**
 * Created by liuyunfan on 2016/4/14.
 */
public class AuxConditionQueryMapMenu extends BaseMapMenu {
    public AuxConditionQueryMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);
        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("附属数据查询");
        view.findViewById(R.id.baseActionBarImageView).setVisibility(View.GONE);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.showToast(mapGISFrame.getResources().getString(R.string.mapmenu_error));
            return false;
        }
        try {
            AuxUtils.openAuxQuery(mapGISFrame, new AuxUtils.AfterOnsucess() {
                @Override
                public void afterSucess() {
                    Intent intent = new Intent(mapGISFrame, ConditionPanelActivity.class);
                    intent.putExtra("Envelope",mapView.getDispRange().toString());
                    mapGISFrame.startActivity(intent);
                }
            });
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
