package com.maintainproduct.v2.caselist;

import com.maintainproduct.entity.MaintainSimpleInfo;

/**
 * 维修工单 数据模型
 */
public class GDItem {

    public String AcceptDepartment;
    public String ActiveID;
    public String ActiveName;
    public String Address;
    public String AppointmentTime;
    public String CID;
    public String CaseCode;
    public String CaseID;
    public String CaseNO;
    public String CaseState;
    public String ContactTel;
    public String Email;
    public String EventID;
    public String EventSource;
    public String ID0;
    public String Level;
    public String LoginMan;
    public String LoginST2;
    public String MessageCode;
    public String MeterState;
    public String MobilePhone;
    public String OccurTime;
    public String Opinion;
    public String Picture;
    public String Position;
    public String Receivetime;
    public String Recording;
    public String Remark;
    public String ReportContent;
    public String ReportDepartment;
    public String ReportForm;
    public String ReportMan;
    public String ReportST;
    public String ReportSource;
    public String ReportType;
    public String State;
    public String ReadGDTime;
    public String UserID;
    public String UserName;
    // 移交方向
    public String Direction;
    public String FlowName;

    // 距离当前坐标距离
    public String Distance;
    // 超过Receivetime的时间
    public String BetTime;

    public String BillCode;
    public String ReportGroup;
    public String DispatchMan;
    public String Description;

    /**
     * 热线上报用户编号
     */
    public String UserCode;
    /**
     * 热线上报用户联系电话
     */
    public String UserTel;
    /**
     * 热线上报用户真实姓名
     */
    public String UserTrueName;

    public MaintainSimpleInfo build() {
        MaintainSimpleInfo info = new MaintainSimpleInfo();
        info.ActiveID = Integer.parseInt(this.ActiveID);
        info.ActiveName = this.ActiveName;
        info.CaseNo = this.CaseNO;
        // info.FlowName = "桂林维修工单";
        info.FlowName = this.FlowName;
        info.ID0 = Integer.parseInt(this.ID0);
        return info;
    }

    /**
     * 关联事件的设备信息
     */
    public String LayerName;
    public String FieldName;
    public String FieldValue;
}
