package com.mapgis.mmt.module.gis.toolbar.accident2.presenter;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaItem;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IAttachDataModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.impl.AttachDataModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.impl.TaskCallback;
import com.mapgis.mmt.module.gis.toolbar.accident2.view.IAttachDataView;

import java.util.Locale;
import java.util.Map;

/**
 * Created by Comclay on 2017/5/17.
 */

public class AttachDataPresenter {
    private IAttachDataView mAttachView;
    private IAttachDataModel mAttachModel;
    private int mCurrentIndex = 0;

    public AttachDataPresenter(IAttachDataView mAttachView, FeatureMetaItem metaItem) {
        this.mAttachView = mAttachView;
        this.mAttachModel = new AttachDataModel(metaItem);
    }

    /**
     * 刷新
     */
    public void loadAttData() {
        String relationShipTableName = mAttachModel.getRelationShipTableName();
        if (BaseClassUtil.isNullOrEmptyString(relationShipTableName)){
            mAttachView.showEmptyView();
            return;
        }
        if (mAttachModel.getAttData() == null) {
            mAttachModel.queryAttachData(new TaskCallback() {
                @Override
                public void onPreExecute() {
                    mAttachView.showLoadProgress();
                }

                @Override
                public void onSuccess() {
                    if (mAttachModel.isEmpty()) {
                        mAttachView.showEmptyView();
                        return;
                    }
                    mAttachView.loadSuccess();
                    refresh();
                }

                @Override
                public void onFailed(String msg) {
                    mAttachView.loadError(msg);
                }
            });
        }
    }

    /**
     * 上一页
     */
    public void prePage() {
        mCurrentIndex--;
        if (mCurrentIndex < 0) {
            mCurrentIndex = mAttachModel.getSize() - 1;
        }
        refresh();
    }

    /**
     * 下一页
     */
    public void nextPage() {
        mCurrentIndex++;
        if (mCurrentIndex >= mAttachModel.getSize()) {
            mCurrentIndex = 0;
        }
        refresh();
    }

    public String getTitleIndex() {
        int size = mAttachModel.getSize();
        if (size == 0){
            return null;
        }
        return String.format(Locale.CHINA, "当前：%d/%d", mCurrentIndex + 1, size);
    }

    private void refresh() {
        Map<String, String> attMap = mAttachModel.getAttData(mCurrentIndex);
        if (attMap == null || attMap.size() == 0) {
            mAttachView.showEmptyView();
        }
        mAttachView.refreshData(attMap);
    }

    public void cancelTask() {
        this.mAttachModel.cancelTask();
    }

    public String getLayerName() {
        return this.mAttachModel.getLayerName();
    }

    public String getAttIds() {
        return this.mAttachModel.getAttIds();
    }

    public void exportXls() {
        this.mAttachModel.exportAttData(new TaskCallback() {
            @Override
            public void onPreExecute() {

            }

            @Override
            public void onSuccess() {
                mAttachView.exportSuccess(mAttachModel.getXlsPath());
            }

            @Override
            public void onFailed(String msg) {
                mAttachView.exportFailed(msg);
            }
        });
    }
}
