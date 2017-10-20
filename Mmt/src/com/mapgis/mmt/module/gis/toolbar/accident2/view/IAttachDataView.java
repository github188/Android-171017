package com.mapgis.mmt.module.gis.toolbar.accident2.view;

import java.util.Map;

/**
 * Created by Comclay on 2017/5/17.
 */

public interface IAttachDataView {
    void prePage();

    void nextPage();

    void refreshData(Map<String, String> attMap);

    void showLoadProgress();

    void loadSuccess();

    void loadError(String msg);

    void showEmptyView();

    void showDialog(String msg);

    void hidenDialog();

    void exportXls();

    void exportSuccess(String xlsPath);

    void exportFailed(String msg);

    void viewExportXls(String xlsPath);
}
