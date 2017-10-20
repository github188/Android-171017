package com.patrolproduct.module.myplan.entity;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.MyPlanUtil;
import com.patrolproduct.module.myplan.SessionManager;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.Hashtable;

public class PatrolPipe extends PatrolEquip {

    public String EquipID;
    public String EquipEntity;
    public String EquipType;
    public String EquipPos;
    public String EquipArea;

    public PatrolPipe() {
    }


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

                // 492994.044024,3521352.508073-493009.399818,3521377.989789-493042.920557,3521360.347295
                String[] linePositions = positionList[i].split("\\*");

                for (int j = 0; j < entities.length; j++) {
                    GraphicPolylin polylin = new GraphicPolylin();
                    polylin.setColor(mapView.getResources().getColor(R.color.progressbar_blue));
                    polylin.setLineWidth(10);

                    String[] positions = linePositions[j].split("-");

                    for (String position : positions) {
                        Dot dot = new Dot(Double.valueOf(position.split(",")[0]), Double.valueOf(position.split(",")[1]));
                        polylin.appendPoint(dot);
                    }

                    polylin.setAttributeValue("$计划类型$", MyPlanUtil.PLAN_PIPE_PID + "");
                    polylin.setAttributeValue("$TaskId$", patrolTask.TaskID + "");
                    polylin.setAttributeValue("$LayerName$", layerList[i] + "");
                    polylin.setAttributeValue("$PipeNo$", entities[j] + "");

                    final PatrolDevice device = new PatrolDevice();

                    device.TaskId = Integer.valueOf(patrolTask.TaskID);
                    device.FlowId = Integer.valueOf(patrolTask.PlanFlowID);

                    device.LayerName = layerList[i];
                    device.PipeNo = entities[j];
                    device.X = polylin.getCenterPoint().getX();
                    device.Y = polylin.getCenterPoint().getY();

                    device.Index = index++;

//                    PatrolDevice deviceDB = device.fromDB();
//
//                    if (deviceDB != null) {
//                        device.IsArrived = deviceDB.IsArrived;
//                        device.ArrivedDate = deviceDB.ArrivedDate;
//                        device.IsFeedbacked = deviceDB.IsFeedbacked;
//                        device.FeedbackDate = deviceDB.FeedbackDate;
//
//                        device.Type = "Line";
//                        device.DotsStr = new Gson().toJson(polylin.getPoints(), new TypeToken<Dot[]>() {
//                        }.getType());
//
//                        DatabaseHelper.getInstance().update(PatrolDevice.class, device.generateContentValues(), "id=" + deviceDB.ID);
//                    }

                    for (Hashtable<String, String> kv : SessionManager.taskStateTable) {
                        if (device.TaskId == Integer.valueOf(kv.get("taskId")) && device.PipeNo.equals(kv.get("pipeId"))
                                && device.LayerName.equals(kv.get("layerName"))) {
                            device.IsArrived = kv.get("isArrive").equals("1");
                            device.IsFeedbacked = kv.get("isFeedback").equals("1");
                            device.Type = "Line";

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

                                BaseClassUtil.logi(this, "完成设备点更新到数据库-" + device.getUid());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                    if (device.IsArrived) {
                        polylin.setColor(mapView.getResources().getColor(R.color.progressbar_orange));
                    }

                    mapView.getGraphicLayer().addGraphic(polylin);
                }
            }

            mapView.refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
