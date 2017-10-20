package com.mapgis.mmt.module.gis.toolbar.accident2.model;

import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;

/**
 * Created by Comclay on 2017/3/2.
 * 某一类型的分析结果数据操作模型
 */

public interface IFeatureItemModel {
    /**
     * 加载一个组中的所有数据
     * @param group
     * @param callback
     */
    void loadFeatureMetaGroup(FeatureMetaGroup group, LoadCallback callback);

    boolean loadFeatureMetaItem(FeatureMetaGroup group, LoadCallback callback, int i);

    void stopAllTask();

    interface LoadCallback {
        void onLoadSuccess(FeatureMetaGroup group, int index);

        void onLoadFailed(String msg);
    }
}
