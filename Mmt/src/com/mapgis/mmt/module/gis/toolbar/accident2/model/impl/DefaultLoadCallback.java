package com.mapgis.mmt.module.gis.toolbar.accident2.model.impl;

import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureItemModel;

/**
 * Created by Comclay on 2017/3/3.
 * 数据加载的默认回调接口
 */

public class DefaultLoadCallback implements IFeatureItemModel.LoadCallback{
    @Override
    public void onLoadSuccess(FeatureMetaGroup group, int index) {

    }

    @Override
    public void onLoadFailed(String msg) {

    }
}
