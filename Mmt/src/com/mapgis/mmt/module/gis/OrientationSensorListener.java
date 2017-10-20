package com.mapgis.mmt.module.gis;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.mapgis.mmt.common.util.GisUtil;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicImage;
import com.zondy.mapgis.android.mapview.MapView;

import java.util.List;

class OrientationSensorListener implements SensorEventListener {

    private float predegree = 0;

    private final MapView mapView;

    public OrientationSensorListener(MapView mapView) {
        this.mapView = mapView;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        try {
            /**
             * values[0]: x-axis 方向加速度 　　 values[1]: y-axis 方向加速度 　　 values[2]:
             * z-axis 方向加速度
             */
            @SuppressWarnings("deprecation")
            float degree = event.values[SensorManager.DATA_X];// 存放了方向值

            if (Math.abs(degree - predegree) < 5) {
                return;
            }

            List<Graphic> graphics = mapView.getGraphicLayer().getGraphicsByAttribute("$定时定位$", "true");

            if (graphics != null && graphics.size() > 0) {
                for (Graphic graphic : graphics) {
                    if (graphic instanceof GraphicImage) {
                        GraphicImage image = (GraphicImage) graphic;

                        if (GisUtil.isInRect(mapView.getDispRange(), image.getPoint(), 10)) {
                            image.setRotateAngle(-degree);

                            predegree = degree;

                            mapView.refresh();
                        }

                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
