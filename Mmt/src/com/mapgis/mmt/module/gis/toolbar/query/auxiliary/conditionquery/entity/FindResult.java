package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity;


import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by liuyunfan on 2016/4/14.
 */
public class FindResult {

    /// <summary>
    /// 字段显示名称
    /// </summary>
    public String displayFieldName;

    /// <summary>
    /// 字段别名表
    /// </summary>
    //public Att_Stru fieldAliases;

    public LinkedHashMap<String, String> fieldAliases;

    /// <summary>
    /// 几何类型
    /// </summary>
    public String geometryType;


    /// <summary>
    /// 几何类型  对前端没有意义
    /// </summary>
    //public SpatialReference spatialReference;

    /// <summary>
    /// 属性列表
    /// </summary>
    public Feature[] features;


    /// <summary>
    /// 属性列表
    /// </summary>
    public String displayFieldList;


    /// <summary>
    // 图层序号
    /// </summary>
    public String layerId;

    public String layerName;

    public List<MapGISField> fields;

    public void setFindResult(FindResult findResult){
        this.displayFieldName=findResult.displayFieldName;
        this.fieldAliases=findResult.fieldAliases;
        this.geometryType=findResult.geometryType;
        this.features=findResult.features;
        this.displayFieldList=findResult.displayFieldList;
        this.layerId=findResult.layerId;
        this.layerName=findResult.layerName;
        this.fields=findResult.fields;
    }

    /**
     * webgis2.0 获取当个数据的坐标信息
     * @param context
     * @param layerID
     * @param strOID
     * @param bRecord
     * @param auxTableName
     * @param listener
     */
    public void getSingleFindResult(Context context, final String layerID, final String strOID, final String bRecord, final String auxTableName, final AuxUtils.AfterOnsucess listener) {
        new MmtBaseTask<Void, Void, String>(context, true) {
            @Override
            protected String doInBackground(Void... params) {
                return NetUtil.executeHttpGet(AuxUtils.getBaseUrl() + layerID + "/DevQueryByAuxInfo", "strOID", strOID, "bRecord", bRecord, "auxTableName", auxTableName);
            }

            @Override
            protected void onSuccess(String s) {
                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(context, "附属数据坐标查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                //webgis2.0 需要替换掉\
                s = s.replace("\\", "");
                FindResult temp = new Gson().fromJson(s, FindResult.class);
                if (temp == null) {
                    Toast.makeText(context, "附属数据坐标解析错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (temp.features!=null&&temp.features.length!=1) {
                    Toast.makeText(context, "附属数据坐标查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                setFindResult(temp);

                if (listener != null) {
                    listener.afterSucess();
                }
            }
        }.mmtExecute();
    }

}
