package com.mapgis.mmt.module.gps;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Message;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicCircle;
import com.zondy.mapgis.android.graphic.GraphicImage;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.graphic.GraphicStippleLine;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.List;

public class NavigationTimerCallback extends BaseMapCallback {

    private static final float DELTA = 0.05f; // 箭头长度为两点连线的 1/20

    private GpsXYZ startXYZ; // The current coordinates
    private GpsXYZ destXYZ;  // The destination coordinates
    private boolean isPanTo; // Whether automatic zoom to center

    private final Rect mapCenterRect; // Rectangle for zooming

    private GraphicStippleLine graphicStippleLine; // Route line
    private GraphicPolylin arrowLineA;
    private GraphicPolylin arrowLineB;
    private int lineColorRes = 0; // Line color

    public NavigationTimerCallback(GpsXYZ startXYZ, GpsXYZ destXYZ, boolean isPanTo) {
        if (startXYZ == null) {
            throw new NullPointerException("startXYZ == null");
        }
        if (destXYZ == null) {
            throw new NullPointerException("destXYZ == null");
        }

        this.startXYZ = startXYZ;
        this.destXYZ = destXYZ;
        this.isPanTo = isPanTo;

        this.mapCenterRect = new Rect();
    }

    public void updateLocations(GpsXYZ startXYZ, GpsXYZ destXYZ, boolean isPanTo) {
        if (startXYZ == null) {
            throw new NullPointerException("startXYZ == null");
        }
        if (destXYZ == null) {
            throw new NullPointerException("destXYZ == null");
        }

        this.startXYZ = startXYZ;
        this.destXYZ = destXYZ;
        this.isPanTo = isPanTo;

        MyApplication.getInstance().sendToBaseMapHandle(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        try {

            // Current point.
            List<Graphic> images = getGpsGraphics();
            for (Graphic graphic : images) {
                if (graphic instanceof GraphicImage) {
                    ((GraphicImage) graphic).setPoint(new Dot(startXYZ.getX(), startXYZ.getY()));
                } else if (graphic instanceof GraphicCircle) {
                    float accuracy = 0;
                    if (GpsReceiver.getInstance().getLastLocation() != null) {
                        accuracy = GpsReceiver.getInstance().getLastLocation().getAccuracy();
                    }
                    ((GraphicCircle) graphic).setRadius(accuracy);
                    ((GraphicCircle) graphic).setCenterPoint(new Dot(startXYZ.getX(), startXYZ.getY()));
                }
            }

            // Line
            if (lineColorRes == 0) {
                lineColorRes = mapGISFrame.getResources().getColor(R.color.default_red);
            }
            if (graphicStippleLine != null) {
                GraphicLayer graphicLayer = mapView.getGraphicLayer();
                graphicLayer.removeGraphic(graphicStippleLine);
                graphicLayer.removeGraphic(arrowLineA);
                graphicLayer.removeGraphic(arrowLineB);
            }
            graphicStippleLine = new GraphicStippleLine(startXYZ.convertToPoint(), destXYZ.convertToPoint());
            graphicStippleLine.setLineWidth(6);
            graphicStippleLine.setIsDisposable(true);
            graphicStippleLine.setColor(lineColorRes);
            mapView.getGraphicLayer().addGraphic(graphicStippleLine);

            Dot midDot = new Dot((startXYZ.getX() + destXYZ.getX()) / 2,
                    (startXYZ.getY() + destXYZ.getY()) / 2);

            Dot arrowDotA = getArrowLinePoint(startXYZ.getX(), startXYZ.getY(), destXYZ.getX(), destXYZ.getY(), true);
            arrowLineA = new GraphicPolylin(new Dot[] {arrowDotA, midDot});
            arrowLineA.setLineWidth(6);
            arrowLineA.setIsDisposable(true);
            arrowLineA.setColor(lineColorRes);
            mapView.getGraphicLayer().addGraphic(arrowLineA);

            Dot arrowDotB = getArrowLinePoint(startXYZ.getX(), startXYZ.getY(), destXYZ.getX(), destXYZ.getY(), false);
            arrowLineB = new GraphicPolylin(new Dot[] {arrowDotB, midDot});
            arrowLineB.setLineWidth(6);
            arrowLineB.setIsDisposable(true);
            arrowLineB.setColor(lineColorRes);
            mapView.getGraphicLayer().addGraphic(arrowLineB);

            // Zoom line to center
            if (isPanTo) {
                double offset = Math.abs((startXYZ.getX() - destXYZ.getX()) / 5);

                if (startXYZ.getX() > destXYZ.getX()) {
                    mapCenterRect.setXMin(destXYZ.getX() - offset);
                    mapCenterRect.setXMax(startXYZ.getX() + offset);
                } else {
                    mapCenterRect.setXMin(startXYZ.getX() - offset);
                    mapCenterRect.setXMax(destXYZ.getX() + offset);
                }
                if (startXYZ.getY() > destXYZ.getY()) {
                    mapCenterRect.setYMin(destXYZ.getY() - offset);
                    mapCenterRect.setYMax(startXYZ.getY() + offset);
                } else {
                    mapCenterRect.setYMin(startXYZ.getY() - offset);
                    mapCenterRect.setYMax(destXYZ.getY() + offset);
                }
                mapView.zoomToRange(mapCenterRect, true);
            }
            mapView.refresh();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    /**
     * 获取GPS图层
     *
     * @return GPS图层
     */
    public List<Graphic> getGpsGraphics() {
        try {
            GraphicLayer layer = mapView.getGraphicLayer();

            List<Graphic> graphics = layer.getGraphicsByAttribute("$定时定位$", "true");

            if (graphics != null && graphics.size() > 0)
                return graphics;

            graphics = new ArrayList<>();

            Bitmap bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.navi_map_gps_locked);

            GraphicImage image = new GraphicImage();
            image.setImage(bitmap);
            image.setAttributeValue("$定时定位$", "true");
            image.setAttributeValue("$类型$", "指针");
            image.setAnchorPoint(new PointF(0.5f, 0.5f));

            GraphicCircle circle = new GraphicCircle();
            circle.setBorderlineColor(Color.parseColor("#93BEDE"));
            circle.setBorderlineWidth(DimenTool.dip2px(mapGISFrame, 2));
            circle.setColor(Color.parseColor("#7FE3EBEE"));
            circle.setAttributeValue("$定时定位$", "true");
            circle.setAttributeValue("$类型$", "背景");

            graphics.add(circle);
            graphics.add(image);

            layer.addGraphic(circle);
            layer.addGraphic(image);

            return graphics;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 在两点连线中点上下 30°偏角画箭头
     */
    private Dot getArrowLinePoint(double startX, double startY, double destX, double destY, boolean flag) {

        double tempA = (1 + 1.732 * DELTA) / 2;
        double tempB = (1 - 1.732 * DELTA) / 2;

        double x = (tempA * startX) + (tempB * destX);
        if (flag) {
            x -= DELTA / 2 * (destY - startY);
        } else {
            x += DELTA / 2 * (destY - startY);
        }

        double y = (tempA * startY) + (tempB * destY);
        if (flag) {
            y += DELTA / 2 * (destX - startX);
        } else {
            y -= DELTA / 2 * (destX - startX);
        }

        return new Dot(x, y);
    }
}
