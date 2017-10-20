package com.mapgis.mmt.module.gis.toolbar.accident2.util;

import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureMetaModel;

/**
 * Created by Comclay on 2017/3/2.
 * 根据civFeatureMetaType对爆管分析结果进行过滤处理
 */

public interface FeatureFilter{
    void filter(IFeatureMetaModel featureMetaModel);
}
