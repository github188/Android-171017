package com.mapgis.mmt.module.gis.toolbar.accident2;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.treeview.MultiTreeAdapter;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.presenter.AnalysisResultAdapter;
import com.mapgis.mmt.module.gis.toolbar.accident2.presenter.AnalysisResultPresenter;
import com.mapgis.mmt.module.gis.toolbar.accident2.view.IAnalysisResultView;
import com.mapgis.mmt.module.gis.toolbar.accident2.view.IPipeBrokenAnalysisView;

import java.util.ArrayList;

/**
 * 爆管分析结果
 */
public class AnalysisResultFragment extends Fragment implements IAnalysisResultView, View.OnClickListener {
    private static final String TAG = "AnalysisResultFragment";
    private AnalysisResultPresenter mPresenter;
    private AnalysisResultAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Button mBtnExpandCloseValve;
    private Button mBtnReselectBrokenPoint;
    private Handler mHandler;

    private ProgressDialog mProgressDialog;

    public AnalysisResultFragment() {
        if (mPresenter == null) {
            mPresenter = new AnalysisResultPresenter(this);
        }
    }

    public static AnalysisResultFragment newInstance() {
        return new AnalysisResultFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mPresenter.getFeatureMetaData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis_result, container, false);
        findView(view);
        return view;
    }

    @Override
    public void findView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mBtnExpandCloseValve = (Button) view.findViewById(R.id.btn_expand_close_valve);
        mBtnReselectBrokenPoint = (Button) view.findViewById(R.id.btn_reselect_broken_point);

        mBtnExpandCloseValve.setOnClickListener(this);
        mBtnReselectBrokenPoint.setOnClickListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new Handler();
        ProgressDialog loadingProgressDialog = MmtProgressDialog.getLoadingProgressDialog(getActivity()
                , getString(R.string.msg_on_loading));
        loadingProgressDialog.show();
        mAdapter = mPresenter.getAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnCheckChangedListener(new MultiTreeAdapter.OnCheckeChangedListener() {
            @Override
            public void onCheckeChanged(MultiTreeAdapter.TreeViewHolder viewHolder, int position, int status) {
                // 判断当前选中的爆管结果是否已经加载了数据，如果未加载,先加载数据
                mAdapter.checked(viewHolder, position, status);
            }
        });
        loadingProgressDialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == mBtnExpandCloseValve.getId()) {
            // 扩大关阀
            expandCloseValve();
        } else {
            // 重选爆管点
            reselectBrokenPoint();
        }
    }

    @Override
    public void expandCloseValve() {
        resultInvalidateValve();
    }

    @Override
    public void resultInvalidateValve() {
        String invalidateValve = mAdapter.getInvalidateValve();
        if (BaseClassUtil.isNullOrEmptyString(invalidateValve)) {
            showToast("失效设备为空");
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(PipeBrokenAnalysisMenu.INVALIDATE_VALVE, invalidateValve);
        getActivity().setResult(PipeBrokenAnalysisMenu.CODE_RESULT_ACTIVITY, intent);
        getActivity().finish();
        MyApplication.getInstance().finishActivityAnimation(getActivity());
    }

    @Override
    public void reselectBrokenPoint() {
        MapGISFrame mapGISFrame = MyApplication.getInstance().mapGISFrame;
        BaseMapMenu menu = mapGISFrame.getFragment().menu;
        if (menu instanceof IPipeBrokenAnalysisView) {
            ((IPipeBrokenAnalysisView) menu).resetBrokenFunction();
        }
        getActivity().finish();
        MyApplication.getInstance().finishActivityAnimation(getActivity());
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        return getActivity().getLayoutInflater();
    }

    /**
     * 查看导出的Excel文件
     *
     * @param fileName 本地文件路径
     */
    @Override
    public void viewExportXls(String fileName) {
        try {
            Intent view = new Intent();
            view.setAction(Intent.ACTION_VIEW);
            view.setDataAndType(Uri.parse(fileName), "application/vnd.ms-excel");
            startActivity(view);
        } catch (Exception e) {
            showToast("文件格式不支持");
            e.printStackTrace();
        }
    }

    @Override
    public void showExportDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = MmtProgressDialog.getLoadingProgressDialog(getActivity()
                    , getString(R.string.build_xls));
        } else if (mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog.show();
    }

    @Override
    public void hidenExportDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void showToast(String msg) {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).showToast(msg);
        }
    }

    /**
     * 返回地图的时候需要将新的数据传回到界面
     */
    @Override
    public void onBackMapView() {
        startMapViewToFront();
    }

    @Override
    public void post(Runnable runnable) {
        mHandler.post(runnable);
    }

    @Override
    public void showLoadingDialog() {
        mProgressDialog = MmtProgressDialog.getLoadingProgressDialog(getActivity(), getString(R.string.msg_on_loading));
        mProgressDialog.show();
    }

    @Override
    public void hidenLoadingDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void startMapViewToFront() {
        Intent intent = new Intent(getActivity(), MapGISFrame.class);
        ArrayList<FeatureMetaGroup> selectMetaGroup = mAdapter.getSelectMetaGroup();
        Log.i(TAG, "选中的数据: " + selectMetaGroup.toString());
        BaseMapMenu menu = MyApplication.getInstance().mapGISFrame.getFragment().menu;
        if (menu instanceof PipeBrokenAnalysisMenu && selectMetaGroup.size() != 0) {
            FeatureMetaGroup[] groups = new FeatureMetaGroup[selectMetaGroup.size()];
            selectMetaGroup.toArray(groups);
            ((PipeBrokenAnalysisMenu) menu).showResultOnMap(groups);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidenExportDialog();
        mProgressDialog = null;
    }
}
