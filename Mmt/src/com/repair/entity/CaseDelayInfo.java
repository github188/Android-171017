package com.repair.entity;

/**
 * 工单延期处理记录model 对应Table CIV_GD_CASEDELAY
 */
public class CaseDelayInfo {
    /// <summary>
    /// 工单延期记录ID
    /// </summary>
    public String ID;
    /// <summary>
    /// 工单ID
    /// </summary>
    public String CaseID;
    /// <summary>
    /// 延期申请人
    /// </summary>
    public String ApplyMan;
    /// <summary>
    /// 延期申请单位
    /// </summary>
    public String ApplyGroup;
    /// <summary>
    /// 延期申请时间
    /// </summary>
    public String ApplyTime;
    /// <summary>
    /// 申请完成时间
    /// </summary>
    public String ApplyFinishTime;
    /// <summary>
    /// 审核人
    /// </summary>
    public String VerifyMan;
    /// <summary>
    /// 审核人单位
    /// </summary>
    public String VerifyGroup;
    /// <summary>
    /// 审核时间
    /// </summary>
    public String VerifyTime;
    /// <summary>
    /// 申请状态
    /// </summary>
    public String State;
    /// <summary>
    /// 申请原因
    /// </summary>
    public String Reason;
}