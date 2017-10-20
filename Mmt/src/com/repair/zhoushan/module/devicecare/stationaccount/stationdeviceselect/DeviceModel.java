package com.repair.zhoushan.module.devicecare.stationaccount.stationdeviceselect;

import android.text.TextUtils;

import com.repair.zhoushan.module.devicecare.TableColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备管理模型
 */
public class DeviceModel {

    public DeviceModel() {
        ID = "";
        DeviceName = "";
        DeviceCode = "";
        BizName = "";
        AccountTable = "";
        WebRow = new ArrayList<TableColumn>();
    }

    /**
     * 记录ID
     */
    public String ID;

    /**
     * 场站名称
     */
    public String StationName;

    /**
     * 场站类型
     */
    public String StationType;

    /**
     * 设备名称
     */
    public String DeviceName;

    /**
     * 设备类型
     */
    public String DeviceType;

    /**
     * 设备编码
     */
    public String DeviceCode;

    /**
     * 部件名称
     */
    public String PartName;

    /**
     * 部件类型
     */
    public String PartType;

    /**
     * 业务类型
     */
    public String BizName;

    /**
     * 设备台账表
     */
    public String AccountTable;

    /**
     * 台账字段集
     */
    public String AccountFields;

    /**
     * 台账显示字段集
     */
    public String AccountShowFields;

    /**
     * GIS图层
     */
    public String GisLayers;

    /**
     * GIS条件
     */
    public String GisCondition;

    /**
     * 父设备名称
     */
    public String ParentDevice;

    /**
     * 设备列表
     */
    public List<TableColumn> WebRow;

    /**
     * 手持显示列表
     */
    public List<TableColumn> MobileRow;

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
