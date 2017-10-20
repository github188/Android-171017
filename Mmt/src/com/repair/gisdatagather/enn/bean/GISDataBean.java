package com.repair.gisdatagather.enn.bean;

import com.repair.zhoushan.entity.FlowNodeMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class GISDataBean {

    public String Operation = "";
    public String GeomType = "";
    public String OldGeom = "";
    public String OldAtt = "";
    public String NewGeom = "";
    public String NewAtt = "";
    public String LayerName = "";
    public String FieldName = "";
    public String FieldValue = "";
    public int IsUpdate = 0;//监控端是否提交到gis服务器了
    public String 坐标位置 = "";//取拾取点的位置


//    public ArrayList<FlowNodeMeta.FieldSchema> convert2schemas() {
//        ArrayList<FlowNodeMeta.FieldSchema> schemas = new ArrayList<FlowNodeMeta.FieldSchema>();
//        FlowNodeMeta flowNodeMeta = new FlowNodeMeta();
//
//        Field[] fields = this.getClass().getDeclaredFields();
//        for (int i = 0; i < fields.length; i++) {
//            FlowNodeMeta.FieldSchema schema = flowNodeMeta.new FieldSchema();
//            String name = fields[i].getName();
//            String value = "";
//            try {
//                value = "" + fields[i].get(this);
//            } catch (Exception ex) {
//
//            }
//            schema.FieldName = name;
//            schema.PresetValue = value;
//            schemas.add(schema);
//        }
//        return schemas;
//    }

    public ArrayList<FlowNodeMeta.TableValue> convert2TableValue() {
        ArrayList<FlowNodeMeta.TableValue> TableValues = new ArrayList<FlowNodeMeta.TableValue>();
        FlowNodeMeta flowNodeMeta = new FlowNodeMeta();

        Field[] fields = this.getClass().getDeclaredFields();
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
//
//    public GISDataBean convertFromGraphic(Graphic graphic) {
//        // GISDataBean gisDataBean = new GISDataBean();
//        OldGeom = graphic.getCenterPoint().toString();
//        FieldName = "编号";
//        FieldValue = graphic.getAttributeValue("编号");
//        IsUpdate = 0;
//        LayerName = graphic.getAttributeValue("$图层名称$");
//        Operation = "编辑";
//        // graphic.getGraphicType().
//        // gisDataBean.NewGeom = centerPoint.toString();
//        GpsXYZ gpsXYZdot = GpsReceiver.getInstance().getLastLocalLocation();
//        坐标位置 = gpsXYZdot.getX() + "," + gpsXYZdot.getY();
//        return this;
//    }

    public void resetValue() {
        Operation = "";
        GeomType = "";
        OldGeom = "";
        OldAtt = "";
        NewGeom = "";
        NewAtt = "";
        LayerName = "";
        FieldName = "";
        FieldValue = "";
        IsUpdate = 0;//监控端是否提交到gis服务器了
        坐标位置 = "";//取拾取点的位置
    }

}
