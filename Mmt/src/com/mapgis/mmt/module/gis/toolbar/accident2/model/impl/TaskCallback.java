package com.mapgis.mmt.module.gis.toolbar.accident2.model.impl;

/**
 * Created by Comclay on 2017/5/17.
 */

public interface TaskCallback {
    void onPreExecute();

    void onSuccess();

    void onFailed(String msg);
}
