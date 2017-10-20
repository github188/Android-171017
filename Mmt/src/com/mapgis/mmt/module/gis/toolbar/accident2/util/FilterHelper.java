package com.mapgis.mmt.module.gis.toolbar.accident2.util;

import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureMetaModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Comclay on 2017/3/2.
 * 过滤器
 */

public class FilterHelper implements FeatureFilter {
    private List<FeatureFilter> mFilterList;

    public FilterHelper() {
        this.mFilterList = new ArrayList<>();
        this.mFilterList.add(new ClearFeatureFilter());
        this.mFilterList.add(new ShowFeatureFilter());
    }

    @Override
    public void filter(IFeatureMetaModel featureMetaModel) {
        for (FeatureFilter filter : this.mFilterList) {
            filter.filter(featureMetaModel);
        }
    }
}
