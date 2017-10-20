package com.repair.gisdatagather.product.gisopt;

import android.graphics.PointF;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.repair.gisdatagather.product.gisgather.GisGather;
import com.zondy.mapgis.android.mapview.MapView;

/**
 * Created by liuyunfan on 2016/5/4.
 */
public class GisOptPanelMobile extends GisOptPanel implements GisOptInterface, View.OnClickListener {

    public GisOptPanelMobile(GisGather gisGather, View view) {
        super(gisGather, view);
    }

    public void initGisOptPanel() {
        targetAddDot = view.findViewById(R.id.targetAddDot);
        targetAddDot.setOnClickListener(this);
        addLine = view.findViewById(R.id.addLine);
        addLine.setOnClickListener(this);
        editGraphic = view.findViewById(R.id.editGraphic);
        editGraphic.setOnClickListener(this);
        deleteGraphic = view.findViewById(R.id.deleteGraphic);
        deleteGraphic.setOnClickListener(this);

        view.findViewById(R.id.currentDot).setOnClickListener(this);
        view.findViewById(R.id.moreBtn).setOnClickListener(this);

        gisGather.mapView.setTapListener(new MapView.MapViewTapListener() {
            @Override
            public void mapViewTap(PointF pointF) {
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (gisGather.mapView == null || gisGather.mapView.getMap() == null) {
            gisGather.mapGISFrame.stopMenuFunction();
            return;
        }

        gisGather.mapView.setTapListener(null);
        String text = ((TextView) v).getText().toString();
        switch (text) {
            case "添点": {
                addDot2MapView();
            }
            break;
            case "连线": {
                addLine2MapView();
            }
            break;
            case "编辑": {
                editGraphic();
            }
            break;
            case "删除": {
                deleteGraphic();
            }
            break;
            case "定位": {
                gisGather.xyShow.paintoDotByDevice();
            }
            break;
            case "更多": {
                openLayerPanel();
            }
            default: {
                // Toast.makeText(gisGather.mapGISFrame, "未知异常", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public PopupWindow popupWindow;

    private void openLayerPanel() {

        int popW = DimenTool.dip2px(
                gisGather.mapGISFrame, 90);
        if (popupWindow == null) {
            View bottombtn = gisGather.mapGISFrame.findViewById(R.id.gisgather_bottomopt_btn_mobile_panel);
            ViewGroup.LayoutParams layoutParams = bottombtn.getLayoutParams();
            int marginpx = 20;
            if (layoutParams instanceof LinearLayout.LayoutParams) {
                marginpx = ((LinearLayout.LayoutParams) layoutParams).bottomMargin;
            }
            if (layoutParams instanceof RelativeLayout.LayoutParams) {
                marginpx = ((RelativeLayout.LayoutParams) layoutParams).bottomMargin;
            }
            int contentH = gisGather.mapView.getHeight() - bottombtn.getHeight() - marginpx-50;
            popupWindow = new PopupWindow(gisGather.layerShow.view, popW, contentH, false);
            popupWindow.setAnimationStyle(R.style.PopupWindowRight2LeftAnimation);
        }

        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            popupWindow.showAtLocation(gisGather.mapView, Gravity.RIGHT, 0, 0);
        }
    }

}
