package com.mapgis.mmt.module.gatherdata;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.gatherdata.operate.GatherDataOperate;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.login.UserBean;

import java.util.ArrayList;
import java.util.List;

public class GatherDataListFragment extends Fragment {
    private final MapGISFrame mapGISFrame;
    private final List<GatherProjectBean> projectBeanList = new ArrayList<GatherProjectBean>();

    private PullToRefreshListView mPullRefreshListView;
    private BaseAdapter adapter;

    // private CreateCaseTask createCaseTask;

    public GatherDataListFragment(MapGISFrame mapGISFrame) {
        this.mapGISFrame = mapGISFrame;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(DimenTool.dip2px(mapGISFrame, 56),
                DimenTool.dip2px(mapGISFrame, 56));
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        ImageView targetView = new ImageView(mapGISFrame);
        targetView.setLayoutParams(layoutParams);
        targetView.setImageResource(R.drawable.mapview_gather_point);
        targetView.setTag("MapViewScreenView");

        mapGISFrame.getMapView().addView(targetView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.base_fragment, null);

        view.findViewById(R.id.mainActionBar).setVisibility(View.GONE);

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.baseBottomView);

        layout.setVisibility(View.VISIBLE);

        layout.addView(new BottomUnitView(mapGISFrame, "新建", new OnClickListener() {

            @Override
            public void onClick(View v) {
                String[] functions = new String[]{"单点采集", "单线采集"};

                ListDialogFragment fragment = new ListDialogFragment("新建任务", functions);
                fragment.setListItemClickListener(new OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {

                        GatherProjectBean projectBean = new GatherProjectBean();
                        projectBean.Name = BaseClassUtil.getSystemTime("yyMMdd-HHmmss");
                        projectBean.Repoter = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;
                        projectBean.Status = "未处理";
                        projectBean.ReportTime = BaseClassUtil.getSystemTime();

                        mapGISFrame.setBottomBarClear();

                        if (value.equals("片区采集")) {
                            projectBean.Type = "片区采集";
                        } else if (value.equals("单点采集")) {
                            projectBean.Type = "单点采集";
                        } else if (value.equals("单线采集")) {
                            projectBean.Type = "单线采集";
                        }

                        // if (createCaseTask != null &&
                        // createCaseTask.getStatus() == Status.RUNNING) {
                        // mapGISFrame.showToast("正在创建案件，请不要重复操作");
                        // return;
                        // }

                        new GatherDataOperate(mapGISFrame, projectBean).showGatherDataMap();

                        // new
                        // CreateCaseTask(projectBean).executeOnExecutor(MyApplication.executorService);

                    }
                });

                fragment.show(mapGISFrame.getSupportFragmentManager(), "");
            }
        }));

        // ListView的布局信息
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(DimenTool.dip2px(getActivity(), 12), 0, DimenTool.dip2px(getActivity(), 12), 0);
        layoutParams.addRule(RelativeLayout.ABOVE, layout.getId());

        mPullRefreshListView = new PullToRefreshListView(getActivity());
        mPullRefreshListView.setId(mPullRefreshListView.hashCode());
        mPullRefreshListView.setLayoutParams(layoutParams);
        mPullRefreshListView.getRefreshableView().setDivider(getResources().getDrawable(R.color.default_no_bg));
        mPullRefreshListView.getRefreshableView().setDividerHeight(DimenTool.dip2px(getActivity(), 12));
        mPullRefreshListView.getRefreshableView().setSelector(R.drawable.item_focus_bg);
        mPullRefreshListView.setFocusable(false);

        ((ViewGroup) view).addView(mPullRefreshListView);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new MyAdapter(getActivity(), projectBeanList);
        mPullRefreshListView.setAdapter(adapter);

        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                new RefreashTask().executeOnExecutor(MyApplication.executorService);
            }
        });

        mPullRefreshListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mapGISFrame.getMapView().getGraphicLayer().removeAllGraphics();

                GatherProjectBean singleBean = (GatherProjectBean) parent.getItemAtPosition(position);

                new GatherDataOperate(mapGISFrame, singleBean, false).showGatherDataMap();
            }
        });

        refreash();
    }

    /**
     * 数据加载完成后，若数据为空，则默认启动创建功能
     */
    private void dataLoadingFinish() {
        // buttomCreateView.performClick();
    }

    public void refreash() {
        mPullRefreshListView.setRefreshing(true);
    }

    /**
     * 从服务获取在办工程
     */
    class RefreashTask extends AsyncTask<Void, Void, ResultData<GatherProjectBean>> {
        @Override
        protected ResultData<GatherProjectBean> doInBackground(Void... params) {

            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/MapgisCity_WXYH_Product/REST/CaseManageREST.svc/GetGatherDataProject";

            String result = NetUtil.executeHttpGet(url, "UserID", MyApplication.getInstance().getUserId() + "");

            if (BaseClassUtil.isNullOrEmptyString(result)) {
                return null;
            }

            ResultData<GatherProjectBean> data = new Gson().fromJson(result, new TypeToken<ResultData<GatherProjectBean>>() {
            }.getType());

            return data;
        }

        @Override
        protected void onPostExecute(ResultData<GatherProjectBean> result) {
            mPullRefreshListView.onRefreshComplete();

            if (result == null) {
                mapGISFrame.showToast("查询数据失败");
                return;
            }

            if (result.ResultCode < 0) {
                mapGISFrame.showToast(result.ResultMessage);
                return;
            }

            projectBeanList.clear();
            projectBeanList.addAll(result.DataList);

            if (projectBeanList.size() == 0) {
                dataLoadingFinish();
            } else {
                for (GatherProjectBean projectBean : projectBeanList) {
                    projectBean.initGatherDataElementDot();
                    projectBean.save();
                }
            }

            adapter.notifyDataSetChanged();
        }
    }

    /**
     * <strong>弃用</strong>，手持只负责上报，不创建工作流<br>
     * 创建案件
     */
    @SuppressWarnings("unused")
    private class CreateCaseTask extends AsyncTask<Void, Void, ResultData<String>> {
        private ProgressDialog loadingDialog;
        private final GatherProjectBean projectBean;

        public CreateCaseTask(GatherProjectBean projectBean) {
            this.projectBean = projectBean;
        }

        @Override
        protected void onPreExecute() {
            loadingDialog = MmtProgressDialog.getLoadingProgressDialog(getActivity(), "正在创建案件");
            loadingDialog.show();
        }

        @Override
        protected ResultData<String> doInBackground(Void... params) {

            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/MapgisCity_WXYH_Product/REST/CaseManageREST.svc/GatherDataCreateNewCase?userID="
                    + MyApplication.getInstance().getUserId();

            String result = null;

            try {
                result = NetUtil.executeHttpPost(url, new Gson().toJson(projectBean, GatherProjectBean.class),
                        "Content-Type", "application/json; charset=utf-8");

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (BaseClassUtil.isNullOrEmptyString(result)) {
                return null;
            }

            ResultData<String> data = new Gson().fromJson(result, new TypeToken<ResultData<String>>() {
            }.getType());

            return data;
        }

        @Override
        protected void onPostExecute(ResultData<String> result) {
            loadingDialog.cancel();

            if (result == null) {
                mapGISFrame.showToast("创建案件失败,请检查网络情况，或者服务是否存在");
                return;
            }

            if (result.ResultCode < 0) {
                mapGISFrame.showToast(result.ResultMessage);
                return;
            }

            mapGISFrame.showToast("创建案件成功");

            refreash();

            projectBean.ID = Integer.valueOf(result.DataList.get(0));
            projectBean.CaseNo = result.DataList.get(1);

            projectBean.save();

            new GatherDataOperate(mapGISFrame, projectBean).showGatherDataMap();
        }
    }

    /**
     * 删除指定采集工程案件
     */
    private class DeleteTask extends AsyncTask<GatherProjectBean, Void, ResultWithoutData> {
        private ProgressDialog loadingDialog;

        @Override
        protected void onPreExecute() {
            loadingDialog = MmtProgressDialog.getLoadingProgressDialog(getActivity(), "正在删除数据");
            loadingDialog.show();
        }

        @Override
        protected ResultWithoutData doInBackground(GatherProjectBean... params) {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/MapgisCity_WXYH_Product/REST/CaseManageREST.svc/DeleteGatherDataProject";

            String result = NetUtil.executeHttpGet(url, "CaseNO", params[0].CaseNo);

            if (BaseClassUtil.isNullOrEmptyString(result)) {
                return null;
            }

            ResultWithoutData data = new Gson().fromJson(result, ResultWithoutData.class);

            return data;
        }

        @Override
        protected void onPostExecute(ResultWithoutData result) {
            loadingDialog.dismiss();

            if (result == null) {
                mapGISFrame.showToast("删数失败,检查服务是否存在");
                return;
            }

            if (result.ResultCode < 0) {
                mapGISFrame.showToast(result.ResultMessage);
                return;
            }

            mapGISFrame.showToast("删除成功");

            refreash();
        }

    }

    /**
     * 列表适配器
     */
    class MyAdapter extends BaseAdapter {
        private final List<GatherProjectBean> projectBeans;
        private final LayoutInflater inflater;

        public MyAdapter(Context context, List<GatherProjectBean> projectBeans) {
            this.projectBeans = projectBeans;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return projectBeans.size();
        }

        @Override
        public Object getItem(int position) {
            return projectBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.acquisition_list_item_view, null);
                viewHolder = new ViewHolder();
                viewHolder.index = (TextView) convertView.findViewById(R.id.acquisitionListItemIndex);
                viewHolder.acquisitionListItemName = (TextView) convertView.findViewById(R.id.acquisitionListItemName);
                viewHolder.acquisitionListItemCaseNO = (TextView) convertView.findViewById(R.id.acquisitionListItemCaseNO);
                viewHolder.acquisitionListItemReporter = (TextView) convertView.findViewById(R.id.acquisitionListItemReporter);
                viewHolder.acquisitionListItemCreateDate = (TextView) convertView
                        .findViewById(R.id.acquisitionListItemCreateDate);
                viewHolder.acquisitionListItemStatus = (TextView) convertView.findViewById(R.id.acquisitionListItemStatus);
                viewHolder.acquisitionListItemReportDate = (TextView) convertView
                        .findViewById(R.id.acquisitionListItemReportDate);
                viewHolder.acquisitionListItemType = (TextView) convertView.findViewById(R.id.acquisitionListItemType);
                viewHolder.acquisitionListItemLeft = (TextView) convertView.findViewById(R.id.acquisitionListItemLeft);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final GatherProjectBean entity = (GatherProjectBean) getItem(position);

            viewHolder.index.setText((position + 1) + ".");
            viewHolder.acquisitionListItemName.setText(entity.Name);
            viewHolder.acquisitionListItemCaseNO.setText(entity.CaseNo);
            viewHolder.acquisitionListItemReporter.setText("上报人:" + entity.Repoter);
            viewHolder.acquisitionListItemCreateDate.setText(entity.ReportTime);
            viewHolder.acquisitionListItemType.setText(entity.Type);

            viewHolder.acquisitionListItemLeft.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final OkCancelDialogFragment fragment = new OkCancelDialogFragment("确认删除");
                    fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
                        @Override
                        public void onRightButtonClick(View view) {
                            new DeleteTask().executeOnExecutor(MyApplication.executorService, entity);
                            fragment.dismiss();
                        }
                    });
                    fragment.show(mapGISFrame.getSupportFragmentManager(), "");
                }
            });

            viewHolder.acquisitionListItemStatus.setVisibility(View.GONE);
            viewHolder.acquisitionListItemReportDate.setVisibility(View.GONE);

            return convertView;
        }

        class ViewHolder {
            public TextView index;
            public TextView acquisitionListItemName;
            public TextView acquisitionListItemCaseNO;
            public TextView acquisitionListItemReporter;
            public TextView acquisitionListItemCreateDate;
            public TextView acquisitionListItemStatus;
            public TextView acquisitionListItemReportDate;
            public TextView acquisitionListItemType;

            public TextView acquisitionListItemLeft;
        }
    }
}
