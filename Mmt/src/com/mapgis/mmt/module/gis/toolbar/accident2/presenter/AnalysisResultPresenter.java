package com.mapgis.mmt.module.gis.toolbar.accident2.presenter;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.mapgis.mmt.module.gis.toolbar.accident2.AnalysisResultActivity;
import com.mapgis.mmt.module.gis.toolbar.accident2.AnalysisResultFragment;
import com.mapgis.mmt.module.gis.toolbar.accident2.PipeBrokenAnalysisMenu;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureMetaModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.view.IAnalysisResultView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Comclay on 2017/3/4.
 * 爆管分析结果界面presenter类
 */

public class AnalysisResultPresenter {
    private static final String TAG = "AnalysisResultPresenter";
    private IFeatureMetaModel mFeatureMetaModel;
    private IAnalysisResultView mAnalysisResultView;
    private RecyclerView.Adapter adapter;

    public AnalysisResultPresenter(IAnalysisResultView mAnalysisResultView) {
        this.mAnalysisResultView = mAnalysisResultView;
        mFeatureMetaModel = PipeBrokenAnalysisPresenter.getFeatureMetaModel();
    }

    /**
     * 将从地图界面获取到的数据赋值给结果展示界面的数据模型
     */
    public void getFeatureMetaData() {
        if (mAnalysisResultView instanceof AnalysisResultFragment) {
            FragmentActivity activity = ((AnalysisResultFragment) mAnalysisResultView).getActivity();
            if (activity instanceof AnalysisResultActivity) {
                Intent intent = activity.getIntent();
                if (intent.hasExtra(PipeBrokenAnalysisMenu.PARAM_ANALYSIS_RESULT)) {
                    /*
                     * 这里要注意数组类型之间的转换问题
                     * 数组的类型取决于数组创建时候的类型，而非数组中元素的类型
                     * 所以直接转型会出现ClassCastException
                     */
                    Parcelable[] parcelableArrayExtra = intent.getParcelableArrayExtra(
                            PipeBrokenAnalysisMenu.PARAM_ANALYSIS_RESULT);
                    List<Parcelable> parcelables = Arrays.asList(parcelableArrayExtra);
                    FeatureMetaGroup[] featureMetaArray = new FeatureMetaGroup[parcelables.size()];
                    parcelables.toArray(featureMetaArray);
                    mFeatureMetaModel.setFeatureMetaArray(featureMetaArray);
                    Log.i(TAG, "爆管分析的结果：" + Arrays.toString(featureMetaArray));
                }
            }
        }
    }

    /**
     * 扩大关阀
     * 重新进行爆管分析
     */
    public void expandCloseValve() {

    }

    public AnalysisResultAdapter getAdapter() {
        return new AnalysisResultAdapter(mAnalysisResultView, mFeatureMetaModel);
    }
}
