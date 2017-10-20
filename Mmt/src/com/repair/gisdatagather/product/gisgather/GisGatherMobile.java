package com.repair.gisdatagather.product.gisgather;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.repair.gisdatagather.common.entity.GISDataProject;
import com.repair.gisdatagather.product.gisopt.GisOptPanelMobile;
import com.repair.gisdatagather.product.layershow.LayerShowPanelMobile;
import com.repair.gisdatagather.product.projectopt.ProjectOptPanel;
import com.repair.gisdatagather.product.xyshow.XYShowMobile;

/**
 * Created by liuyunfan on 2016/5/5.
 */
public class GisGatherMobile extends GisGather {

    public GisGatherMobile(final MapGISFrame mapGISFrame, final View view, GISDataProject gisDataProject, boolean readOnly, boolean isPad, boolean isSelfAdd) {
        super(mapGISFrame, view, gisDataProject, readOnly, isPad, isSelfAdd);
    }

    @Override
    void initGisOpt(View view) {
        if (view == null) {
            return;
        }

        gisOpt = new GisOptPanelMobile(this, view);
        gisOpt.initGisOptPanel();
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
        xyShow = new XYShowMobile(this, view);
        xyShow.initXYShow();
    }

    @Override
    void initLayerShow(View view) {
        if (view == null) {
            return;
        }
        layerShow = new LayerShowPanelMobile(this, view);
        layerShow.initLayerShowPanel();
    }

    @Override
    public void initMapFrame(boolean isReadOnly) {
        super.initMapFrame(isReadOnly);

        view.setVisibility(View.VISIBLE);
        mapGISFrame.findViewById(R.id.gisgatherPanel_pad).setVisibility(View.GONE);

        mapGISFrame.getBaseLeftImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mapGISFrame.findViewById(R.id.mapviewClear)
                        .performClick();
                restroeMapFrame();
                mapGISFrame.backByReorder(isSelfAdd);
            }
        });
    }

    @Override
    public void initGisGather() {

        initMapFrame(isReadOnly);

        if (isReadOnly) {
            return;
        }

        initGisOpt(view.findViewById(R.id.gisgather_bottomopt_btn_mobile_panel));

        View optview = mapGISFrame.getLayoutInflater().inflate(R.layout.gisdataoperatepanel_mobile, null);

        initGisProjectOpt(optview);

        initxyShow(view.findViewById(R.id.xyInfo_mobile));

        initLayerShow(optview);

        initTargetView();


        mapGISFrame.setCustomView(getHead());

    }

    @Override
    public void initTargetView() {

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                DimenTool.dip2px(mapGISFrame, 30), DimenTool.dip2px(
                mapGISFrame, 30));
//        int top = DimenTool.dip2px(
//                mapGISFrame, 100);
//        layoutParams.setMargins(0, top, 0, 0);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        ImageView targetView = new ImageView(mapGISFrame);
        targetView.setLayoutParams(layoutParams);
        targetView.setImageResource(R.drawable.mapview_gather_point);
        targetView.setTag("MapViewScreenView");
        mapView.addView(targetView);

    }

    private View getHead() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.gisgather_mobile_head, null);

        TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        txtTitle.setMaxWidth(400);
        txtTitle.setEllipsize(TextUtils.TruncateAt.END);
        txtTitle.setText(gisDataProject.ProjectName);
        txtTitle.getPaint().setTypeface(Typeface.DEFAULT);

        LinearLayout txtRTKHLayout = (LinearLayout) view.findViewById(R.id.rtkDeviceHLayout);
        final TextView txtRTKH = (TextView) txtRTKHLayout.findViewById(R.id.txtRTKH);
        txtRTKHLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (xyShow == null) {
                    return;
                }
                xyShow.deviceSet(txtRTKH);
            }
        });
        if (xyShow != null) {
            xyShow.initDeviceHView(txtRTKH);
        }
        // 返回按钮
        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapGISFrame.findViewById(R.id.mapviewClear)
                        .performClick();
                restroeMapFrame();
                mapGISFrame.backByReorder(isSelfAdd);
            }
        });

        view.setBackgroundResource(AppStyle.getActionBarStyleResource());
        return view;
    }
}
