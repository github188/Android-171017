package com.patrol.module.map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.global.MmtMainService;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.MapViewExtentChangeListener;
import com.mapgis.mmt.module.gis.toolbar.query.point.OnPointClickListener;
import com.patrol.common.TaskActionListener;
import com.patrol.entity.KeyPoint;
import com.patrol.entity.TaskInfo;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicImage;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.List;

public class OnTaskMapChangeListener implements MapViewExtentChangeListener, MapView.MapViewTapListener, Runnable {
    private MapGISFrame mapGISFrame;
    private MapView mapView;

    private GraphicLayer pointLayer = new GraphicLayer();
    private GraphicLayer lineLayer = new GraphicLayer();

    private TaskInfo task;
    private TaskActionListener listener;
    private long level = 0;
    private long lineLevel = 0;

    private OnPointClickListener pointClickListener;

    public OnTaskMapChangeListener(MapGISFrame mapGISFrame, MapView mapView, TaskActionListener listener) {
        this.mapGISFrame = mapGISFrame;
        this.mapView = mapView;
        this.listener = listener;

        this.level = MyApplication.getInstance().getConfigValue("MyPlanDetailLevel", 6);

        if (this.level > mapView.getMaxZoom())
            this.level = (long) mapView.getMaxZoom() / 2;

        this.lineLevel = MyApplication.getInstance().getConfigValue("MyPlanDetailLevelForLine", this.level);

        pointClickListener = new OnPointClickListener(mapGISFrame, mapView);

        lineLayer.setName("MyPlanLines");
        mapView.getGraphicLayers().add(lineLayer);

        pointLayer.setName("MyPlanPoints");
        mapView.getGraphicLayers().add(pointLayer);

        mapView.setTapListener(this);
    }

    /**
     * 点击地图的响应事件，用于反馈点太密集时候的列表选择
     */
    public void mapViewTap(PointF pointF) {
        try {
            boolean isOk = queryKeyPoint(pointF);

            if (!isOk && MyApplication.getInstance().getConfigValue("QueryInPatrol", 0) > 0)
                pointClickListener.onClick(pointF);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean queryKeyPoint(PointF pointF) {
        try {
            final List<KeyPoint> points = new ArrayList<>();
            List<String> items = new ArrayList<>();

            for (Graphic graphic : pointLayer.getAllGraphics()) {
                if (!(graphic instanceof GraphicImage))
                    return false;

                Dot dot = ((GraphicImage) graphic).getPoint();

                if (rect != null && !GisUtil.isInRect(rect, dot, 0))//不在范围内的剔除
                    continue;

                if (!mapView.graphicHitTest(graphic, pointF.x, pointF.y))
                    continue;

                int id = Integer.valueOf(graphic.getAttributeValue("KP-ID"));

                KeyPoint kp = task.findPointByID(id);

                String layerName = kp.GisLayer;
                String pipeNo = kp.FieldName + "=" + kp.FieldValue;

                String state = kp.getState();

                items.add(layerName + "," + pipeNo + "," + state);
                points.add(kp);
            }

            if (points.size() > 1) {
                ListDialogFragment fragment = new ListDialogFragment("设备点列表", items);

                fragment.show(mapGISFrame.getSupportFragmentManager(), "");

                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {

                    @Override
                    public void onListItemClick(int arg2, String value) {
                        try {
                            KeyPoint kp = points.get(arg2);

                            listener.onFeedback(kp);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                return true;
            } else if (points.size() == 1) {
                KeyPoint kp = points.get(0);

                listener.onFeedback(kp);

                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public void ExtentChanging() {
    }

    private Rect rect;

    @Override
    public void ExtentChanged() {
        if (!isRun)
            refresh();
    }

    private void refresh() {
        try {
            Intent intent = new Intent(MmtMainService.class.getName());

            Dot center = mapView.getCenterPoint();

            intent.putExtra("cx", center.x);
            intent.putExtra("cy", center.y);

            mapGISFrame.sendBroadcast(intent);

            rect = mapView.getDispRange();

            pointLayer.removeAllGraphics();
            lineLayer.removeAllGraphics();
//            mapView.getAnnotationLayer().removeAllAnnotations();
            int zoom = Math.round(mapView.getZoom());

            if (zoom >= level) {//第几级开始显示大图标
                showKeyPointsDetail(zoom);

                showPipeLine(zoom);
            } else {
                for (KeyPoint kp : task.Points) {
                    if (kp.Type == 2)//管段剔除
                        continue;

                    if (rect != null && !GisUtil.isInRect(rect, kp.getDot(), 0))//不在范围内的剔除
                        continue;

                    GraphicPoint point = new GraphicPoint(kp.getDot(), 5);

                    point.setAttributeValue("KP-ID", String.valueOf(kp.ID));
                    point.setColor(kp.getStateColor(mapGISFrame));

                    pointLayer.addGraphic(point);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showKeyPointsDetail(int zoom) {
        //绘制点
        for (KeyPoint kp : task.Points) {
            try {
                if (rect != null && !GisUtil.isInRect(rect, kp.getDot(), 0))//不在范围内的剔除
                    continue;

                if (kp.Type == 2) {//管段
                    if (lineLevel == -1)
                        continue;

                    if (zoom < lineLevel)
                        continue;

                    GraphicPolylin line = new GraphicPolylin(kp.getLine());

                    line.setLineWidth(8);
                    line.setAttributeValue("KP-ID", String.valueOf(kp.ID));
                    line.setColor(kp.IsArrive == 1 ? Color.GREEN : Color.RED);

                    lineLayer.addGraphic(line);
                } else {//设备点
                    Bitmap bitmap = kp.getStateBitmap(mapGISFrame);

                    GraphicImage image = new GraphicImage(kp.getDot(), bitmap);

                    image.setAttributeValue("KP-ID", String.valueOf(kp.ID));

                    image.setAnchorPoint(new PointF(0.5f, 0f));

                    pointLayer.addGraphic(image);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showPipeLine(int zoom) {
        try {
            if (rect == null)
                return;

            if (lineLevel == -1)
                return;

            if (zoom < lineLevel)
                return;

            List<KeyPoint> points = listener.fetchPipeLines(task.ID, rect.xMin, rect.yMin, rect.xMax, rect.yMax);

            //绘制点
            for (KeyPoint kp : points) {
                try {
                    GraphicPolylin line = new GraphicPolylin(kp.getLine());

                    line.setLineWidth(8);
                    line.setAttributeValue("KP-ID", String.valueOf(kp.ID));
                    line.setColor(kp.IsArrive == 1 ? Color.GREEN : Color.RED);

                    lineLayer.addGraphic(line);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setTask(TaskInfo task) {
        this.task = task;
    }

    public void remove() {
        mapView.getGraphicLayers().remove(pointLayer);
        mapView.getGraphicLayers().remove(lineLayer);

//        mapView.getAnnotationLayer().removeAllAnnotations();

//        executorService.shutdownNow();

        pointClickListener.onStop();
    }

    private boolean isRun = false;

    @Override
    public void run() {
        isRun = true;

        refresh();

        isRun = false;
    }
}
