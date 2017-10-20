package com.repair.entity;

/**
 * 工单退单记录model 对应Table CIV_GD_CASEBACK
 */
public class CaseBackInfo {
    /// <summary>
    /// 工单退单记录ID
    /// </summary>
    public String ID;
    /// <summary>
    /// 工单ID
    /// </summary>
    public String CaseID;
    /// <summary>
    /// 流程节点
    /// </summary>
    public String ActiveName;
    /// <summary>
    /// 退单人
    /// </summary>
    public String BackMan;
    /// <summary>
    /// 退单部门
    /// </summary>
    public String BackManDepart;
    /// <summary>
    /// 退单时间
    /// </summary>
    public String BackTime;
    /// <summary>
    /// 退单原因
    /// </summary>
    public String Reason;
}
