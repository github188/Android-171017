package com.repair.zhoushan.module.tablecommonhand;

/**
 * Created by liuyunfan on 2016/7/15.
 */
public class TableMode {

    public String tableName = "";

    /**
     * 该记录所处的组名
     */
    public String uiGroup = "";

    public int ID = -1;
    public String key = "";
    public String value = "";

    public int viewMode =-1;

    public TableMode() {
    }

    public TableMode(String tableName, int ID, int viewMode) {
        this.tableName = tableName;
        this.ID = ID;
        this.viewMode = viewMode;
        this.key = "id";
        this.value = String.valueOf(ID);
    }

    public TableMode(String tableName, String key, String value, int viewMode) {
        this.tableName = tableName;
        this.viewMode = viewMode;
        this.key = key;
        this.value = value;
    }

}
