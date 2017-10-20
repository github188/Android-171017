package com.mapgis.mmt.module.gis.toolbar.accident2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaItem;
import com.mapgis.mmt.module.gis.toolbar.accident2.presenter.AttachDataPresenter;
import com.mapgis.mmt.module.gis.toolbar.accident2.view.IAttachDataView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Comclay on 2017/5/17.
 * 附属数据
 */

public class AttachDataFragment extends Fragment implements View.OnClickListener, IAttachDataView {
    private static final String ARG_FeatureMetaItem = "FeatureMetaItem";
    private FeatureMetaItem mMetaItem;
    private BottomUnitView mPreButton;
    private BottomUnitView mNextButton;
    private AttachDataPresenter mPresenter;
    private FrameLayout mEmptyView;
    private FrameLayout mLoadingView;
    private LinearLayout mContentView;
    private AttDataAdapter mAdapter;
    private ProgressDialog mDialog;
    private AlertDialog mExportDialog;

    public static AttachDataFragment getInstance(FeatureMetaItem metaItem) {
        AttachDataFragment attchDataFragment = new AttachDataFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_FeatureMetaItem, metaItem);
        attchDataFragment.setArguments(bundle);
        return attchDataFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_att_data, null);
        if (this.getArguments() != null) {
            mMetaItem = this.getArguments().getParcelable(ARG_FeatureMetaItem);
        }
        if (mPresenter == null) {
            mPresenter = new AttachDataPresenter(this, mMetaItem);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    private void initView(View view) {
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.ll_bottomView);
        mEmptyView = (FrameLayout) view.findViewById(R.id.emptyData);
        mLoadingView = (FrameLayout) view.findViewById(R.id.loadingData);
        mContentView = (LinearLayout) view.findViewById(R.id.contentView);
        mEmptyView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
        mContentView.setVisibility(View.GONE);

        ListView mListView = (ListView) view.findViewById(R.id.listView);
        mAdapter = new AttDataAdapter(null);
        mListView.setAdapter(mAdapter);

        initBottomView(linearLayout);
    }

    private void initBottomView(LinearLayout linearLayout) {
        mPreButton = BottomUnitView.create(getActivity())
                .setImageResource(R.drawable.mapview_back)
                .setContent("上一页");
        mPreButton.setOnClickListener(this);
        mNextButton = BottomUnitView.create(getActivity())
                .setImageResource(R.drawable.mapview_back_reverse)
                .setContent("下一页");
        mNextButton.setOnClickListener(this);
        linearLayout.addView(mPreButton);
        linearLayout.addView(mNextButton);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mPreButton)) {
            prePage();
        } else if (v.equals(mNextButton)) {
            nextPage();
        }
    }

    private void initData() {
        this.mPresenter.loadAttData();
    }

    @Override
    public void prePage() {
        showDialog("正在加载...");
        this.mPresenter.prePage();
        hidenDialog();
    }

    @Override
    public void nextPage() {
        showDialog("正在加载...");
        this.mPresenter.nextPage();
        hidenDialog();
    }

    public void refreshData(Map<String, String> attMap) {
        this.mAdapter.setMap(attMap);
        this.mAdapter.notifyDataSetChanged();
        Activity activity = getActivity();
        if (activity instanceof AttachDataActivity) {
            String currentTabTitle = ((AttachDataActivity) activity).getCurrentTabTitle();
            String layerName = mPresenter.getLayerName();
            if (layerName.equals(currentTabTitle)){
                ((AttachDataActivity) activity).setTitleIndex(this.mPresenter.getTitleIndex());
            }
        }
    }

    @Override
    public void showLoadProgress() {
        mLoadingView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
        mContentView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void loadSuccess() {
        Toast.makeText(getActivity(), "加载成功", Toast.LENGTH_SHORT).show();
        mLoadingView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mContentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadError(String msg) {
        mLoadingView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mContentView.setVisibility(View.INVISIBLE);
        if (!BaseClassUtil.isNullOrEmptyString(msg)) {
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    public void showEmptyView() {
        mLoadingView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
        mContentView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showDialog(String msg) {
        if (this.mDialog == null) {
            this.mDialog = MmtProgressDialog.getLoadingProgressDialog(getActivity(), msg);
            this.mDialog.setCancelable(false);
        } else if (this.mDialog.isShowing()) {
            return;
        }
        this.mDialog.show();
    }

    @Override
    public void hidenDialog() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    @Nullable
    public String getLayerName() {
        if (mPresenter == null) {
            return null;
        }
        return mPresenter.getLayerName();
    }

    public void showExportDialog() {
        // 判断是否已经从获取了附属数据的id
        String attIds = this.mPresenter.getAttIds();
        if (BaseClassUtil.isNullOrEmptyString(attIds)) {
            Toast.makeText(getActivity(), "附属数据的关键信息未加载完成，请稍后再试!", Toast.LENGTH_LONG).show();
            return;
        }

        /*if (mDialog != null) {
            mDialog.show();
            return;
        }*/
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提醒");
        builder.setMessage("是否导出" + mPresenter.getLayerName() + "附属数据");
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exportXls();
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        mExportDialog = builder.create();
        mExportDialog.show();
    }

    public void exportXls() {
        showDialog("正在导出" + mPresenter.getLayerName() + "附属数据，请稍等...");
        this.mPresenter.exportXls();
    }

    @Override
    public void exportSuccess(String xlsPath) {
        hidenDialog();
        Toast.makeText(getActivity(), "导出路径：" + xlsPath, Toast.LENGTH_LONG).show();
        viewExportXls(xlsPath);
    }

    /**
     * 查看导出的Excel文件
     *
     * @param xlsPath 本地文件路径
     */
    @Override
    public void viewExportXls(String xlsPath) {
        try {
            Intent view = new Intent();
            view.setAction(Intent.ACTION_VIEW);
            view.setDataAndType(Uri.parse(xlsPath), "application/vnd.ms-excel");
            startActivity(view);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "文件格式不支持,无法打开", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void exportFailed(String msg) {
        hidenDialog();
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void hidenExportDialog() {
        if (mExportDialog != null && mExportDialog.isShowing()) {
            mExportDialog.dismiss();
        }
    }

    public String getTitleIndex() {
        if (mPresenter == null){
            return null;
        }
        return mPresenter.getTitleIndex();
    }

    private class AttDataAdapter extends BaseAdapter {
        LayoutInflater inflater;
        Map<String, String> attMap;
        List<String> keyList;

        AttDataAdapter(Map<String, String> attMap) {
            inflater = getActivity().getLayoutInflater();
            keyList = new ArrayList<>();
            setMap(attMap);
        }

        private void setMap(Map<String, String> attMap) {
            if (attMap == null) return;
            this.attMap = attMap;
            for (String key : attMap.keySet())
                keyList.add(key);
        }

        @Override
        public int getCount() {
            return attMap == null ? 0 : attMap.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_attach_data_view, parent, false);
            }
            ViewHolder holder = ViewHolder.getHolder(convertView);
            final String key = keyList.get(position);
            holder.tvKey.setText(key);
            String value = attMap.get(key);
            holder.tValue.setText(BaseClassUtil.isNullOrEmptyString(value) ? "-" : value);
            return convertView;
        }
    }

    static class ViewHolder {
        private TextView tvKey;
        private TextView tValue;

        public ViewHolder(View view) {
            tvKey = (TextView) view.findViewById(R.id.tvKey);
            tValue = (TextView) view.findViewById(R.id.tValue);
        }

        private static ViewHolder getHolder(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder == null) {
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            return holder;
        }
    }

    @Override
    public void onDestroy() {
        hidenDialog();
        hidenExportDialog();
        mPresenter.cancelTask();
        super.onDestroy();
    }
}
