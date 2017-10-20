package com.mapgis.mmt.module.gis.toolbar.accident2.util;

import android.util.Log;

import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureMetaModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Comclay on 2017/3/2.
 * 过滤掉不需要显示在结果列表中的
 */

public class ClearFeatureFilter implements FeatureFilter {
    private static final String TAG = "ClearFeatureFilter";
    // 默认过滤的类型
    private final static String[] mFilterFeatureTypes = {"civFeatureMetaTypeRegionResult", "civFeatureMetaTypeResstop"};

    @Override
    public void filter(IFeatureMetaModel featureMetaModel) {
        FeatureMetaGroup[] featureMetaArray = featureMetaModel.getFeatureMetaArray();
        List<FeatureMetaGroup> list = new ArrayList<>();
        for (FeatureMetaGroup group : featureMetaArray) {
            boolean isClear = false;
            for (String type : mFilterFeatureTypes) {
                if (type.equals(group.getCivFeatureMetaType())) {
                    isClear = true;
                    break;
                }
            }
            if (!isClear){
                list.add(group);
            }
        }
        FeatureMetaGroup [] groupArray = new FeatureMetaGroup[list.size()];
        featureMetaModel.setFeatureMetaArray(list.toArray(groupArray));
        Log.i(TAG, "清楚后显示的结果有: "+ Arrays.toString(featureMetaModel.getFeatureMetaArray()));
    }
}
