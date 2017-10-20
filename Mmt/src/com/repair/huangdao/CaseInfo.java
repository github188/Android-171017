package com.repair.huangdao;

public class CaseInfo {
    public CaseInfo() {
        userID = "";
        userTrueName = "";
        stepID = 0;
        CaseNO = "";
        flowID = 0;
        activeID = 0;
        activeName = "";
        nextActiveID = "";
        undertakeman = "";
        opinion = "";
        direction = 1;
        caseName = "";
        CaseID = "";
        IsOver = 0;
        nextActiveName = "";
        IsAssist = 0;
    }

    /// <summary>
    /// 案卷ID
    /// </summary>
    public String CaseID;

    /// <summary>
    /// 案卷办理人ID
    /// </summary>
    public String userID;

    /// <summary>
    /// 案卷办理人名称
    /// </summary>
    public String userTrueName;

    /// <summary>
    /// 案卷节点ID
    /// </summary>
    public int stepID;

    /// <summary>
    /// 案卷流程编码
    /// </summary>
    public String CaseNO;

    /// <summary>
    /// 案卷流程ID
    /// </summary>
    public int flowID;

    /// <summary>
    ///
    /// </summary>
    public int activeID;

    /// <summary>
    /// 办理节点名称
    /// </summary>
    public String activeName;

    /// <summary>
    /// 下一节点ID
    /// </summary>
    public String nextActiveID;

    /// <summary>
    /// 承办人
    /// </summary>
    public String undertakeman;

    /// <summary>
    /// 交办意见
    /// </summary>
    public String opinion;

    /// <summary>
    /// 1:Forward
    /// -1:Back
    /// 0:Local
    /// </summary>
    public int direction;

    /// <summary>
    /// 案卷名称
    /// </summary>
    public String caseName;

    // 是否结束
    public int IsOver;

    //
    public String nextActiveName;

    // 是否协助
    public int IsAssist;
}
