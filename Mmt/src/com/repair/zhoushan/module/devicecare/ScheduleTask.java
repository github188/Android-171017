package com.repair.zhoushan.module.devicecare;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 设备养护任务模型
 */
public class ScheduleTask implements Parcelable {

    public ScheduleTask() {
        ID = 0;
        UserID = "";
        UserName = "";
        BizName = "";
        GisCode = "";
        DeviceCode = "";
        Position = "";
        BizAccountTable = "";
        AccountFields = "";
        BizTaskTable = "";
        TaskFields = "";
        BizFeedBackTable = "";
        TaskCode = "";
        IsDelay = 0;
        IsDrawBack = 0;
        Area = "";
        PlanCureType = "";
        RelateEvent = "";
        WebRow = new ArrayList<>();
        MobileRow = new ArrayList<>();

        PreCodeFormat = "";
        GisLayer = "";
        EquipmentType = "";
    }

    /**
     * 自增ID
     */
    public int ID;

    /**
     * 用户ID
     */
    public String UserID;

    /**
     * 用户名称
     */
    public String UserName;

    /**
     * 业务名称
     */
    public String BizName;

    /**
     * 设备GIS编号
     */
    public String GisCode;

    /**
     * 编号
     */
    public String DeviceCode;

    /**
     * 坐标位置
     */
    public String Position;

    /**
     * 业务台账表
     */
    public String BizAccountTable;

    /**
     * 台账字段集
     */
    public String AccountFields;

    /**
     * 业务任务表
     */
    public String BizTaskTable;

    /**
     * 任务字段集
     */
    public String TaskFields;

    /**
     * 业务反馈表
     */
    public String BizFeedBackTable;

    /**
     * 任务编号
     */
    public String TaskCode;

    /**
     * 能否延期
     */
    public int IsDelay;

    /**
     * 能否退单
     */
    public int IsDrawBack;

    /**
     * 区域
     */
    public String Area;

    /**
     * 前端列表项
     */
    public List<TableColumn> WebRow;

    /**
     * 手持列表项
     */
    public List<TableColumn> MobileRow;

    /**
     * 计划养护类型
     */
    public String PlanCureType;

    /**
     * 是否反馈
     */
    public String IsFeedBack;

    /**
     * 反馈类型
     */
    public String FeedBackType;

    /**
     * 任务编码前缀
     */
    public String PreCodeFormat;

    /**
     * GIS图层名
     */
    public String GisLayer;

    /**
     * 反馈ID
     */
    public String FbId;

    /**
     * 设备类型
     */
    public String EquipmentType;

    /**
     * 能否添加材料
     */
    public int HasMaterial;

    /**
     * 能否添加耗材
     */
    public int HasConsumable;

    /**
     * 能否添加材料
     */
    public int HasCaiGouOrder;

    /**
     * 是否已延期
     */
    public int HasDelay;

    /**
     * 关联事件
     */
    public String RelateEvent;

    protected ScheduleTask(Parcel in) {
        ID = in.readInt();
        UserID = in.readString();
        UserName = in.readString();
        BizName = in.readString();
        GisCode = in.readString();
        DeviceCode = in.readString();
        Position = in.readString();
        BizAccountTable = in.readString();
        AccountFields = in.readString();
        BizTaskTable = in.readString();
        TaskFields = in.readString();
        BizFeedBackTable = in.readString();
        TaskCode = in.readString();
        IsDelay = in.readInt();
        IsDrawBack = in.readInt();
        Area = in.readString();
        WebRow = in.createTypedArrayList(TableColumn.CREATOR);
        MobileRow = in.createTypedArrayList(TableColumn.CREATOR);
        PlanCureType = in.readString();
        IsFeedBack = in.readString();
        FeedBackType = in.readString();
        PreCodeFormat = in.readString();
        GisLayer = in.readString();
        FbId = in.readString();
        EquipmentType = in.readString();
        HasMaterial = in.readInt();
        HasConsumable = in.readInt();
        HasCaiGouOrder = in.readInt();
        HasDelay = in.readInt();
        RelateEvent = in.readString();
    }

    public static final Creator<ScheduleTask> CREATOR = new Creator<ScheduleTask>() {
        @Override
        public ScheduleTask createFromParcel(Parcel in) {
            return new ScheduleTask(in);
        }

        @Override
        public ScheduleTask[] newArray(int size) {
            return new ScheduleTask[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeString(UserID);
        dest.writeString(UserName);
        dest.writeString(BizName);
        dest.writeString(GisCode);
        dest.writeString(DeviceCode);
        dest.writeString(Position);
        dest.writeString(BizAccountTable);
        dest.writeString(AccountFields);
        dest.writeString(BizTaskTable);
        dest.writeString(TaskFields);
        dest.writeString(BizFeedBackTable);
        dest.writeString(TaskCode);
        dest.writeInt(IsDelay);
        dest.writeInt(IsDrawBack);
        dest.writeString(Area);
        dest.writeTypedList(WebRow);
        dest.writeTypedList(MobileRow);
        dest.writeString(PlanCureType);
        dest.writeString(IsFeedBack);
        dest.writeString(FeedBackType);
        dest.writeString(PreCodeFormat);
        dest.writeString(GisLayer);
        dest.writeString(FbId);
        dest.writeString(EquipmentType);
        dest.writeInt(HasMaterial);
        dest.writeInt(HasConsumable);
        dest.writeInt(HasCaiGouOrder);
        dest.writeInt(HasDelay);
        dest.writeString(RelateEvent);
    }

    /**
     * 本地将 WebRow 中的 TableColumn存储在一个 Map中便于按名查找
     */
    public HashMap<String, String> columnList = new HashMap<String, String>();

    public Dot mDot;

    public String getColumnValueByName(String name) {

        String value = "";

        if (TextUtils.isEmpty(name)) {
            return value;
        }

        if (columnList.size() == 0) {

            if (WebRow != null && WebRow.size() > 0) {
                for (TableColumn tableColumn : WebRow) {
                    columnList.put(tableColumn.FieldName, tableColumn.FieldValue);
                }
            }

            if (MobileRow != null && MobileRow.size() > 0) {
                for (TableColumn tableColumn : MobileRow) {
                    columnList.put(tableColumn.FieldName, tableColumn.FieldValue);
                }
            }
        }

        if (columnList.containsKey(name)) {
            value = columnList.get(name);
        }

        return value;
    }

    public int getColumnIndexFromWebRow(String name) {

        int index = -1;

        if (TextUtils.isEmpty(name)) {
            return index;
        }

        if (WebRow != null && WebRow.size() > 0) {

            int webRowLength = WebRow.size();
            for (int i = 0; i < webRowLength; i++) {
                if (name.equals(WebRow.get(i).FieldName)) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }
}