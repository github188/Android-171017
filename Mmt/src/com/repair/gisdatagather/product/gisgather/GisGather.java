package com.repair.gisdatagather.product.gisgather;

import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.mapgis.mmt.R;
import com.repair.gisdatagather.enn.bean.GISDeviceSetBean;
import com.repair.gisdatagather.common.entity.GISDataBeanBase;
import com.repair.gisdatagather.common.entity.GISDataProject;
import com.repair.gisdatagather.common.entity.TextDot;
import com.repair.gisdatagather.product.gisopt.GisOptPanel;
import com.repair.gisdatagather.product.gisopt.GisOptPanelMobile;
import com.repair.gisdatagather.product.layershow.LayerShowPanel;
import com.repair.gisdatagather.product.projectopt.ProjectOptPanel;
import com.repair.gisdatagather.product.xyshow.XYShow;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuyunfan on 2016/5/5.
 */
public abstract class GisGather {
    //整个采集面板
    protected View view;

    public boolean isReadOnly = false;
    public boolean isAutoLinkLine = false;
    public boolean isPad = true;
    public GisOptPanel gisOpt;
    public ProjectOptPanel projectOpt;
    public LayerShowPanel layerShow;
    public XYShow xyShow;

    public MapGISFrame mapGISFrame;
    public MmtMapView mapView;
    //当前采集的gis数据
    public static GISDataProject gisDataProject;

    //图层默认的属性
    public static HashMap<String, GISDataBeanBase> layerDefaultAttrs = new HashMap<>();
    //靶心dot
    public Dot dot = new Dot();
//    public TextView xEditText;
//    public TextView yEditText;

    //配置的可编辑的图层信息列表
    public List<GISDeviceSetBean> gisDeviceSetBeans = new ArrayList<>();
    //当前选中的可编辑的图层
    public GISDeviceSetBean hasChoseGISDeviceSetBean = new GISDeviceSetBean();
    //连线时捕获的点
    public List<TextDot> hasCatchTextDots = new ArrayList<>();

    public RadioGroup rg;

    public boolean isSelfAdd = false;

    public boolean isShowCurrentLocRun = false;

    public GisGather(MapGISFrame mapGISFrame, View view, GISDataProject gisDataProject, boolean isReadOnly, boolean isPad, boolean isSelfAdd) {
        this.mapGISFrame = mapGISFrame;
        this.view = view;
        this.mapView = (MmtMapView) mapGISFrame.getMapView();
        GisGather.gisDataProject = gisDataProject;
        this.isReadOnly = isReadOnly;
        this.isPad = isPad;
        this.isSelfAdd = isSelfAdd;
    }

    //gis操作，新增管点，连线等
    abstract void initGisOpt(View view);

    //图层信息和xy坐标展示 及相关事件
    abstract void initLayerShow(View view);

    //xy和精度展示 及相关操作
    abstract void initxyShow(View view);

    //工程相关的按钮，删除工程，提交工程等
    abstract void initGisProjectOpt(View view);

    public void initTargetView() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                DimenTool.dip2px(mapGISFrame, 30), DimenTool.dip2px(
                mapGISFrame, 30));
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        ImageView targetView = new ImageView(mapGISFrame);
        targetView.setLayoutParams(layoutParams);
        targetView.setImageResource(R.drawable.mapview_gather_point);
        targetView.setTag("MapViewScreenView");
        mapView.addView(targetView);
    }

    public void initMapFrame(boolean isReadOnly) {

        mapGISFrame.findViewById(R.id.mapviewClear).performClick();
        mapGISFrame.findViewById(R.id.rightDrawer).setVisibility(View.GONE);
        mapGISFrame.getBaseLeftImageView().setVisibility(View.VISIBLE);
        mapGISFrame.findViewById(R.id.include).setVisibility(View.GONE);
        mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.GONE);
        mapGISFrame.findViewById(R.id.toolFrame).setVisibility(View.GONE);

        mapGISFrame.getBaseLeftImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapGISFrame.findViewById(R.id.mapviewClear)
                        .performClick();
                restroeMapFrame();
                mapGISFrame.backByReorder(isSelfAdd);
            }
        });
        mapGISFrame.setTitleAndClear(gisDataProject.ProjectName);

        if (isReadOnly) {
            return;
        }

        View gisGathertoolFrameFroProduct = mapGISFrame.findViewById(R.id.gisGathertoolFrameFroProduct);
        gisGathertoolFrameFroProduct.setVisibility(View.VISIBLE);

        view.findViewById(R.id.gis_imageview_main_zoomin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.zoomIn(true);
            }
        });

        view.findViewById(R.id.gis_imageview_main_zoomout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.zoomOut(true);
            }
        });

        /** *－－－地图复位按钮－－－ * **/
        view.findViewById(R.id.gis_imageview_main_zoomfull).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.zoomFull();
            }
        });

    }

    public void restroeMapFrame() {
        mapGISFrame.findViewById(R.id.rightDrawer).setVisibility(View.VISIBLE);
        mapGISFrame.findViewById(R.id.toolFrame).setVisibility(View.VISIBLE);
        mapGISFrame.findViewById(R.id.gisGathertoolFrameFroProduct).setVisibility(View.GONE);
        mapGISFrame.findViewById(R.id.include).setVisibility(View.VISIBLE);
        mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.VISIBLE);

        if (gisOpt instanceof GisOptPanelMobile) {
            PopupWindow popupWindow = ((GisOptPanelMobile) gisOpt).popupWindow;
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        }

        List<TextDot> textDots = gisDataProject.getTextDots();
        textDots.addAll(gisDataProject.getTodayGISData().textDots);

        for (TextDot textDot : textDots) {
            if (!textDot.isGlint) {
                continue;
            }
            textDot.isGlint = false;
        }

        //关闭实时展示xy，精度，状态的线程
        isShowCurrentLocRun = false;
    }

    public abstract void initGisGather();
}
