package com.repair.zhoushan.module.devicecare.platfromgislink;

import com.repair.zhoushan.module.devicecare.TableColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/3/23.
 */
public class DeviceModel {

    public DeviceModel() {
        WebRow = new ArrayList<TableColumn>();
        MobileRow = new ArrayList<TableColumn>();
    }

    /// <summary>
    /// 记录ID
    /// </summary>
    public String ID;

    /// <summary>
    /// 设备名称
    /// </summary>
    public String DeviceName;

    /// <summary>
    /// 设备编码
    /// </summary>
    public String DeviceCode;

    /// <summary>
    /// 设备类型
    /// </summary>
    public String DeviceType;

    /// <summary>
    /// 设备台账表
    /// </summary>
    public String AccountTable;

    /// <summary>
    /// 台账字段集
    /// </summary>
    public String AccountFields;

    /// <summary>
    /// 台账显示字段集
    /// </summary>
    public String AccountShowFields;

    /// <summary>
    /// GIS图层
    /// </summary>
    public String GisLayers;

    /// <summary>
    /// GIS条件
    /// </summary>
    public String GisCondition;

    /// <summary>
    /// 父设备名称
    /// </summary>
    public String ParentDevice;

    /// <summary>
    /// 设备列表
    /// </summary>
    public List<TableColumn> WebRow;


    public List<TableColumn> MobileRow;
}
