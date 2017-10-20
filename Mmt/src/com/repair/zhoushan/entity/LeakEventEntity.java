package com.repair.zhoushan.entity;

/**
 * 手机端检漏事件模型
 */
public class LeakEventEntity {

    public LeakEventEntity() {
        ID = 0;
        EventCode = "";
        EventSource = "";
        EventState = "";
        EventType = "";
        EventClass = "";
        DistrictName = "";
        Address = "";
        Description = "";
        Images = "";
        Audios = "";
        ReporterID = "";
        ReporterName = "";
        ReporterGroup = "";
        ReportTime = "";
        Position = "";
        LeakCode = "";
        PipeSize = "";
        PipeAge = "";
        PipeMaterial = "";
        PipeHeight = "";
        PipeDamage = "";
        GroundMedia = "";
        LeakPlace = "";
        LeakType = "";
        EmergencyLevel = "";
        DetectMethod = "";
        DetectEquipment = "";

        LeakExceptionDescript = "";
        DigVerifyDescript = "";
        Exist = 1;

        LayerName = "";
        FieldName = "";
        FieldValue = "";
    }

    /**
     * 事件主键ID
     */
    public int ID;
    /**
     * 事件编号
     */
    public String EventCode;
    /**
     * 事件来源
     */
    public String EventSource;
    /**
     * 事件状态
     */
    public String EventState;
    /**
     * 事件类型
     */
    public String EventType;
    /**
     * 事件类别
     */
    public String EventClass;
    /**
     * 所属区域
     */
    public String DistrictName;
    /**
     * 事件地址
     */
    public String Address;
    /**
     * 事件描述
     */
    public String Description;
    /**
     * 现场图片
     */
    public String Images;
    /**
     * 现场录音
     */
    public String Audios;
    /**
     * 上报人ID
     */
    public String ReporterID;
    /**
     * 上报人姓名
     */
    public String ReporterName;
    /**
     * 上报部门
     */
    public String ReporterGroup;
    /**
     * 上报时间
     */
    public String ReportTime;
    /**
     * 地图位置
     */
    public String Position;
    /**
     * 漏点编号
     */
    public String LeakCode;
    /**
     * 管径
     */
    public String PipeSize;
    /**
     * 管材
     */
    public String PipeMaterial;
    /**
     * 管道埋设年代
     */
    public String PipeAge;
    /**
     * 管道埋深
     */
    public String PipeHeight;
    /**
     * 管道破损形态
     */
    public String PipeDamage;
    /**
     * 地面介质
     */
    public String GroundMedia;
    /**
     * 漏点位置
     */
    public String LeakPlace;
    /**
     * 漏水类型(名漏/暗漏)
     */
    public String LeakType;
    /**
     * 探测方法
     */
    public String DetectMethod;
    /**
     * 探测仪器
     */
    public String DetectEquipment;
    /**
     * 紧急程度
     */
    public String EmergencyLevel;
    /**
     * 漏水点异常说明
     */
    public String LeakExceptionDescript;
    /**
     * 开挖验证相关说明
     */
    public String DigVerifyDescript;
    /**
     * 记录是否存在
     */
    public int Exist;

    /**
     * 事件关联的设备的图层名称
     */
    public String LayerName;

    /**
     * 事件关联的设备的字段名称
     */
    public String FieldName;

    /**
     * 事件关联的设备的字段值
     */
    public String FieldValue;

    public void setIdentityField(String[] args) {
        if (args == null) {
            return;
        }

        this.LayerName = args[0];
        this.FieldName = args[1];
        this.FieldValue = args[2];
    }

}
