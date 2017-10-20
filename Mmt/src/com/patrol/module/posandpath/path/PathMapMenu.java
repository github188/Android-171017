package com.patrol.module.posandpath.path;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.common.detector.MathUtils;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.MapViewExtentChangeListener;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.patrol.module.posandpath.beans.PointInfo;
import com.mapgis.mmt.global.MmtBaseTask;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicImage;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;
import com.zondy.mapgis.geometry.Rect;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * User: zhoukang
 * Date: 2016-03-24
 * Time: 13:56
 * <p>
 * trunk:
 */
public class PathMapMenu extends BaseMapMenu implements MapViewExtentChangeListener {
    private Context mContext;
    private ArrayList<PointInfo> listInfos;  // 所有提交的点的信息

    View bottomBarView;

    public PathMapMenu(Context context, final MapGISFrame mapGISFrame, ArrayList listInfos) {
        super(mapGISFrame);
        this.mContext = context;
        this.listInfos = listInfos;

        bottomBarView = initBottomBarView(R.layout.today_trace_bottom_bar);

        bottomBarView.findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ShowTraceTask(mapGISFrame).mmtExecute("");
            }
        });
    }

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

            /**
             * 给mapView添加单击事件
             */
            mapView.setTapListener(new MapView.MapViewTapListener() {
                @Override
                public void mapViewTap(PointF pointF) {
                    for (Graphic graphic :
                            pointLayer.getAllGraphics()) {
                        if (graphic instanceof GraphicPoint && mapView.graphicHitTest(graphic, pointF.x, pointF.y)) {
                            // 该对象时GraphicPoint的实例
                            // 弹出对话框显示详细信息
                            String index = graphic.getAttributeValue("index");
                            if (TextUtils.isEmpty(index)) {
                                return;
                            }
                            showInfo(listInfos.get(Integer.valueOf(index)));
                            return;
                        }
                    }
                }
            });
            ((MmtMapView) mapView).setExtentChangeListener(this);

            return true;
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    private Dialog dialog;

    /**
     * 显示某个点的详细信息
     */
    private void showInfo(PointInfo pointInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mapGISFrame);
        builder.setTitle("详细信息");

        View view = View.inflate(mapGISFrame, R.layout.point_info_view, null);

        ((TextView) view.findViewById(R.id.name)).setText("姓名：" + pointInfo.getName());
        ((TextView) view.findViewById(R.id.time)).setText("时间：" + pointInfo.getTime());
        ((TextView) view.findViewById(R.id.position)).setText("位置：" + pointInfo.getPosition());
        ((TextView) view.findViewById(R.id.accr)).setText("定位精度：" + pointInfo.getAccr());
        ((TextView) view.findViewById(R.id.res)).setText("定位来源：" + pointInfo.getRes());
        ((TextView) view.findViewById(R.id.lat)).setText("经度：" + pointInfo.getLat());
        ((TextView) view.findViewById(R.id.lag)).setText("纬度：" + pointInfo.getLag());
        ((TextView) view.findViewById(R.id.speed)).setText("速度：" + pointInfo.getSpeed());
        ((TextView) view.findViewById(R.id.cpu)).setText("cpu：" + pointInfo.getCpu());
        ((TextView) view.findViewById(R.id.category)).setText("电量：" + pointInfo.getCategory());
        ((TextView) view.findViewById(R.id.memory)).setText("内存：" + pointInfo.getMemory());

        builder.setView(view);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);

        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("今日轨迹");

        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });

        return view;
    }

    int[] icons;
    Resources resources;
    int start = 0;

    class ShowTraceTask extends MmtBaseTask<String, Integer, Dots> {
        String span;

        public ShowTraceTask(Context context) {
            super(context);
        }

        /**
         * 在执行异步任务时清除地图上的所有信息
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mapView.getAnnotationLayer().removeAllAnnotations();
            mapView.getGraphicLayer().removeAllGraphics();

            pointLayer.removeAllGraphics();
            lineLayer.removeAllGraphics();
        }

        @Override
        protected Dots doInBackground(String... params) {
            ArrayList<Dots> dotsList = new ArrayList<>();
            // 创建 线 对象
            Dots dots = new Dots();
            try {
                for (int i = start; i < listInfos.size(); i++) {
                    try {
                        PointInfo pointInfo = listInfos.get(i);

                        String position = pointInfo.getPosition();

                        Dot dot = new Dot(Double.valueOf(position.split(",")[0]), Double.valueOf(position.split(",")[1]));

                        dots.append(dot);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (dots.size() > 0)
                    dotsList.add(dots);

                span = listInfos.get(0).getTime() + " 到 " + listInfos.get(listInfos.size() - 1).getTime();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return dots;
        }

        @Override
        protected void onSuccess(Dots dots) {
            super.onSuccess(dots);
            try {
                GraphicPolylin online = new GraphicPolylin(dots);

                online.setColor(resources.getColor(R.color.trace_online));
                online.setLineWidth(6);

                lineLayer.addGraphic(online);

                if (listInfos.size() > 0) {
                    String position = listInfos.get(0).getPosition();
                    String[] xy = position.split(",");
                    Dot dot = new Dot(Double.valueOf(xy[0]), Double.valueOf(xy[1]));

                    GraphicImage startImage = new GraphicImage(dot, BitmapFactory.decodeResource(resources, R.drawable.icon_track_navi_end));
                    startImage.setAnchorPoint(new PointF(0.5f, 0f));

                    lineLayer.addGraphic(startImage);
                }

                if (listInfos.size() > 1) {

                    String position = listInfos.get(listInfos.size() - 1).getPosition();
                    String[] xy = position.split(",");
                    Dot dot = new Dot(Double.valueOf(xy[0]), Double.valueOf(xy[1]));
                    GraphicImage endImage = new GraphicImage(dot, BitmapFactory.decodeResource(resources, R.drawable.icon_track_navi_start));
                    endImage.setAnchorPoint(new PointF(0.5f, 0f));

                    lineLayer.addGraphic(endImage);
                }

                setStatics();

                ((TextView) bottomBarView.findViewById(R.id.tvTraceSpan)).setText(span);
                ((TextView) bottomBarView.findViewById(R.id.tvTraceStatistics)).setText("总路程：" + statisics + "米");
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                mapView.refresh();
            }
        }
    }

    private Double statisics = 0.0;

    /**
     * 计算总路程
     */
    private void setStatics() {
        for (int i = 0; i < listInfos.size() - 1; i++) {
            String[] xy1 = listInfos.get(i).getPosition().split(",");
            String[] xy2 = listInfos.get(i + 1).getPosition().split(",");
            statisics += MathUtils.distance(Float.valueOf(xy1[0]), Float.valueOf(xy1[1]), Float.valueOf(xy2[0]), Float.valueOf(xy2[1]));
        }
        BigDecimal bigDecimal = new BigDecimal(statisics);
        statisics = bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
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

            for (int i = 0; i < listInfos.size(); i++) {
                PointInfo pointInfo = listInfos.get(i);

                Dot dot = convertToPoint(pointInfo.getPosition());

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

    private Dot convertToPoint(String position) {
        String[] xy = position.split(",");
        return new Dot(Double.valueOf(xy[0]), Double.valueOf(xy[1]));
    }

    /**
     * 从地图界面返回到之前的界面
     */
    private void backActivity() {
        mapView.getGraphicLayers().remove(pointLayer);
        mapView.getGraphicLayers().remove(lineLayer);
        mapGISFrame.resetMenuFunction();
        mapView.removeView(bottomBarView);

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        try {
            Intent intent = ((Activity) mContext).getIntent();

            intent.setClass(mapGISFrame, mContext.getClass());

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            mapGISFrame.startActivity(intent);
            MyApplication.getInstance().startActivityAnimation(mapGISFrame);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onBackPressed() {
        backActivity();
        return true;
    }
}
