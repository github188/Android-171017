package com.repair.gisdatagather.common.entity;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPolylin;

/**
 * Created by liuyunfan on 2016/2/25.
 */
public class GISDataBeanBase {

    /**
     * id不参与保存到服务器
     */
    private int ID;

    public String Operation = "";
    public String GeomType = "";
    public String OldGeom = "";
    public String OldAtt = "";
    public String NewGeom = "";
    public String NewAtt = "";
    public String LayerName = "";
    public String FieldName = "";
    public String FieldValue = "";
    public String 上报时间 = "";
    public String 更新时间 = "";

    //后来追加，存储本地号
    public String 事件编号 = "";

    public GISDataBeanBase(int ID, String Operation, String GeomType, String OldGeom, String OldAtt, String NewGeom, String NewAtt, String LayerName, String FieldName, String FieldValue, String 更新时间, String 事件编号) {
        this.ID = ID;
        this.Operation = Operation;
        this.GeomType = GeomType;
        this.OldGeom = OldGeom;
        this.OldAtt = OldAtt;
        this.NewGeom = NewGeom;
        this.NewAtt = NewAtt;
        this.LayerName = LayerName;
        this.FieldName = FieldName;
        this.FieldValue = FieldValue;
        this.更新时间 = 更新时间;
        this.事件编号 = 事件编号;
    }

    public int getID() {
        return ID;
    }

//    public ArrayList<FlowNodeMeta.TableValue> convert2TableValue() {
//        ArrayList<FlowNodeMeta.TableValue> TableValues = new ArrayList<FlowNodeMeta.TableValue>();
//        FlowNodeMeta flowNodeMeta = new FlowNodeMeta();
//
//        Field[] fields = this.getClass().getDeclaredFields();
//        for (int i = 0; i < fields.length; i++) {
//            String name = fields[i].getName();
//            String value = "";
//            try {
//                value = "" + fields[i].get(this);
//            } catch (Exception ex) {
//                value = "";
//            }
//            TableValues.add(flowNodeMeta.new TableValue(name, value));
//        }
//        return TableValues;
//    }

    public GISDataBeanBase() {
    }

    public GISDataBeanBase copyFromGISDataBeanBase(GISDataBeanBase gisDataBeanBase) {
        this.Operation = gisDataBeanBase.Operation;
        this.GeomType = gisDataBeanBase.GeomType;
        this.OldGeom = gisDataBeanBase.OldGeom;
        this.OldAtt = gisDataBeanBase.OldAtt;
        this.NewGeom = gisDataBeanBase.NewGeom;
        this.NewAtt = gisDataBeanBase.NewAtt;
        this.LayerName = gisDataBeanBase.LayerName;
        this.FieldName = gisDataBeanBase.FieldName;
        this.FieldValue = gisDataBeanBase.FieldValue;
        this.更新时间 = gisDataBeanBase.更新时间;
        return this;
    }

    public GISDataBeanBase copyNewAttrs2OldAttrs() {
        if (!TextUtils.isEmpty(this.NewAtt)) {
            String[] kvs = GisDataGatherUtils.splitGisVal(this.NewAtt);

            this.OldAtt = GisDataGatherUtils.gisVals2Str(kvs, true);
        }
        return this;
    }

    public GISDataBeanBase(Graphic graphic, String Operation, String LayerName, String GeomType) {
        FieldName = "编号";
        FieldValue = graphic.getAttributeValue("编号");
        if (BaseClassUtil.isNullOrEmptyString(FieldValue)) {
            FieldName = "GUID";
            FieldValue = graphic.getAttributeValue("GUID");
        }
        this.LayerName = LayerName;
        this.Operation = Operation;
        this.GeomType = GeomType;
        if (TextUtils.isEmpty(事件编号)) {
            if (GeomType.equals("管点")) {
                事件编号 = graphic.getAttributeValue("本点号");
            } else {
                事件编号 = graphic.getAttributeValue("本点号") + "-" + graphic.getAttributeValue("上点号");
            }
        }
        StringBuilder strBuilder = new StringBuilder();

        for (int i = 0; i < graphic.getAttributeNum(); i++) {
            String key = graphic.getAttributeName(i);
            if ("GISType".equals(key)) {
                continue;
            }
            if ("flag".equals(key)) {
                continue;
            }
            if ("$图层名称$".equals(key)) {
                continue;
            }


            String value = graphic.getAttributeValue(i);

            strBuilder.append(key + ":" + value + ",");
        }

        if (strBuilder.length() > 0) {
            if ("编辑".equals(Operation)) {
                this.OldAtt = strBuilder.toString().substring(0, strBuilder.length() - 1);
                this.NewAtt=this.OldAtt;
                if ("管点".equals(GeomType)) {
                    this.OldGeom = graphic.getCenterPoint().toString();
                } else {
                    if (graphic instanceof GraphicPolylin) {
                        GraphicPolylin graphicPolylin = (GraphicPolylin) graphic;
                        for (int i = 0; i < graphicPolylin.getPointCount(); i++) {
                            this.OldGeom = this.OldGeom + graphicPolylin.getPoint(i).toString() + "|";
                        }
                        if (this.OldGeom.length() > 0) {
                            this.OldGeom = this.OldGeom.substring(0, this.OldGeom.length() - 1);
                        }
                    }
                }

            } else {
                this.OldAtt="";
                this.NewAtt = strBuilder.toString().substring(0, strBuilder.length() - 1);
            }
        }

    }

    public void deleteFromServer(Context context, MmtBaseTask.OnWxyhTaskListener<String> listener) {
        new MmtBaseTask<Void, Void, String>(context, true, listener) {
            @Override
            protected String doInBackground(Void... params) {
                return NetUtil.executeHttpGet(ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeleteGISData/" + ID);
            }
        }.mmtExecute();
    }

    public void updataGisData(Context context, MmtBaseTask.OnWxyhTaskListener<String> listener) {
        new MmtBaseTask<Void, Void, String>(context, true, listener) {
            @Override
            protected String doInBackground(Void... params) {

                String result = "";
                try {
                    result = NetUtil.executeHttpPost(ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/UpdateGISData", new Gson().toJson(GISDataBeanBase.this));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }
        }.mmtExecute();
    }

}
