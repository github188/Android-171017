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
import com.zondy.mapgis.android.graphic.GraphicText;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LocationTimerCallback extends BaseMapCallback {
    GpsXYZ xyz;
    boolean isPanTo;

    public LocationTimerCallback(GpsXYZ xyz) {
        this(xyz, false);
    }

    public LocationTimerCallback(GpsXYZ xyz, boolean isPanTo) {
        this.xyz = xyz;
        this.isPanTo = isPanTo;
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

            GraphicText text = new GraphicText();
            text.setColor(Color.parseColor("#E51C23"));
            text.setFontSize(25);
            text.setAttributeValue("$定时定位$", "true");
            text.setAttributeValue("$类型$", "精度");

            graphics.add(circle);
            graphics.add(image);
            graphics.add(text);

            layer.addGraphic(circle);
            layer.addGraphic(image);
            layer.addGraphic(text);

            return graphics;
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        try {
            List<Graphic> images = getGpsGraphics();

            Dot dot = new Dot(xyz.getX(), xyz.getY());
            float accuracy = 0;
            if (GpsReceiver.getInstance().getLastLocation() != null) {
                accuracy = GpsReceiver.getInstance().getLastLocation().getAccuracy();
            }

            for (Graphic graphic : images) {
                if (graphic instanceof GraphicImage) {

                    ((GraphicImage) graphic).setPoint(dot);
                } else if (graphic instanceof GraphicCircle) {

                    ((GraphicCircle) graphic).setRadius(accuracy);
                    ((GraphicCircle) graphic).setCenterPoint(dot);
                } else if (graphic instanceof GraphicText) {

                    ((GraphicText) graphic).setText(formatAccuracy(accuracy));
                    ((GraphicText) graphic).setPoint(dot);
                }
            }

            refreshAccuracyText();
            mapView.setZoomChangedListener(new MapView.MapViewZoomChangedListener() {
                @Override
                public void mapViewZoomChanged(MapView mapView, float v, float v1) {
                    MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            refreshAccuracyText();
                            return false;
                        }
                    });
                }
            });

            if (isPanTo)
                mapView.panToCenter(dot, true);

            mapView.refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    private void refreshAccuracyText() {
        List<Graphic> images = getGpsGraphics();

        Dot dot = new Dot(xyz.getX(), xyz.getY());
        float accuracy = 0;
        if (GpsReceiver.getInstance().getLastLocation() != null) {
            accuracy = GpsReceiver.getInstance().getLastLocation().getAccuracy();
        }
        float imageHeight = 0f;
        float radiusViewLength = 0f;
        GraphicText text = null;

        for (Graphic graphic : images) {
            if (graphic instanceof GraphicImage) {
                imageHeight = ((GraphicImage) graphic).getImageHeight() / 3f;
            } else if (graphic instanceof GraphicCircle) {
                radiusViewLength = getViewRadius(dot, accuracy);
            } else if (graphic instanceof GraphicText) {
                text = (GraphicText) graphic;
            }
        }

        if (text != null) {
            long textHeight = text.getTextHeight();
            if (imageHeight > radiusViewLength) {
                text.setAnchorPoint(new PointF(0.5f, -imageHeight / textHeight - 0.1f));
            } else {
                // 保证文字在线上，下移0.5f
                text.setAnchorPoint(new PointF(0.5f, -radiusViewLength / textHeight + 0.5f));
            }
            mapView.refresh();
        }
    }

    private String formatAccuracy(float accuracy) {
        DecimalFormat format = new DecimalFormat();
        if (accuracy < 1) {
            format.applyPattern("#cm");
            return format.format(accuracy * 100);
        } else if (accuracy < 10) {
            format.applyPattern("#.0m");
            return format.format(accuracy);
        } else if (accuracy < 1000) {
            format.applyPattern("#m");
            return format.format(accuracy);
        } else {
            format.applyPattern("#.0km");
            return format.format(accuracy / 1000);
        }
    }

    private float getViewRadius(Dot dot, float accuracy) {
        PointF pointF = mapView.mapPointToViewPoint(dot);
        PointF pointF1 = mapView.mapPointToViewPoint(new Dot(dot.getX(), dot.getY() + accuracy));
        return Math.abs(pointF1.y - pointF.y);
    }

    public GpsXYZ getXyz() {
        return xyz;
    }

    public void setXyz(GpsXYZ xyz) {
        this.xyz = xyz;
    }
}
