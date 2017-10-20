package com.patrol.module.map;

import android.graphics.Color;
import android.os.Message;
import android.text.TextUtils;

import com.mapgis.mmt.entity.BaseMapCallback;
import com.patrol.entity.KeyPoint;
import com.patrol.module.MyPlanMapMenu;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicImage;

public class DeviceStatusChangedCallback extends BaseMapCallback {
    KeyPoint kp;

    public DeviceStatusChangedCallback(KeyPoint kp) {
        this.kp = kp;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (this.mapView == null) {
            return false;
        }

        boolean shouldRefresh = false;

        try {
            if (!kp.isInView(mapView))
                return false;

            if (kp.Type != 2) {//设备点
                changeGraphicState("MyPlanPoints");
            } else {//管段
                changeGraphicState("MyPlanLines");
            }

            shouldRefresh = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (shouldRefresh)
                    mapView.refresh();

                (((MyPlanMapMenu) mapGISFrame.getFragment().menu)).onStateChanged();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }

    private void changeGraphicState(String name) {
        int index = mapView.getGraphicLayers().indexOf(name);

        if (index < 0)
            return;

        for (Graphic graphic : mapView.getGraphicLayers().getGraphicLayer(index).getAllGraphics()) {
            String id = graphic.getAttributeValue("KP-ID");

            if (TextUtils.isEmpty(id) || !id.equals(String.valueOf(kp.ID)))
                continue;

            if (kp.Type == 2) {
                graphic.setColor(kp.IsArrive == 1 ? Color.GREEN : Color.RED);
            } else {
                if (graphic instanceof GraphicImage) {
                    ((GraphicImage) graphic).setImage(kp.getStateBitmap(mapGISFrame));
                } else {
                    graphic.setColor(kp.getStateColor(mapGISFrame));
                }
            }

            return;
        }
    }
}
