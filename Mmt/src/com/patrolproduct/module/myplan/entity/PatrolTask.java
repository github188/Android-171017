package com.patrolproduct.module.myplan.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Rect;

import java.util.LinkedHashMap;

public class PatrolTask implements Parcelable {
    public String TaskID;
    public String PlanFlowID;
    public String CaseNo;
    public String StartTime;
    public String EndTime;
    public String ArriveState;
    public String FeedBackState;
    public String Desc;
    public PatrolPlan PlanInfo;

    public Rect draw(MapView mapView) {
        return PlanInfo.draw(mapView, this);
    }

    public PatrolTask() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(TaskID);
        out.writeString(PlanFlowID);
        out.writeString(CaseNo);
        out.writeString(StartTime);
        out.writeString(EndTime);
        out.writeString(ArriveState);
        out.writeString(FeedBackState);
        out.writeString(Desc);
        out.writeParcelable(PlanInfo, flags);
    }

    public static final Parcelable.Creator<PatrolTask> CREATOR = new Parcelable.Creator<PatrolTask>() {
        @Override
        public PatrolTask createFromParcel(Parcel in) {
            return new PatrolTask(in);
        }

        @Override
        public PatrolTask[] newArray(int size) {
            return new PatrolTask[size];
        }
    };

    private PatrolTask(Parcel in) {
        TaskID = in.readString();
        PlanFlowID = in.readString();
        CaseNo = in.readString();
        StartTime = in.readString();
        EndTime = in.readString();
        ArriveState = in.readString();
        FeedBackState = in.readString();
        Desc = in.readString();
        PlanInfo = in.readParcelable(PatrolPlan.class.getClassLoader());
    }

    public LinkedHashMap<String, String> toLinkedHashMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("任务编号", TaskID);
        map.put("流程编号", PlanFlowID);
        map.put("开始时间", StartTime);
        map.put("结束时间", EndTime);

        map.put("计划编号", PlanInfo.PlanID);
        map.put("计划名称", PlanInfo.PlanName);
        map.put("计划类型", PlanInfo.PlanType);

        map.put("区域名称", PlanInfo.PArea.AreaName);
//		map.put("路径名称", PlanInfo.PPath.PathName);

//		map.put("设备实体", PlanInfo.PEquip.EquipEntity);
        map.put("设备类型", PlanInfo.PEquip.EquipType);

        map.put("任务描述", Desc);

        return map;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof PatrolTask && (((PatrolTask) o).TaskID.equals(this.TaskID));
    }
}
