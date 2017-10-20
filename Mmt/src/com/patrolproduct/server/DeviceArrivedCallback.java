package com.patrolproduct.server;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.support.v4.view.ViewPager;

import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.R;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.map.PlanPageFragment;
import com.zondy.mapgis.android.annotation.Annotation;

import java.util.List;

public class DeviceArrivedCallback extends BaseMapCallback {
    private List<PatrolDevice> devices;

    public DeviceArrivedCallback(List<PatrolDevice> devices) {
        this.devices = devices;
    }

    @Override
    public boolean handleMessage(Message msg) {
        try {
            Resources resources = mapGISFrame.getResources();
            boolean shouldRefresh = false;

            int index = 0;

            // 设备点的到位反馈情况
            while (index < mapView.getAnnotationLayer().getAllAnnotations().size()) {
                try {
                    Annotation annotation = mapView.getAnnotationLayer().getAllAnnotations().get(index);

                    if (!(annotation instanceof MmtAnnotation)) {
                        continue;
                    }

                    MmtAnnotation mmtAnnotation = (MmtAnnotation) annotation;

                    if (mmtAnnotation.Type != MmtAnnotation.MY_PLAN) {
                        continue;
                    }

                    PatrolDevice patrolDevice = new PatrolDevice();

                    patrolDevice.TaskId = Integer.valueOf(mmtAnnotation.attrMap.get("TaskId"));
                    patrolDevice.LayerName = mmtAnnotation.attrMap.get("LayerName");
                    patrolDevice.PipeNo = mmtAnnotation.attrMap.get("PipeNo");

                    int i = this.devices.indexOf(patrolDevice);

                    if (i == -1)
                        continue;

                    if (this.devices.get(i).IsFeedbacked) {
                        annotation.setImage(BitmapFactory.decodeResource(resources, R.drawable.map_patrol_feedbacked));
                    } else if (this.devices.get(i).IsArrived) {
                        annotation.setImage(BitmapFactory.decodeResource(resources, R.drawable.map_patrol_arrived));
                    }

                    shouldRefresh = true;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    index++;
                }
            }

            if (!shouldRefresh)
                return false;

            mapView.refresh();

            ViewPager pager = mapGISFrame.getFragment().getViewPager();

            if (pager == null || pager.getAdapter() == null)
                return false;

            ((PlanPageFragment) pager.getAdapter().instantiateItem(pager, pager.getCurrentItem())).setTaskState();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }
}