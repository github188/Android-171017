package com.patrolproduct.module.myplan.entity;

import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.MyPlanUtil;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;

public class PatrolPath implements Parcelable {
    public String PathID;
    public String PathName;
    public String PathRange;

    /**
     * 绘制路径
     */
    public Rect draw(MapView mapView, PatrolTask patrolTask) {
        String[] lineStrings = patrolTask.PlanInfo.PPath.PathRange.split(";");

        Dots dots = MyPlanUtil.buildDots(lineStrings);

        int cur = mapView.getGraphicLayer().getGraphicCount();

        Rect rect = GisUtil.showLine(mapView, dots);

        for (int i = cur; i < mapView.getGraphicLayer().getGraphicCount(); i++) {
            mapView.getGraphicLayer().getGraphic(i).setAttributeValue("图形显示类别", "巡检任务");
            mapView.getGraphicLayer().getGraphic(i).setAttributeValue("巡检任务编号", patrolTask.TaskID);
        }

        // 这段代码作为本地存储路径巡检的关键点，不能注释掉
        try {
            ArrayList<PatrolDevice> devices = DatabaseHelper.getInstance().query(PatrolDevice.class,
                    "taskId=" + patrolTask.TaskID);

            // 仅仅路径巡检绘制路径拐点作为到位关键点
            if (Integer.valueOf(patrolTask.PlanInfo.PType) == MyPlanUtil.PLAN_PATH_PID) {
                for (int i = 0; i < dots.size(); i++) {
                    drawKeyDot(mapView, patrolTask, devices, dots.get(i), i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return rect;
    }

    /**
     * 路径巡检，区域巡检 绘制存储关键点
     */
    private void drawKeyDot(MapView mapView, PatrolTask patrolTask, ArrayList<PatrolDevice> devices, Dot dot, int index) {

        final PatrolDevice device = new PatrolDevice();

        device.TaskId = Integer.valueOf(patrolTask.TaskID);
        device.FlowId = Integer.valueOf(patrolTask.PlanFlowID);

        device.X = dot.getX();
        device.Y = dot.getY();

        device.Index = index;

        device.LayerName = "";
        device.PipeNo = index + "";

        if (devices != null && devices.size() != 0) {
            PatrolDevice patrolDevice = devices.get(index);

            device.IsArrived = patrolDevice.IsArrived;
            device.ArrivedDate = patrolDevice.ArrivedDate;
            device.IsFeedbacked = patrolDevice.IsFeedbacked;
            device.FeedbackDate = patrolDevice.FeedbackDate;

            device.Type = "pathKeyPoint";
        }

        MyApplication.getInstance().executeSingleExecutorService(new Runnable() {
            @Override
            public void run() {
                try {
                    if (device.fromDB() != null)
                        DatabaseHelper.getInstance().update(PatrolDevice.class, device.generateContentValues(), device.getUid());
                    else {
                        device.IsArrived = false;
                        device.IsFeedbacked = false;
                        device.Type = "pathKeyPoint";
                        DatabaseHelper.getInstance().insert(device);
                    }

                    BaseClassUtil.loge(this, "完成路径关键点更新到数据库-" + device.getUid());

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        int state = R.drawable.map_patrol_unarrived;

        if (device.IsFeedbacked) {
            state = R.drawable.map_patrol_feedbacked;
        } else if (device.IsArrived) {
            state = R.drawable.map_patrol_arrived;
        }

        MmtAnnotation mmtAnnotation = new MmtAnnotation("", "拐点", device.X + "," + device.Y, dot, BitmapFactory.decodeResource(
                mapView.getContext().getResources(), state));

        mmtAnnotation.attrMap.put("TaskId", String.valueOf(device.TaskId));
        mmtAnnotation.attrMap.put("LayerName", device.LayerName);
        mmtAnnotation.attrMap.put("PipeNo", device.PipeNo);
        mmtAnnotation.attrMap.put("FlowId", String.valueOf(device.FlowId));
        mmtAnnotation.attrMap.put("IsArrived", String.valueOf(device.IsArrived));
        mmtAnnotation.attrMap.put("IsFeedbacked", String.valueOf(device.IsFeedbacked));

        mmtAnnotation.attrMap.put("X", String.valueOf(device.X));
        mmtAnnotation.attrMap.put("Y", String.valueOf(device.Y));


        mmtAnnotation.Type = MmtAnnotation.MY_PLAN;

        mapView.getAnnotationLayer().addAnnotation(mmtAnnotation);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(PathID);
        out.writeString(PathName);
        out.writeString(PathRange);
    }

    public static final Parcelable.Creator<PatrolPath> CREATOR = new Parcelable.Creator<PatrolPath>() {
        @Override
        public PatrolPath createFromParcel(Parcel in) {
            return new PatrolPath(in);
        }

        @Override
        public PatrolPath[] newArray(int size) {
            return new PatrolPath[size];
        }
    };

    private PatrolPath(Parcel in) {
        PathID = in.readString();
        PathName = in.readString();
        PathRange = in.readString();
    }

}
