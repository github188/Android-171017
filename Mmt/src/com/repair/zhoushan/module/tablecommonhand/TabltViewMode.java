package com.repair.zhoushan.module.tablecommonhand;

/**
 * Created by liuyunfan on 2016/7/15.
 */
public enum TabltViewMode {
    READ(1),
    EDIT(2),
    DELETE(3),
    REPORT(4),
    EDIT_DELETE(5);
    private int tableViewMode;

    TabltViewMode(int tableViewMode) {
        this.tableViewMode = tableViewMode;
    }

    public int getTableViewMode() {
        return this.tableViewMode;
    }
}
