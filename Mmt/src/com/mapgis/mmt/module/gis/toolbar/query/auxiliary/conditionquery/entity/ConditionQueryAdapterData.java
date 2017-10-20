package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity;

import java.util.HashMap;
import java.util.List;

/**
 * Created by liuyunfan on 2016/4/20.
 */
public class ConditionQueryAdapterData {
    public String layerID;
    public String auxTableName;
    public HashMap<String, String> atts = new HashMap<>();

    //属性别名列表，（在手持上展示时不理会这个限制）
    public HashMap<String, String> fieldAliases;
    //属性是否可见，是否可编辑 （在手持上展示时不理会这个限制）
    public List<MapGISField> fields;

    public ConditionQueryAdapterData(String layerID, String auxTableName, HashMap<String, String> atts, HashMap<String, String> fieldAliases, List<MapGISField> fields) {
        this.layerID = layerID;
        this.auxTableName = auxTableName;
        this.atts = atts;
        this.fieldAliases = fieldAliases;
        this.fields = fields;
    }
}
