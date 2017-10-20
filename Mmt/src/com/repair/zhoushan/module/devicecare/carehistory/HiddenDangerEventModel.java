package com.repair.zhoushan.module.devicecare.carehistory;

/**
 * 隐患事件信息Model(“工商户安检”存在“隐患事件上报”)
 */
public class HiddenDangerEventModel {

    public HiddenDangerEventModel() {
        ID = "";
        EventCode = "";
        EventType = "";
        EventContent = "";
        Reporter = "";
        ReportTime = "";
        Pic = "";
    }

    /**
     * ID
     */
    public String ID;

    /**
     * 事件编号
     */
    public String EventCode;

    /**
     * 事件类型
     */
    public String EventType;

    /**
     * 事件内容
     */
    public String EventContent;

    /**
     * 上报人名称
     */
    public String Reporter;

    /**
     * 上报时间
     */
    public String ReportTime;

    /**
     * 现场图片
     */
    public String Pic;
}
