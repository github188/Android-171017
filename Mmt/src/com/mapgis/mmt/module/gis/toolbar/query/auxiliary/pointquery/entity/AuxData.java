package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity;

import java.util.List;

/**
 * Created by liuyunfan on 2016/4/13.
 * <p>
 * 点击查询附属数据adapter数据
 */
public class AuxData {
    public String name;
    public List<String> displayFieldList;
    public List<AuxDic> attributes;

    public AuxData(List<String> displayFieldList, List<AuxDic> attributes, String name) {
        this.displayFieldList = displayFieldList;
        this.attributes = attributes;
        this.name = name;
    }
}
