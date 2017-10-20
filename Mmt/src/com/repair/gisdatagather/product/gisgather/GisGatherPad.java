package com.repair.gisdatagather.product.gisgather;

import android.view.MotionEvent;
import android.view.View;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.R;
import com.repair.gisdatagather.common.entity.GISDataProject;
import com.repair.gisdatagather.product.gisopt.GisOptPanel;
import com.repair.gisdatagather.product.layershow.LayerShowPanelPad;
import com.repair.gisdatagather.product.projectopt.ProjectOptPanel;
import com.repair.gisdatagather.product.xyshow.XYShowPad;

/**
 * Created by liuyunfan on 2016/5/5.
 */
public class GisGatherPad extends GisGather {

    public GisGatherPad(MapGISFrame mapGISFrame, View view, GISDataProject gisDataProject, boolean readOnly, boolean isPad, boolean isSelfAdd) {
        super(mapGISFrame, view, gisDataProject, readOnly, isPad, isSelfAdd);
        final View parentView = view.findViewById(R.id.gis_operatePanel_pad);
        final View childView = view.findViewById(R.id.radioGroup);
        parentView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                childView.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        childView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    @Override
    void initGisOpt(View view) {
        if (view == null) {
            return;
        }
        gisOpt = new GisOptPanel(this, view);
        gisOpt.initGisOptPanel();
    }

    @Override
    void initLayerShow(View view) {
        if (view == null) {
            return;
        }
        layerShow = new LayerShowPanelPad(this, view);
        layerShow.initLayerShowPanel();
    }

    @Override
    void initGisProjectOpt(View view) {
        if (view == null) {
            return;
        }
        projectOpt = new ProjectOptPanel(this, view);
        projectOpt.initProjectOptPanel();
    }

    @Override
    void initxyShow(View view) {
        if (view == null) {
            return;
        }
        xyShow = new XYShowPad(this, view);
        xyShow.initXYShow();
    }

    @Override
    public void initMapFrame(boolean isReadOnly) {

        super.initMapFrame(isReadOnly);

        view.setVisibility(View.VISIBLE);
        mapGISFrame.findViewById(R.id.gisgatherPanel_mobile).setVisibility(View.GONE);
    }

    @Override
    public void initGisGather() {

        initMapFrame(isReadOnly);
        if (isReadOnly) {
            return;
        }

        initGisOpt(view.findViewById(R.id.gisgather_optbtnPanel_pad));

        initGisProjectOpt(view.findViewById(R.id.pojOpt_pad));
        initxyShow(view.findViewById(R.id.xyInfo_pad));
        initLayerShow(view.findViewById(R.id.layerInfo_pad));
        initTargetView();
    }
}
