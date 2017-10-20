package com.patrolproduct.module.myplan.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.patrolproduct.module.myplan.MyPlanUtil;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Rect;

public class PatrolPlan implements Parcelable {
    public String PlanID;
    public String PlanName;
    public String PlanType;
    public String PType;
    public String IsFeedBack;
    public String PlanCycle;
    public String PlanTypeID;
    public PatrolArea PArea;
    public PatrolPath PPath;
    public PatrolEquip PEquip;

    public Rect draw(MapView mapView, PatrolTask patrolTask) {
        Rect rect = null;

        // 绘制巡检点
        //或区域的关键点
        if (!PEquip.EquipEntity.equals("")) {
            if (Integer.valueOf(PType) == MyPlanUtil.PLAN_PIPE_PID) {// 管线巡检
                PEquip = PEquip.createPatrolPipe();
            }
            PEquip.draw(mapView, patrolTask);
        }
        // 绘制路径,并且得到路径的外界矩形
        if (!PPath.PathRange.equals("")) {
            rect = PPath.draw(mapView, patrolTask);
        }
        // 绘制区域,并且得到区域的外界矩形
        if (!PArea.AreaRange.equals("")) {
            Rect areaRect = PArea.draw(mapView, patrolTask);
            rect = areaRect == null ? rect : areaRect;
        }
        return rect;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(PlanID);
        out.writeString(PlanName);
        out.writeString(PlanType);
        out.writeString(PType);
        out.writeString(IsFeedBack);
        out.writeString(PlanCycle);
        out.writeString(PlanTypeID);
        out.writeParcelable(PArea, flags);
        out.writeParcelable(PPath, flags);
        out.writeParcelable(PEquip, flags);
    }

    public static final Parcelable.Creator<PatrolPlan> CREATOR = new Parcelable.Creator<PatrolPlan>() {
        @Override
        public PatrolPlan createFromParcel(Parcel in) {
            return new PatrolPlan(in);
        }

        @Override
        public PatrolPlan[] newArray(int size) {
            return new PatrolPlan[size];
        }
    };

    private PatrolPlan(Parcel in) {
        PlanID = in.readString();
        PlanName = in.readString();
        PlanType = in.readString();
        PType = in.readString();
        IsFeedBack = in.readString();
        PlanCycle = in.readString();
        PlanTypeID = in.readString();
        PArea = in.readParcelable(PatrolArea.class.getClassLoader());
        PPath = in.readParcelable(PatrolPath.class.getClassLoader());
        PEquip = in.readParcelable(PatrolEquip.class.getClassLoader());
    }

}
