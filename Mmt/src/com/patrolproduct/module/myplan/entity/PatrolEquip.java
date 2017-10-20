package com.patrolproduct.module.myplan.entity;

import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.SessionManager;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.Hashtable;

public class PatrolEquip implements Parcelable {
    public String EquipID;
    public String EquipEntity;
    public String EquipType;
    public String EquipPos;
    public String EquipArea;

    // 设备巡检绘制设备点
    public void draw(MapView mapView, PatrolTask patrolTask) {
        try {
            String[] layerList = EquipType.split(",");
            String[] entityList = EquipEntity.split("\\|");
            String[] positionList = EquipPos.split("\\|");

            int index = 0;

            for (int i = 0; i < layerList.length; i++) {
                if (layerList[i] == null) {
                    continue;
                }

                String[] entities = entityList[i].split(",");
                String[] positions = positionList[i].split("\\*");

                for (int j = 0; j < positions.length; j++) {
                    Dot dot = new Dot(Double.valueOf(positions[j].split(",")[0]), Double.valueOf(positions[j].split(",")[1]));

                    final PatrolDevice device = new PatrolDevice();

                    device.TaskId = Integer.valueOf(patrolTask.TaskID);
                    device.FlowId = Integer.valueOf(patrolTask.PlanFlowID);

                    device.LayerName = layerList[i];

                    // 类似这种FM010776,, 经过split后只会生成长度为1的数组 [FM010776],空的不在数组中
                    device.PipeNo = j < entities.length ? entities[j] : "";

                    device.X = dot.getX();
                    device.Y = dot.getY();

                    device.Index = index++;

//                    PatrolDevice deviceDB = device.fromDB();
//
//                    if (deviceDB != null) {
//                        device.IsArrived = deviceDB.IsArrived;
//                        device.ArrivedDate = deviceDB.ArrivedDate;
//                        device.IsFeedbacked = deviceDB.IsFeedbacked;
//                        device.FeedbackDate = deviceDB.FeedbackDate;
//
//                        device.Type = "Point";
//
//                        DatabaseHelper.getInstance().update(PatrolDevice.class, device.generateContentValues(), "id=" + deviceDB.ID);
//                    }

                    for (Hashtable<String, String> kv : SessionManager.taskStateTable) {
                        if (device.TaskId == Integer.valueOf(kv.get("taskId")) && device.PipeNo.equals(kv.get("pipeId"))
                                && device.LayerName.equals(kv.get("layerName"))) {
                            device.IsArrived = kv.get("isArrive").equals("1");
                            device.IsFeedbacked = kv.get("isFeedback").equals("1");
                            device.Type = "Point";

                            break;
                        }
                    }

                    MyApplication.getInstance().executeSingleExecutorService(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (device.fromDB() != null)
                                    DatabaseHelper.getInstance().update(PatrolDevice.class, device.generateContentValues(), device.getUid());
                                else
                                    DatabaseHelper.getInstance().insert(device);

                                BaseClassUtil.loge(this, "完成设备点更新到数据库-" + device.getUid());
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

                    MmtAnnotation mmtAnnotation = new MmtAnnotation("", device.LayerName, device.PipeNo, dot, BitmapFactory.decodeResource(
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PatrolPipe createPatrolPipe() {
        PatrolPipe patrolPipe = new PatrolPipe();
        patrolPipe.EquipArea = EquipArea;
        patrolPipe.EquipEntity = EquipEntity;
        patrolPipe.EquipID = EquipID;
        patrolPipe.EquipPos = EquipPos;
        patrolPipe.EquipType = EquipType;
        return patrolPipe;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(EquipID);
        out.writeString(EquipEntity);
        out.writeString(EquipType);
        out.writeString(EquipPos);
        out.writeString(EquipArea);
    }

    public static final Parcelable.Creator<PatrolEquip> CREATOR = new Parcelable.Creator<PatrolEquip>() {
        @Override
        public PatrolEquip createFromParcel(Parcel in) {
            return new PatrolEquip(in);
        }

        @Override
        public PatrolEquip[] newArray(int size) {
            return new PatrolEquip[size];
        }
    };

    public PatrolEquip() {
    }

    private PatrolEquip(Parcel in) {
        EquipID = in.readString();
        EquipEntity = in.readString();
        EquipType = in.readString();
        EquipPos = in.readString();
        EquipArea = in.readString();
    }

}
