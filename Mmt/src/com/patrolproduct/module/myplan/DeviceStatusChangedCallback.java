package com.patrolproduct.module.myplan;

import android.content.ContentValues;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.support.v4.view.ViewPager;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.SavedReportInfo;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.R;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.feedback.FeedItem;
import com.patrolproduct.module.myplan.map.PlanPageFragment;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPolylin;

import java.util.ArrayList;

public class DeviceStatusChangedCallback extends BaseMapCallback {
    PatrolDevice device;

    public DeviceStatusChangedCallback(PatrolDevice device) {
        this.device = device;
    }

    void saveState() {
        try {
            ContentValues cv = new ContentValues();

            if (device.IsFeedbacked) {
                MyPlanUtil.updateArriveOrFeedbackState(device.TaskId, device.LayerName, device.PipeNo, false);

                cv.put("isFeedbacked", true);
                cv.put("feedbackDate", BaseClassUtil.getSystemTime());
            } else if (device.IsArrived) {
                MyPlanUtil.updateArriveOrFeedbackState(device.TaskId, device.LayerName, device.PipeNo, true);

                ArrayList<FeedItem> items = new ArrayList<>();

                items.add(new FeedItem("taskid", "0", String.valueOf(device.TaskId)));
                items.add(new FeedItem("index", "0", String.valueOf(device.Index)));

                String json = new Gson().toJson(items);

                SavedReportInfo info = new SavedReportInfo(device.TaskId, json, "", "arrive");

                DatabaseHelper.getInstance().insert(info);

                cv.put("isArrived", true);
                cv.put("arrivedDate", BaseClassUtil.getSystemTime());
            }

            DatabaseHelper.getInstance().update(PatrolDevice.class, cv, device.getUid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        try {
            saveState();

            if (this.mapView == null) {
                return false;
            }

            Resources resources = mapGISFrame.getResources();
            boolean shouldRefresh = false;

            PatrolDevice patrolDevice = new PatrolDevice();

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

                    patrolDevice.TaskId = Integer.valueOf(mmtAnnotation.attrMap.get("TaskId"));
                    patrolDevice.LayerName = mmtAnnotation.attrMap.get("LayerName");
                    patrolDevice.PipeNo = mmtAnnotation.attrMap.get("PipeNo");

                    if (patrolDevice.equals(device)) {
                        if (device.IsFeedbacked) {
                            annotation.setImage(BitmapFactory.decodeResource(resources, R.drawable.map_patrol_feedbacked));
                        } else if (device.IsArrived) {
                            annotation.setImage(BitmapFactory.decodeResource(resources, R.drawable.map_patrol_arrived));
                        }

                        shouldRefresh = true;
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    index++;
                }
            }

            index = 0;

            // 管线设备的到位显示
            while (index < mapView.getGraphicLayer().getAllGraphics().size()) {
                try {
                    Graphic graphic = mapView.getGraphicLayer().getAllGraphics().get(index);

                    String value = graphic.getAttributeValue("$计划类型$");

                    if (graphic instanceof GraphicPolylin && !BaseClassUtil.isNullOrEmptyString(value)
                            && Integer.valueOf(value) == MyPlanUtil.PLAN_PIPE_PID) {
                        patrolDevice.TaskId = Integer.valueOf(graphic.getAttributeValue("$TaskId$"));
                        patrolDevice.LayerName = graphic.getAttributeValue("$LayerName$");
                        patrolDevice.PipeNo = graphic.getAttributeValue("$PipeNo$");

                        if (patrolDevice.equals(device)) {
                            if (device.IsArrived) {
                                graphic.setColor(mapGISFrame.getResources().getColor(R.color.progressbar_blue));
                            }

                            shouldRefresh = true;
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    index++;
                }
            }

            if (shouldRefresh) {
                mapView.refresh();
                updatePlanTitleBar(patrolDevice.TaskId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    /**
     * 更新计划状态栏
     */
    private void updatePlanTitleBar(int taskID) {
        if (mapGISFrame.getFragment().getViewPager() == null) {
            return;
        }

        ViewPager viewPager = mapGISFrame.getFragment().getViewPager();

        if (viewPager.getAdapter() == null) {
            return;
        }

        int index = viewPager.getCurrentItem();

        PlanPageFragment fragment = (PlanPageFragment) viewPager.getAdapter().instantiateItem(viewPager, index);

        fragment.setTaskState(taskID);
    }
}
