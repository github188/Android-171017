package com.repair.entity;

/**
 * 工单维修信息
 */
public class WorkBillProcess {
    public String ID;
    public String CaseID;

    /**
     * 工单处理状态
     */
    public String WorkBillState;

    /**
     * 时间(接单，到场，完工等时间)
     */
    public String Time;

    public String Remark;
    public String Images;
    public String Audios;
    public String ReporterName;
    public int ReporterID;
    public String ReporterDept;
}
