package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity;

/**
 * Created by liuyunfan on 2016/4/14.
 */
public class MapGISField {
    /// <summary>
    /// 字段名
    /// </summary>
    public String name;


    /// <summary>
    /// 字段类型
    /// </summary>
    public String type;

    /// <summary>
    /// 字段别名
    /// </summary>
    public String alias;

//
//    /// <summary>
//    /// 字段默认值
//    /// </summary>
//    public String DefVal;
//
//
//
//    /// <summary>
//    /// 数据形态扩展
//    /// </summary>
//    public String ShapeVal;
//
//    /// <summary>
//    /// 长度
//    /// </summary>
//    public String Length;
//
//
//    /// <summary>
//    /// 小数位数
//    /// </summary>
//    public String PointLen;


    /// <summary>
    /// 是否可为空
    /// </summary>
    public boolean nullable;

    public boolean editable;
    public boolean visible;
    public String domain;
}
