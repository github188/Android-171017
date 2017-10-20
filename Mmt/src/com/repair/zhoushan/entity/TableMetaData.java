package com.repair.zhoushan.entity;

/**
 * 根据表名分类信息
 */
public class TableMetaData {

    public TableMetaData() {
        TableName = "";
        TableAlias = "";
        ExportTemp = "";
        ShowTemp = "";
        CorrectFields = "";
        FlowNodeMeta = new FlowNodeMeta();
    }

    /**
     * 表名
     */
    public String TableName;

    /**
     * 表的别名
     */
    public String TableAlias;

    /**
     * 导出模板
     */
    public String ExportTemp;

    /**
     * 显示模板
     */
    public String ShowTemp;

    /**
     * 可补正字段集
     */
    public String CorrectFields;

    /**
     * 表中的数据结构
     */
    public FlowNodeMeta FlowNodeMeta;
}