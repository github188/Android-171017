package com.mapgis.mmt.module.gis.toolbar.areaopr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;

/**
 * Created by liuyunfan on 2016/3/21.
 */
public class SelectAreaMapMenu extends BaseMapMenu implements MapView.MapViewTapListener {
    Context context;
    //区域值
    //{"rings":[[[29029.484570612167,28779.70815032596],[29032.24513279996,28775.949789475908],[29027.359899238025,28774.28626901193],[29029.484570612167,28779.70815032596]]]}
    String value;

    public SelectAreaMapMenu(MapGISFrame mapGISFrame, Context context, String value) {
        super(mapGISFrame);
        this.context = context;
        this.value = value;
        AreaOprUtil.clearArea(mapView);
        if (!TextUtils.isEmpty(value)) {
            tip = AreaOprUtil.painArea(mapView, value);
        }
    }

    String tip;
    Dots dots;
    View viewBar;
    TextView tipView;

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);
        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("区域选择");
        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity(true);
            }
        });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }
        mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);

        viewBar = mapGISFrame.getLayoutInflater().inflate(R.layout.map_select_point_bar, null);
        viewBar.findViewById(R.id.tvAddr2).setVisibility(View.GONE);
        tipView = (TextView) viewBar.findViewById(R.id.tvAddr1);
        final TextView tvOK = (TextView) viewBar.findViewById(R.id.tvOk);
        if (TextUtils.isEmpty(value)) {
            tipView.setText(AreaOprUtil.notPainTxt);
            tvOK.setText(AreaOprUtil.okbtn);
            tvOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backActivity(false);
                }
            });
            mapView.setTapListener(this);
        } else {
            tipView.setText(tip);
            tvOK.setText("重新绘制");
            tvOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AreaOprUtil.clearArea(mapView);
                    value = "";
                    tipView.setText(AreaOprUtil.notPainTxt);
                    mapView.setTapListener(SelectAreaMapMenu.this);
                    tvOK.setText("确定");
                    tvOK.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            backActivity(false);
                        }
                    });
                }
            });
        }

        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(-1, -2);

        params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        viewBar.setLayoutParams(params1);
        mapView.addView(viewBar);

        this.dots = new Dots();

        return true;
    }

//    private String getvalue(Dots dots) {
//        JSONObject areaobject = new JSONObject();
//        try {
//            JSONArray jos = new JSONArray();
//            for (int i = 0; i < dots.size(); i++) {
//                JSONArray jo = new JSONArray();
//                jo.put(dots.get(i).getX());
//                jo.put(dots.get(i).getY());
//                jos.put(jo);
//            }
//            JSONArray joParent = new JSONArray();
//            joParent.put(jos);
//            areaobject.put("rings", joParent);
//
//        } catch (Exception ex) {
//            MyApplication.getInstance().showMessageWithHandle(ex.getMessage());
//        } finally {
//            return areaobject.toString();
//        }
//    }

    private void backActivity(boolean isback) {
        try {
            Intent intent = ((Activity) context).getIntent();
            intent.setClass(mapGISFrame, context.getClass());
            intent.removeExtra("area");
            if (!isback) {
                intent.putExtra("area", GisUtil.getFormatAreaByDots(dots));
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            mapGISFrame.startActivity(intent);
            mapView.removeView(viewBar);
            mapView.setTapListener(null);
            mapGISFrame.resetMenuFunction();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mapViewTap(PointF pointF) {
        if (!TextUtils.isEmpty(value)) {
            MyApplication.getInstance().showMessageWithHandle("点击" + AreaOprUtil.rebtn + "按钮开始重新绘制");
            return;
        }

        Dot dot = mapView.viewPointToMapPoint(pointF);

        GraphicPoint point = new GraphicPoint(dot, 10);

        point.setColor(Color.RED);

        mapView.getGraphicLayer().addGraphic(point);

        dots.append(dot);

        mapView.getGraphicLayer().removeGraphicByAttribute("area", "NoPoints");

        if (dots.size() > 2) {
            Dots fullDots = new Dots();

            fullDots.append(dots);
            fullDots.append(dots.get(0));

            GraphicPolygon polygon = new GraphicPolygon(fullDots);

            polygon.setColor(Color.argb(100, 0, 255, 0));
            polygon.setAttributeValue("area", "NoPoints");

            mapView.getGraphicLayer().addGraphic(polygon);

            // 兼容“逆时针在地图上点击范围时，测量结果是负值”问题
            double area = Math.abs(polygon.getArea());

            String areaString = "";

            // 1平方公里=1000000平方米
            if (area < 1000000) {
                areaString = String.format("%.1f", area) + "平方米";
            } else {
                areaString = String.format("%.1f", area / 1000000) + "平方公里";
            }
            tipView.setText("已绘制" + areaString);
        } else if (dots.size() > 1) {
            GraphicPolylin polylin = new GraphicPolylin(dots);

            polylin.setLineWidth(5);
            polylin.setColor(Color.BLACK);
            polylin.setAttributeValue("area", "NoPoints");

            mapView.getGraphicLayer().addGraphic(polylin);
        }

        mapView.refresh();
    }

    @Override
    public boolean onBackPressed() {
        backActivity(true);

        return true;
    }
}
