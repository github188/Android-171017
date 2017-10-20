package com.patrol.module.posandpath.path;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.MapViewExtentChangeListener;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gps.GpsStateFragment;
import com.mapgis.mmt.module.gps.entity.TracePoint;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.global.MmtBaseTask;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicImage;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.graphic.GraphicStippleLine;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 今日轨迹
 */
public class PathOnMapMenu extends BaseMapMenu implements MapViewExtentChangeListener, MapView.MapViewTapListener {
    View bottomBarView;
    private Context context;

    public PathOnMapMenu(final MapGISFrame mapGISFrame,Context context) {
        super(mapGISFrame);

        this.context = context;

        bottomBarView = initBottomBarView(R.layout.today_trace_bottom_bar);

        bottomBarView.findViewById(R.id.btnNext).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new ShowTraceTask(mapGISFrame).mmtExecute("");
            }
        });
    }

    List<GpsXYZ> xyzs;
    GraphicLayer pointLayer = new GraphicLayer();
    GraphicLayer lineLayer = new GraphicLayer();
    long level = 0;

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();

            return false;
        }

        try {
            this.level = MyApplication.getInstance().getConfigValue("MyTraceDetailLevel", 8);

            if (this.level > mapView.getMaxZoom())
                this.level = (long) mapView.getMaxZoom() / 2;

            lineLayer.setName("MyPlanLines");
            mapView.getGraphicLayers().add(lineLayer);

            pointLayer.setName("MyPlanPoints");
            mapView.getGraphicLayers().add(pointLayer);

            resources = mapView.getContext().getResources();
            icons = new int[]{R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc
                    , R.drawable.icon_markd, R.drawable.icon_marke, R.drawable.icon_markf, R.drawable.icon_markg
                    , R.drawable.icon_markh, R.drawable.icon_marki, R.drawable.icon_markj};

            new ShowTraceTask(mapGISFrame).mmtExecute("");

            mapView.setTapListener(this);
            ((MmtMapView) mapView).setExtentChangeListener(this);

            return true;
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.today_trace_top_bar, mapView, false);

        // 返回按钮
        view.findViewById(R.id.todayTraceTitleBackButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        view.findViewById(R.id.ivNext).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new ShowTraceTask(mapGISFrame).mmtExecute("");
            }
        });

        return view;
    }

    int index = 0;
    int[] icons;
    Resources resources;
    ArrayList<GpsXYZ> allDots;
    int start = 0;
    String statistics;

    class ShowTraceTask extends MmtBaseTask<String, Integer, ArrayList<Dots>> {
        String span;

        public ShowTraceTask(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mapView.getAnnotationLayer().removeAllAnnotations();
            mapView.getGraphicLayer().removeAllGraphics();

            pointLayer.removeAllGraphics();
            lineLayer.removeAllGraphics();
        }

        @Override
        protected ArrayList<Dots> doInBackground(String... params) {
            ArrayList<Dots> dotsList = new ArrayList<>();

            try {
                if (xyzs == null && !initData()) {
                    return null;
                }

                // 创建 线 对象
                Dots dots = new Dots();
                boolean inHourse = false;
                long preTick = -1;
                int total = 0;
                allDots = new ArrayList<>();

                if (xyzs.size() > 0 && start >= xyzs.size()) {
                    start = 0;
                    index = 0;
                }

                GpsXYZ startXY = xyzs.get(start);

                for (int i = start; i < xyzs.size(); i++) {
                    try {
                        GpsXYZ xyz = xyzs.get(i);

                        if (!xyz.isUsefull()) {
                            continue;
                        }

                        Location location = xyz.getLocation();

                        if (location == null)
                            continue;

                        if ("BD-NET,HC-未知,NC-未知".contains(location.getProvider()))
                            continue;

                        if (location.getAccuracy() > 10) {
                            if (!inHourse) {
                                inHourse = true;
                            } else
                                continue;
                        } else
                            inHourse = false;

                        Date now = BaseClassUtil.parseTime(xyz.getReportTime());

                        if (now == null)
                            continue;

                        if (preTick > 0 && (now.getTime() - preTick) > 15 * 60 * 1000 && dots.size() > 0) {
                            dotsList.add(dots);

                            dots = new Dots();
                        }

                        preTick = now.getTime();
                        total++;

                        Dot dot = new Dot(xyz.getX(), xyz.getY());

                        dots.append(dot);
                        allDots.add(xyz);

                        if (total >= 1000) {
                            break;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (total >= 1000 || (i == xyzs.size() - 1 && start > 0)) {
                            start = i + 1;
                        }
                    }
                }

                if (dots.size() > 0)
                    dotsList.add(dots);

                GpsXYZ endXY = xyzs.get(start > 0 ? (start - 1) : (xyzs.size() - 1));

                span = startXY.getReportTime().split(" ")[1] + " 到 " + endXY.getReportTime().split(" ")[1];
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return dotsList;
        }

        private boolean initData() {
            // 查询存储在Sqlite数据库的今日的坐标点
            String sql = "select * from positonreporter "
                    + "where userId=" + MyApplication.getInstance().getUserId()
                    + " and accuracy not like '%-未知%' and accuracy not like '%BD-NET%' "
                    + "and reportTime >= date('now','localtime','start of day') order by reporttime asc";

            xyzs = DatabaseHelper.getInstance().queryBySql(GpsXYZ.class, sql);

            System.out.println(xyzs.toString());

            if (xyzs == null || xyzs.size() == 0) {
                return false;
            }

            statistics = "未获取到时长里程统计信息";

            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/FetchTraceStatistics";
            String userID = String.valueOf(MyApplication.getInstance().getUserId());

            String day = BaseClassUtil.getSystemTime("yyyy-MM-dd");

            String json = NetUtil.executeHttpGet(url, "userID", userID, "start", day + " 00:00:00", "end", day + " 23:59:59");

            if (!TextUtils.isEmpty(json)) {
                ResultData<TracePoint> data = new Gson().fromJson(json, new TypeToken<ResultData<TracePoint>>() {
                }.getType());

                if (data != null && data.ResultCode > 0) {
                    statistics = data.ResultMessage;
                }
            }

            return true;
        }

        @Override
        protected void onSuccess(ArrayList<Dots> dotsList) {
            super.onSuccess(dotsList);

            try {
                if (xyzs == null || xyzs.size() == 0) {
                    Toast.makeText(mapGISFrame, "没有查询到轨迹记录", Toast.LENGTH_SHORT).show();

                    return;
                }

                for (int i = 0; i < dotsList.size(); i++) {
                    Dots dots = dotsList.get(i);

                    GraphicPolylin online = new GraphicPolylin(dots);

                    online.setColor(resources.getColor(R.color.trace_online));
                    online.setLineWidth(6);

                    lineLayer.addGraphic(online);

                    if (i > 0) {
                        Dots preDots = dotsList.get(i - 1);

                        Dot startDot = preDots.get(preDots.size() - 1), endDot = dots.get(0);

                        GraphicStippleLine offline = new GraphicStippleLine(startDot, endDot);

                        offline.setColor(resources.getColor(R.color.trace_offline));
                        offline.setLineWidth(6);

                        lineLayer.addGraphic(offline);
                    }
                }

                if (allDots.size() > 0) {
                    GraphicImage startImage = new GraphicImage(allDots.get(0).convertToPoint(), BitmapFactory.decodeResource(resources, R.drawable.icon_track_navi_end));
                    startImage.setAnchorPoint(new PointF(0.5f, 0f));

                    lineLayer.addGraphic(startImage);
                }

                if (allDots.size() > 1) {
                    GraphicImage endImage = new GraphicImage(allDots.get(allDots.size() - 1).convertToPoint(), BitmapFactory.decodeResource(resources, R.drawable.icon_track_navi_start));
                    endImage.setAnchorPoint(new PointF(0.5f, 0f));

                    lineLayer.addGraphic(endImage);
                }

                ((TextView) bottomBarView.findViewById(R.id.tvTraceSpan)).setText(span);
                ((TextView) bottomBarView.findViewById(R.id.tvTraceStatistics)).setText(statistics);

                if (start > 0) {
                    bottomBarView.findViewById(R.id.btnNext).setVisibility(View.VISIBLE);
                    bottomBarView.findViewById(R.id.tvTraceSN).setVisibility(View.VISIBLE);

                    String sn = (++index) + ".";
                    ((TextView) bottomBarView.findViewById(R.id.tvTraceSN)).setText(sn);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                mapView.refresh();
            }
        }
    }

    @Override
    public void mapViewTap(PointF arg0) {
        try {
            for (Graphic graphic : pointLayer.getAllGraphics()) {
                if (!(graphic instanceof GraphicPoint) || !mapView.graphicHitTest(graphic, arg0.x, arg0.y))
                    continue;

                String index = graphic.getAttributeValue("index");

                if (TextUtils.isEmpty(index))
                    break;

                GpsXYZ xyz = allDots.get(Integer.valueOf(index));

                Bundle bundle = new Bundle();

                bundle.putParcelable("xyz", xyz);

                GpsStateFragment fragment = new GpsStateFragment();

                fragment.setArguments(bundle);

                fragment.show(mapGISFrame.getSupportFragmentManager(), "");

                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void ExtentChanging() {
    }

    @Override
    public void ExtentChanged() {
        try {
            pointLayer.removeAllGraphics();
            int zoom = Math.round(mapView.getZoom());

            if (zoom < level)
                return;

            Rect rect = mapView.getDispRange();

            if (rect == null)
                return;

            for (int i = 0; i < allDots.size(); i++) {
                GpsXYZ xyz = allDots.get(i);

                Dot dot = xyz.convertToPoint();

                if (!GisUtil.isInRect(rect, dot))
                    continue;

                // 构造GPS点加到地图图层中
                GraphicPoint point = new GraphicPoint();

                point.setColor(resources.getColor(R.color.trace_point));
                point.setPoint(dot);
                point.setSize(zoom > 8 ? zoom : 8);

                point.setAttributeValue("index", String.valueOf(i));

                pointLayer.addGraphic(point);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onBackPressed() {
        mapView.getGraphicLayers().remove(pointLayer);
        mapView.getGraphicLayers().remove(lineLayer);

        backActivity();
        return true;
    }

    private void backActivity(){
        Intent intent = ((Activity)context).getIntent();
        intent.setClass(mapGISFrame,context.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }
}
