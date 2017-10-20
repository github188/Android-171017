package com.mapgis.mmt.module.gis;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.global.MmtMainService;
import com.mapgis.mmt.module.gis.onliemap.Extent;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicImage;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.graphic.GraphicText;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.Map;

public class MmtMapView extends MapView implements MapViewExtentChangeListener {
    public MmtMapView(Context arg0) {
        super(arg0);
    }

    @Override
    protected void onAttachedToWindow() {
        Log.v(getClass().getSimpleName(), "onAttachedToWindow");

        if (task.getStatus() == Status.PENDING) {
            task.executeOnExecutor(MyApplication.executorService, "");
        }
        //  super.onAttachedToWindow();
    }


    private static final Object syncRoot = new Object();

    AsyncTask<String, Integer, Rect> task = new AsyncTask<String, Integer, Rect>() {

        @Override
        protected Rect doInBackground(String... params) {
            try {
                Rect rect = getEntireRange();

                if (rect == null) {
                    synchronized (syncRoot) {
                        syncRoot.wait();

                        rect = getEntireRange();

                        if (rect == null) {
                            return null;
                        }
                    }
                }

                String range = MyApplication.getInstance().getSystemSharedPreferences().getString("preDispRange", "");

                if (!BaseClassUtil.isNullOrEmptyString(range)) {
                    Rect preRect = new Gson().fromJson(range, Rect.class);

                    // 判断上次退出的地图范围是否是有效范围
                    if (GisUtil.IsInEnvelope(preRect, rect)) {
                        rect = preRect;
                    }
                }

                return rect;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Rect rect) {
            if (rect != null) {
                zoomToRange(rect, false);
            }
        }
    };

    public Rect getEntireRange() {
        Rect rect = null;

        try {
            if (this.getMap() != null) {
                rect = this.getMap().getEntireRange();

                if (MobileConfig.MapConfigInstance != null && !BaseClassUtil.isNullOrEmptyString(MobileConfig.MapConfigInstance.Fullextent)) {
                    rect = new Extent(MobileConfig.MapConfigInstance.Fullextent).getRect();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return rect;
    }

    @Override
    public void setMap(Map map) {
        if (map != null && map.getLayerCount() > 0) {
            super.setMap(map);
        }

        synchronized (syncRoot) {
            syncRoot.notify();
        }
    }

    public void zoomFull() {
        try {
            Rect rect = this.getEntireRange();

            if (rect != null) {
                this.ExtentChanging();

                this.zoomToRange(rect, true);

                this.ExtentChanged();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean graphicHitTest(Graphic graphic, float v, float v1) {
        //解决graphicHitTest很难点击的问题

        if (graphic instanceof GraphicImage) {
            GraphicImage graphicImage = (GraphicImage) graphic;
            return custormGraphicHitTest(graphic, v, v1, Math.max(graphicImage.getImageHeight(), graphicImage.getImageWidth()) / 2.0);
        } else if (graphic instanceof GraphicPoint) {
            GraphicPoint graphicPoint = (GraphicPoint) graphic;
            return custormGraphicHitTest(graphic, v, v1, graphicPoint.getSize() / 2.0);
        } else if (graphic instanceof GraphicText) {
            GraphicText graphicText = (GraphicText) graphic;
            return custormGraphicHitTest(graphic, v, v1, Math.max(graphicText.getTextHeight(), graphicText.getTextWidth()) / 2.0);
        } else if (graphic instanceof GraphicPolylin) {
            GraphicPolylin graphicPolylin = (GraphicPolylin) graphic;
            return custormGraphicLineHitTest(graphicPolylin, v, v1);
        }

        return super.graphicHitTest(graphic, v, v1);
    }

    public boolean superGraphicHitTest(Graphic graphic, float v, float v1) {
        return super.graphicHitTest(graphic, v, v1);
    }

    private boolean custormGraphicHitTest(Graphic graphic, float v, float v1, double tolerance) {
        Rect rect = new Rect();
        PointF pointF = this.mapPointToViewPoint(graphic.getCenterPoint());
        rect.setXMin(pointF.x - 20);
        rect.setYMin(pointF.y - 20);
        rect.setXMax(pointF.x + 20);
        rect.setYMax(pointF.y + 20);
        return GisUtil.isInRect(rect, new Dot(v, v1), tolerance);
    }

    private boolean custormGraphicLineHitTest(GraphicPolylin graphicPolylin, float v, float v1) {
        int pointSize = graphicPolylin.getPointCount();

        PointF pointF0 = this.mapPointToViewPoint(graphicPolylin.getPoint(0));

        float xMin = pointF0.x;
        float xMax = pointF0.x;
        float yMin = pointF0.y;
        float yMax = pointF0.y;

        for (int i = 1; i < pointSize; i++) {
            PointF pointF = this.mapPointToViewPoint(graphicPolylin.getPoint(i));

            if (pointF.x > xMax) {
                xMax = pointF.x;
            }

            if (pointF.x < xMin) {
                xMin = pointF.x;
            }

            if (pointF.y > yMax) {
                yMax = pointF.y;
            }
            if (pointF.y < yMin) {
                yMin = pointF.y;
            }
        }

        Rect rect = new Rect(xMin - 20, yMin - 20, xMax + 20, yMax + 20);

        return GisUtil.isInRect(rect, new Dot(v, v1), graphicPolylin.getLineWidth() / 2.0);
    }

    MapViewExtentChangeListener extentChangeListener = null;

    public void setExtentChangeListener(MapViewExtentChangeListener extentChangeListener) {
        this.extentChangeListener = extentChangeListener;
    }

    public void initExtentChangeListener() {
        this.setAnimationListener(new MmtMapViewAnimationListener(this));

        this.setMapTool(new MmtMapTool(this));
    }

    @Override
    public void ExtentChanging() {
        if (extentChangeListener != null)
            extentChangeListener.ExtentChanging();
    }

    @Override
    public void ExtentChanged() {
        if (MyApplication.getInstance().getConfigValue("RandomGPS").equalsIgnoreCase("view")) {
            Intent intent = new Intent(MmtMainService.class.getName());

            Dot center = this.getCenterPoint();

            intent.putExtra("cx", center.x);
            intent.putExtra("cy", center.y);

            getContext().sendBroadcast(intent);
        }

        if (extentChangeListener != null)
            extentChangeListener.ExtentChanged();
    }
}