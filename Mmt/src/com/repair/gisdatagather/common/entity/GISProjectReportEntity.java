package com.repair.gisdatagather.common.entity;

import com.repair.zhoushan.entity.FlowNodeMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by liuyunfan on 2016/3/31.
 */
public class GISProjectReportEntity {
    public int IsUpdate = 0;//监控端是否提交到gis服务器了
    public String 坐标位置 = "0,0";//取拾取点的位置
    public String 外包矩形 = "";//所有管点和管线的外包矩形
    public int 是否采集完成 = 0;
    public String 工程名称 = "";
    public String 是否导出过点线表 = "否";

    public ArrayList<FlowNodeMeta.TableValue> convert2TableValue() {
        ArrayList<FlowNodeMeta.TableValue> TableValues = new ArrayList<FlowNodeMeta.TableValue>();
        FlowNodeMeta flowNodeMeta = new FlowNodeMeta();

        Field[] fields = this.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            String value = "";
            try {
                value = "" + fields[i].get(this);
            } catch (Exception ex) {
                value = "";
            }
            TableValues.add(flowNodeMeta.new TableValue(name, value));
        }
        return TableValues;
    }

    public GISProjectReportEntity(GISDataProject gisDataProject) {
        this.IsUpdate = gisDataProject.IsUpdate;
        this.坐标位置 = gisDataProject.positionxy;
        this.外包矩形 = gisDataProject.BoundRect;
        this.是否采集完成 = gisDataProject.IsGatherFinish;
        this.工程名称 = gisDataProject.ProjectName;
        this.是否导出过点线表 = gisDataProject.IsExportedPointLineTable;
    }
}
