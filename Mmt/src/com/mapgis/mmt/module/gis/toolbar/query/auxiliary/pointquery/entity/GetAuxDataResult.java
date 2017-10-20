package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity;

import java.util.List;

/**
 * Created by liuyunfan on 2016/4/13.
 */
public class GetAuxDataResult {
    /// <summary>
    /// 是否成功
    /// </summary>
    public boolean isSuccess;

    /// <summary>
    /// 操作过程返回的错误信息
    /// </summary>
    public String Msg;

    /// <summary>
    /// 显示的字段集合
    /// </summary>
    public List<String> displayFieldList;

    /// <summary>
    /// 表名
    /// </summary>
    public String tableName;

    /// <summary>
    /// 结果的集合
    /// </summary>
    public List<List<AuxDic>> attributes;

    /// <summary>
    /// 结果总数
    /// </summary>
    public int totalRcdNum;

    public String exportUrl;

//public AuxDataList getAuxData
}

