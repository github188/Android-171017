package com.mapgis.mmt.module.gis.toolbar.accident2.model.impl;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.treeview.ThreeStatusCheckBox;
import com.mapgis.mmt.common.widget.treeview.TreeNode;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureItem;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaItem;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureItemModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureMetaModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.util.FilterHelper;
import com.mapgis.mmt.module.gis.toolbar.accident2.util.MetaType;
import com.mapgis.mmt.module.gis.toolbar.accident2.util.ShowFeatureFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Comclay on 2017/3/1.
 * 爆管分析界面的数据模型处理类
 * <p>
 * 该类中拥有爆管分析中所有分析结果数据，充分与界面解耦
 */

public class FeatureMetaModel implements IFeatureMetaModel {
    private static final String TAG = "FeatureMetaModel";

    private IFeatureItemModel mItemModel;
    private FeatureMetaGroup[] mFeatureMetaArray;
    private transient PipeBrokenAnalysisTask mBrokenTask;

    public FeatureMetaModel() {
        mItemModel = new FeatureItemModel();
    }

    @Override
    public void pipeBrokenAnalysis(String url, AnalysisCallback<FeatureMetaGroup[]> callback) {
        // 开启异步任务执行爆管分析
        mBrokenTask = new PipeBrokenAnalysisTask(callback);
        mBrokenTask.execute(url);
    }

    public FeatureMetaGroup[] getFeatureMetaArray() {
        return mFeatureMetaArray;
    }

    @Override
    public void setFeatureMetaArray(FeatureMetaGroup[] featureMetaArray) {
        this.mFeatureMetaArray = featureMetaArray;
    }

    /**
     * 用线程池请求所有默认可见的设备数据
     */
    @Override
    public void loadFeatureData(IFeatureItemModel.LoadCallback callback) {
        if (mFeatureMetaArray == null || mFeatureMetaArray.length == 0) {
            return;
        }
        for (FeatureMetaGroup group : mFeatureMetaArray) {
            if (group.isShow()) {
                mItemModel.loadFeatureMetaGroup(group, callback);
            }
        }
    }

    @Override
    public void loadFeatureMetaItem(FeatureMetaGroup group, IFeatureItemModel.LoadCallback callback, int i) {
        mItemModel.loadFeatureMetaItem(group, callback, i);
    }

    @Override
    public void doFilter() {
        // 根据需求过滤不需要的数据
        new FilterHelper().filter(this);
    }

    @Override
    public List<TreeNode> getAdapterData() {
        List<TreeNode> nodeList = new ArrayList<>();
        if (this.mFeatureMetaArray != null && this.mFeatureMetaArray.length != 0) {
            TreeNode node;
            int index;
            int childIndex;

            String[] types = Arrays.copyOfRange(ShowFeatureFilter.mShowFeatureTypes, 0, ShowFeatureFilter.mShowFeatureTypes.length);
            Arrays.sort(types);
            for (int i = 0; i < mFeatureMetaArray.length; i++) {
                FeatureMetaGroup metaGroup = mFeatureMetaArray[i];
                index = nodeList.size();
                node = new TreeNode(index, -1, metaGroup);
                nodeList.add(node);
                ArrayList<FeatureMetaItem> resultList = metaGroup.getResultList();
                if (resultList == null || resultList.size() == 0) {
                    node.setExpandable(false);
                    node.setCheckable(false);
                    continue;
                }

                node.setExpand(true);

                // 管段部分只显示管线不显示管段标注
                if(metaGroup.isShow() && !MetaType.TYPE_PIPE_LINE.equals(metaGroup.getCivFeatureMetaType())){
                    // 默认显示的部分
                    node.setCheckStatus(ThreeStatusCheckBox.CHECK_ALL);
                }

                for (int j = 0; j < resultList.size(); j++) {
                    FeatureMetaItem metaItem = resultList.get(j);
                    childIndex = nodeList.size();
                    node = new TreeNode(childIndex, index, metaItem);
                    nodeList.add(node);
                    node.setHideChecked(true);

                    FeatureGroup featureGroup = metaItem.getFeatureGroup();
                    if (featureGroup == null) {
                        continue;
                    }
                    ArrayList<FeatureItem> features = featureGroup.getFeatures();
                    if (features == null || features.size() == 0) {
                        continue;
                    }

                    for (int k = 0; k < features.size(); k++) {
                        FeatureItem featureItem = features.get(k);
                        node = new TreeNode(nodeList.size(), childIndex, featureItem);
                        nodeList.add(node);

                        node.setHideExpand(true);
                        node.setHideChecked(true);
                    }
                }

            }
        }
        return nodeList;
    }

    /**
     * 爆管分析的异步任务
     */
    private class PipeBrokenAnalysisTask extends AsyncTask<String, Void, String> {
        private AnalysisCallback<FeatureMetaGroup[]> callback;

        PipeBrokenAnalysisTask(AnalysisCallback<FeatureMetaGroup[]> callback) {
            if (callback == null) {
                this.callback = new DefaultAnalysisCallback<>();
            } else {
                this.callback = callback;
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String msg = null;
            try {
                String result = NetUtil.executeHttpGet(params[0]);
                if (BaseClassUtil.isNullOrEmptyString(result)) {
                    msg = "爆管失败，请检查网络并重新选择爆管点";
                } else {
                    FeatureMetaModel.this.mFeatureMetaArray = new Gson().fromJson(result
                            , new TypeToken<FeatureMetaGroup[]>() {
                            }.getType());
                    Log.i(TAG, "过滤之前的结果: " + Arrays.toString(FeatureMetaModel.this.mFeatureMetaArray));
                    doFilter();
                    Log.i(TAG, "过滤之后的结果: " + Arrays.toString(FeatureMetaModel.this.mFeatureMetaArray));
                }
            } catch (Exception e) {
                msg = "数据解析异常";
                e.printStackTrace();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String s) {
            if (!BaseClassUtil.isNullOrEmptyString(s)) {
                this.callback.onAnalysisFailed(s);
                return;
            }
            this.callback.onAnalysisSuccess(FeatureMetaModel.this.mFeatureMetaArray);
        }
    }

    /**
     * 取消爆管分析任务
     */
    public void cancelBrokenAnalysis() {
        try {
            if (this.mBrokenTask != null && !this.mBrokenTask.isCancelled()) {
                this.mBrokenTask.cancel(true);
            }
            this.mBrokenTask = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
