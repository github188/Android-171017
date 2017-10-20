package com.repair.zhoushan.module.devicecare.platfromadd;

/**
 * Created by liuyunfan on 2016/7/18.
 */
public class MaintanceConfig {
    /// 任务编码前缀
    public String PreCodeFormat = "";
    /// 计划养护类型
    public String PlanCureType = "";
    // 能否延期
    public int isDelay = 0;
    // 能否退单
    public int isReturn = 0;
    // 能否编辑
    public int isEdit = 0;
    // 能否添加数据
    public int isAddData = 0;
    // 能否同步
    public int isSyn = 0;
    // 能否添加到停气列表
    public int isStopList = 0;
    // 设备类型
    public String EquipmentType = "";
    // 能否添加材料
    public int HasMaterial = 0;
    // 能否添加耗材
    public int HasConsumable = 0;
    //能否GIS分区
    public int IsAreaShow = 0;
}
