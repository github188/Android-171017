package com.mapgis.mmt.module.gis.toolbar.accident2.model;

import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.impl.TaskCallback;

import java.util.Map;

/**
 * Created by Comclay on 2017/5/17.
 * 附属数据模型
 */

public interface IAttachDataModel {

    // 附属数据查询
    void queryAttachData(TaskCallback callback);

    int getSize();

    FeatureGroup getAttData();

    String getAttIds();

    Map<String, String> getAttData(int index);

    boolean isEmpty();

    void cancelTask();

    String getLayerName();

    /**
     * 导出附属数据
     */
    void exportAttData(TaskCallback callback);

    String getXlsPath();

    String getRelationShipTableName();
}
