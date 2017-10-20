package com.patrolproduct.module.myplan.entity;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.MyPlanUtil;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;

public class PatrolArea implements Parcelable {
    public String AreaID;
    public String AreaName;
    public String AreaRange;

    public Rect draw(MapView mapView, PatrolTask patrolTask) {
       // String[] temp=patrolTask.PlanInfo.PArea.AreaRange.split("-");
       // String[] lineStrings = temp[0].split(";");
        String[] lineStrings = patrolTask.PlanInfo.PArea.AreaRange.split(";");
        Dots dots = MyPlanUtil.buildDots(lineStrings);

        GraphicPolylin polylin = new GraphicPolylin(dots);

        polylin.setLineWidth(5);
        polylin.setColor(Color.BLUE);

        polylin.setAttributeValue("图形显示类别", "巡检任务");
        polylin.setAttributeValue("巡检任务编号", patrolTask.TaskID);

        mapView.getGraphicLayer().addGraphic(polylin);
        Rect boundingRect = polylin.getBoundingRect();

        //考虑局域巡检未绘制关键点的情况，用外界矩形中心点作为关键点
        if(patrolTask.PlanInfo.PEquip.EquipEntity.equals("")){
            Dots keyDots = new Dots();
            keyDots.append(polylin.getCenterPoint());
            try {

                ArrayList<PatrolDevice> devices = DatabaseHelper.getInstance().query(PatrolDevice.class,
                        "taskId=" + patrolTask.TaskID);

                if (Integer.valueOf(patrolTask.PlanInfo.PType) == MyPlanUtil.PLAN_AREA_PID) {
                    for (int i = 0; i < keyDots.size(); i++) {
                        //如果设置了多个关键点，不存储DotsStr
                        //本地设备表中，DotsStr此处没有使用，现在用于存储该区域的外包框，用作只有一个关键点到位的判断依据
                        drawKeyDot(mapView, patrolTask, devices, keyDots.get(i), i);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        //获取关键点
//        String[] keyDotsStr = null;
//        if (temp.length>1&&!BaseClassUtil.isNullOrEmptyString(temp[1])) {
//            keyDotsStr =temp[1].split("\\*");
//        }
//        Dots keyDots = new Dots();
//        if (keyDotsStr != null && keyDotsStr.length > 0) {
//            keyDots.append(MyPlanUtil.buildDots(keyDotsStr));
//        }
        //没有设置关键点
        //将局域中心点作为到位的关键点，但是在判断是否到位时，已只要进入该局域就代表到位
        //有关键点就将关键点作为到位标志
//        if (keyDots.size() <= 0) {
//            keyDots.append(polylin.getCenterPoint());
//        }
//
//        try {
//
//            ArrayList<PatrolDevice> devices = DatabaseHelper.getInstance().query(PatrolDevice.class,
//                    "taskId=" + patrolTask.TaskID);
//
//            if (Integer.valueOf(patrolTask.PlanInfo.PType) == MyPlanUtil.PLAN_AREA_PID) {
//                String DotsStr = keyDots.size() > 1 ? boundingRect.toString() : null;
//                for (int i = 0; i < keyDots.size(); i++) {
//                    //如果设置了多个关键点，不存储DotsStr
//                    //本地设备表中，DotsStr此处没有使用，现在用于存储该区域的外包框，用作只有一个关键点到位的判断依据
//                    drawKeyDot(mapView, patrolTask, devices, keyDots.get(i), i, DotsStr);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return boundingRect;
    }

    /**
     * 区域巡检 绘制存储关键点
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

            device.Type = "Point";

           // DatabaseHelper.getInstance().update(PatrolDevice.class, device.generateContentValues(), "id=" + patrolDevice.ID);
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
                        device.Type = "Point";
                        DatabaseHelper.getInstance().insert(device);
                    }

                    BaseClassUtil.loge(this, "完成区域关键点更新到数据库-" + device.getUid());

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

        MmtAnnotation mmtAnnotation = new MmtAnnotation("", "关键点", device.X+","+device.Y, dot, BitmapFactory.decodeResource(
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
        out.writeString(AreaID);
        out.writeString(AreaName);
        out.writeString(AreaRange);
    }

    public static final Parcelable.Creator<PatrolArea> CREATOR = new Parcelable.Creator<PatrolArea>() {
        @Override
        public PatrolArea createFromParcel(Parcel in) {
            return new PatrolArea(in);
        }

        @Override
        public PatrolArea[] newArray(int size) {
            return new PatrolArea[size];
        }
    };

    private PatrolArea(Parcel in) {
        AreaID = in.readString();
        AreaName = in.readString();
        AreaRange = in.readString();
    }

}
