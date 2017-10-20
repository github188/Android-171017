package com.repair.zhoushan.entity;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/3/15.
 */
public class CrashPointRecord {
    public CrashPointRecord() {
        ID = 0;
        CaseNO = "";
        CrashSafeMethod = "";
        EnvironemntProspect = "";
        ErrorRecord = "";
        CrackDetection = "";
        CrackDetectionReport = "";
        AirTightTest = "";
        FiniteSpaceOxygenTest = "";
        DeepPitOxygenTest = "";
        GasDetect = "";
        DetectRecord = "";
        PressureTest = "";
        TestRecord = "";
        Photos = "";
        CrashPointPosition = "";
        Remark = "";
        RemarkFile = "";
    }

    /// <summary>
    /// ID
    /// </summary>
    public int ID;

    /// <summary>
    /// 工单编号
    /// </summary>
    public String CaseNO;

    /// <summary>
    /// 碰接现场安全措施
    /// </summary>
    public String CrashSafeMethod;

    /// <summary>
    /// 环境勘查
    /// </summary>
    public String EnvironemntProspect;

    /// <summary>
    /// 异常情况记录
    /// </summary>
    public String ErrorRecord;

    /// <summary>
    /// 探伤
    /// </summary>
    public String CrackDetection;

    /// <summary>
    /// 探伤报告
    /// </summary>
    public String CrackDetectionReport;

    /// <summary>
    /// 气密性试验
    /// </summary>
    public String AirTightTest;

    /// <summary>
    /// 有限空间含氧量检测
    /// </summary>
    public String FiniteSpaceOxygenTest;

    /// <summary>
    /// 深坑作业含氧量检测
    /// </summary>
    public String DeepPitOxygenTest;

    /// <summary>
    /// 燃气浓度检测
    /// </summary>
    public String GasDetect;

    /// <summary>
    /// 检测记录
    /// </summary>
    public String DetectRecord;

    /// <summary>
    /// 保压测试
    /// </summary>
    public String PressureTest;

    /// <summary>
    /// 测试记录
    /// </summary>
    public String TestRecord;

    /// <summary>
    /// 现场照片
    /// </summary>
    public String Photos;

    /// <summary>
    /// 碰接点选择
    /// </summary>
    public String CrashPointPosition;

    /// <summary>
    /// 情况说明
    /// </summary>
    public String Remark;

    /// <summary>
    /// 情况说明附件
    /// </summary>
    public String RemarkFile;


    public List<FlowNodeMeta.TableValue> Convert2TableValue() {
        List<FlowNodeMeta.TableValue> vals = new ArrayList<>();
        Field[] fields = this.getClass().getDeclaredFields();
        FlowNodeMeta flowNodeMeta = new FlowNodeMeta();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            String value = "";
            try {
                value = "" + fields[i].get(this);
            } catch (Exception ex) {
                value = "";
            }
            String chinesename = getChineseName(name);
            if (TextUtils.isEmpty(chinesename)) {
                continue;
            }
            vals.add(flowNodeMeta.new TableValue(chinesename, value));
        }
        return vals;
    }

    private String getChineseName(String name) {
        String chineseName = "";
        switch (name) {
            case "RemarkFile": {
                chineseName = "情况说明附件";
            }
            break;
            case "Remark": {
                chineseName = "情况说明";
            }
            break;
            case "CrashPointPosition": {
                chineseName = "碰接点选择";
            }
            break;
            case "Photos": {
                chineseName = "现场照片";
            }
            break;
            case "TestRecord": {
                chineseName = "测试记录";
            }
            break;
            case "PressureTest": {
                chineseName = "保压测试";
            }
            break;
            case "DetectRecord": {
                chineseName = "检测记录";
            }
            break;
            case "GasDetect": {
                chineseName = "燃气浓度检测";
            }
            break;
            case "DeepPitOxygenTest": {
                chineseName = "深坑作业含氧量检测";
            }
            break;
            case "FiniteSpaceOxygenTest": {
                chineseName = "有限空间含氧量检测";
            }
            break;
            case "AirTightTest": {
                chineseName = "气密性试验";
            }
            break;
            case "CrackDetectionReport": {
                chineseName = "探伤报告";
            }
            break;
            case "CrackDetection": {
                chineseName = "探伤";
            }
            break;
            case "ErrorRecord": {
                chineseName = "异常情况记录";
            }
            break;
            case "EnvironemntProspect": {
                chineseName = "环境勘查";
            }
            break;
            case "CrashSafeMethod": {
                chineseName = "碰接现场安全措施";
            }
            break;
            case "CaseNO": {
                chineseName = "工单编号";
            }
            break;
            case "ID": {
                chineseName = "ID";
            }
            break;
        }
        return chineseName;
    }
}
