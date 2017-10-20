package com.mapgis.mmt.module.gis.toolbar.accident2.util;

import android.util.Log;

import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureMetaModel;

/**
 * Created by Comclay on 2017/3/2.
 * 设置默认需要显示的civFeatureMetaType
 */

public class ShowFeatureFilter implements FeatureFilter {
    private static final String TAG = "ShowFeatureFilter";
    // 默认显示的结果类型
    public final static String[] mShowFeatureTypes = {
            "civFeatureMetaTypeIncidentPoint"
            , "civFeatureMetaTypeSwitch"
            , "civFeatureMetaTypePipeLine"
           // , "civFeatureMetaTypeSwieffect"  // 受影响用户
    };

    @Override
    public void filter(IFeatureMetaModel featureMetaModel) {
        FeatureMetaGroup[] featureMetaArray = featureMetaModel.getFeatureMetaArray();
        // 只显示部分设备
        for (FeatureMetaGroup group : featureMetaArray) {
            boolean isShow = false;
            for (String type : mShowFeatureTypes)
                if (type.equals(group.getCivFeatureMetaType())
                        && group.getResultList() != null && group.getResultList().size() != 0
                        ) {
                    isShow = true;
                    break;
                }
            group.setShow(isShow);
            Log.i(TAG, "爆管分析结束之后默认显示: " + group.toString());
        }
    }
}
