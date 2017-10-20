package com.repair.shaoxin.water.highrisesearch;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.constant.RequestCode;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.R;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.mapview.MapView;

public class HighRiseCloseVavleMenu extends BaseMapMenu {
    public HighRiseCloseVavleMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    /**
     * 退出高层关阀功能
     */
    private void quitFunction() {
        HighRiseCloseValveConstant.isOn = false;
        HighRiseCloseValveConstant.MeterNo = null;
        HighRiseCloseValveConstant.queryValve.clear();
        HighRiseCloseValveConstant.queryUser.clear();
        mapView.setTapListener(null);
    }

    /**
     * 初始化功能列表
     */
    private List<String> initFunctionList() {
        List<String> list = new ArrayList<String>();

        if (HighRiseCloseValveConstant.queryValve.size() > 0) {
            list.add("显示阀门");
        }
        if (HighRiseCloseValveConstant.queryUser.size() > 0) {
            list.add("显示用户");
        }
        list.add("上报本次记录");
        list.add("退出高层关阀");

        return list;
    }
    @Override
    public boolean onOptionsItemSelected() {
        if(mapView == null){
            mapGISFrame.showToast("没有加载地图无法使用该功能");
        }
        HighRiseCloseValveConstant.isOn = true;

//        PatrolEquipmentsTask.searchLayerNameAndId();

//        mapView.setTapListener(new MapView.MapViewTapListener() {
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void mapViewTap(PointF pointF) {
//
////                GraphicsSource graphicsSource = mapView.getGraphicsSource();
//                GraphicLayer graphicsSource = mapView.getGraphicLayer();
//                graphicsSource.
//
//                Graphic[] graphics = graphicsSource.getGraphics((int) pointF.x, (int) pointF.y, 30);
//                if (graphics != null && graphics.length > 0) {
//                    SimpleBubble simpleBubble = new SimpleBubble(mapGISFrame, mapView, BubbleType.ESMapResponseGraphic,
//                            GeometryUtil.GetGeometryCenter(graphics[0].getGeometry()), graphics[0]);
//                    simpleBubble.Show();
//                } else {
//                    if (mapView.getCallout() != null) {
//                        mapView.getCallout().hide();
//
//                    }
//                }
//            }
//        });
//
//        Intent intent = new Intent(mapGISFrame, HRCVInputTextDialog.class);
//        mapGISFrame.startActivityForResult(intent, RequestCode.HIGH_RISE_CLOSE_VALVE_CODE_SX);

        HRCVInputTextDialogFragment dialogFragment = HRCVInputTextDialogFragment.newInstance();
        dialogFragment.show(mapGISFrame.getSupportFragmentManager(), "2");
        return true;
    }

    /**
     * 初始化标题栏
     * @return
     */
    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(com.mapgis.mmt.R.layout.vague_point_query_titlebar, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 退出功能按钮
        view.findViewById(com.mapgis.mmt.R.id.tvPlanBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                mapGISFrame.resetMenuFunction();
                mapGISFrame.onBackPressed();
            }
        });

        ((TextView) view.findViewById(com.mapgis.mmt.R.id.tvPlanName)).setText("高层查询");
        view.findViewById(com.mapgis.mmt.R.id.tvTaskState).setVisibility(View.GONE);
        // 高层关阀功能
        ImageView imageView = (ImageView) view.findViewById(com.mapgis.mmt.R.id.ivPlanDetail);
        imageView.setVisibility(View.VISIBLE);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moreFunction();
            }
        });

        // 隐藏底部地图的工具栏
        mapGISFrame.findViewById(com.mapgis.mmt.R.id.layoutMapToolbar).setVisibility(View.GONE);

        return view;
    }

    /**
     * 高层关阀功能列表
     */
    private void moreFunction() {
        final List<String> functions = initFunctionList();

        AlertDialog.Builder builder = new AlertDialog.Builder(mapGISFrame);
        builder.setTitle("高层关阀");
        builder.setItems(functions.toArray(new String[functions.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String function = functions.get(which);
                if (function.equals("显示用户")) {
                    HighRiseCloseValveConstant.showQueryUser().show();
                } else if (function.equals("显示阀门")) {
                    HighRiseCloseValveConstant.showQueryValve();
                } else if (function.equals("显示关闭阀门受影响用户")) {
                    // HighRiseCloseValveConstant.showQueryEffectUser();
                } else if (function.equals("上报本次记录")) {

                    if (HighRiseCloseValveConstant.queryUser.size() == 0) {
                        Toast.makeText(mapGISFrame, "未查询用户信息", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (HighRiseCloseValveConstant.queryValve.size() == 0) {
                        Toast.makeText(mapGISFrame, "未查询用户阀门信息", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new ReportHRCVTask(mapGISFrame).execute();

                } else if (function.equals("退出高层关阀")) {
                    quitFunction();
                    onBackPressed();
                }
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onBackPressed() {
        return super.onBackPressed();
    }
}
