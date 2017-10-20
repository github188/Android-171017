package com.repair.gisdatagather.product.projectopt;

/**
 * Created by liuyunfan on 2016/5/4.
 */
public interface ProjectOptInterface {
    void clearProject();

    void delProject();

    void submitProject();

    void lightTodayGisData();

    void autoLinkLine(boolean isAutoLinkLine);
}
