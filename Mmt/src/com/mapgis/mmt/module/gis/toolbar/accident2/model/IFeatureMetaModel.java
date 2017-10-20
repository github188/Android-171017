package com.mapgis.mmt.module.gis.toolbar.accident2.model;

import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.common.widget.treeview.TreeNode;

import java.util.List;

/**
 * 爆管分析结果实体类的处理模型接口
 * Created by Comclay on 2017/3/1.
 */

public interface IFeatureMetaModel {

    FeatureMetaGroup[] getFeatureMetaArray();

    void setFeatureMetaArray(FeatureMetaGroup[] featureMetaArray);

    /**
     * 爆管分析
     */
    void pipeBrokenAnalysis(String url, AnalysisCallback<FeatureMetaGroup[]> callback);

    /**
     * 加载要素数据
     */
    void loadFeatureData(IFeatureItemModel.LoadCallback callback);

    void cancelBrokenAnalysis();

    void loadFeatureMetaItem(FeatureMetaGroup group, IFeatureItemModel.LoadCallback callback, int i);

    void doFilter();

    /**
     * 爆管分析中请求数据时的回调接口
     */
    interface AnalysisCallback<T> {
        void onAnalysisSuccess(T t);

        void onAnalysisFailed(String msg);
    }

    List<TreeNode> getAdapterData();
}
