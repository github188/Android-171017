package com.repair.zhoushan.module.casemanage.caseoverview;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DateUtil;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.filtermenubar.FilterMenuBar;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.R;
import com.repair.common.CaseSearchActivity;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CaseOverviewListActivity extends BaseActivity {

    protected final ArrayList<CaseItem> data = new ArrayList<>();

    private final String FILTER_UNDERTAKE_TIME = "承办时间";
    private final String FILTER_HANDLE_STATION = "处理站点";
    private final String FILTER_FLOW_NAME = "流程名称";

    private String filterTime = "本周"; // 承办时间
    private String filterStation = ""; // 处理站点
    private String filterFlowName = ""; // 流程名称

    protected PullToRefreshListView mPullRefreshListView;
    protected ListView actualListView;
    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener2;
    private CaseOverviewListAdapter adapter;

    private FilterMenuBar mFilterMenuBar;
    private EditText txtSearch; // ActionBar搜索框

    // For paging loading
    private int currentPageIndex = 1; // Start from 1
    private final int pageSize = 10;
    private boolean isLoadMoreMode = false;

    private String userBelongStation = "";
    private String existFlowNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setDefaultContentView() {

        setSwipeBackEnable(false);
        setContentView(R.layout.doingbox_list_activity);

        initView();
        initListener();
    }

    private void initView() {

        initActionBar();

        this.mFilterMenuBar = (FilterMenuBar) findViewById(R.id.mMenuBar);
        mFilterMenuBar.setMenuItems(new String[]{FILTER_UNDERTAKE_TIME},
                new String[][]{{"昨天", "今天", "本周", "上周", "本月", "上月"}},
                new String[]{filterTime});

        existFlowNames = getIntent().getStringExtra("FlowNames");
        if (!TextUtils.isEmpty(existFlowNames)) {
            String[] flowNames = existFlowNames.split(",");
            mFilterMenuBar.addMenuItem(FILTER_FLOW_NAME, flowNames, "");
        } else {
            getFlowNameList();
        }

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.maintenanceFormList);
        actualListView = mPullRefreshListView.getRefreshableView();
        // mPullRefreshListView.setEmptyView(inflater.inflate(R.layout.empty_default, null));
        registerForContextMenu(actualListView);

        adapter = new CaseOverviewListAdapter(CaseOverviewListActivity.this, data);
        mPullRefreshListView.setAdapter(adapter);

        // 下拉刷新时的提示文本设置
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");
        // 上拉加载更多时的提示文本设置
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");

    }

    private void initActionBar() {

        findViewById(R.id.btnOtherAction).setVisibility(View.GONE);

        addBackBtnListener(findViewById(R.id.btnBack));

        this.txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.setVisibility(View.VISIBLE);
        txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CaseOverviewListActivity.this, CaseSearchActivity.class);

                // key 为搜索框的当前值
                intent.putExtra("key", txtSearch.getText().toString());
                intent.putExtra("searchHint", "事件编号、工单编号、上报人、摘要");
                intent.putExtra("searchHistoryKey", "CaseListSearchHistory_CaseOverview");

                startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });
    }

    private void initListener() {

        mFilterMenuBar.setOnMenuItemSelectedListener(new FilterMenuBar.OnMenuItemSelectedListener() {
            @Override
            public void onItemSelected(Map<String, String> selectResult) {
                filterTime = selectResult.get(FILTER_UNDERTAKE_TIME);
                filterStation = selectResult.get(FILTER_HANDLE_STATION);
                filterFlowName = selectResult.get(FILTER_FLOW_NAME);
                refreshData();
            }
        });

        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.onDetailClick(position - 1);
            }
        });

        this.mOnRefreshListener2 = new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 更新下拉面板
                String label = DateUtils.formatDateTime(CaseOverviewListActivity.this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(label);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                data.clear();
                currentPageIndex = 1;
                isLoadMoreMode = false;

                loadData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 更新上拉面板
                String label = DateUtils.formatDateTime(CaseOverviewListActivity.this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                isLoadMoreMode = true;
                loadData();
            }
        };

        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(mOnRefreshListener2);

        //region Filter criteria
        getUserStationList();
        //endregion
    }

    private void getUserStationList() {

        MmtBaseTask<String, Void, Results<String>> mmtBaseTask = new MmtBaseTask<String, Void, Results<String>>(CaseOverviewListActivity.this, false) {

            @Override
            protected Results<String> doInBackground(String... params) {

                Results<String> result;
                String stationUrl = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CommonModule/GetStationListByUserID?userID="
                        + userID + "&isAll=0";
                try {
                    String jsonResult = NetUtil.executeHttpGet(stationUrl);
                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取站点列表失败：网络错误");
                    }
                    result = new Gson().fromJson(jsonResult, new TypeToken<Results<String>>(){}.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                    result = new Results<>("-100", e.getMessage());
                }
                return result;
            }

            @Override
            protected void onSuccess(Results<String> result) {

                ResultData<String> resultStation = result.toResultData();
                if (resultStation.ResultCode != 200) {
                    Toast.makeText(CaseOverviewListActivity.this, resultStation.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                mFilterMenuBar.addMenuItem(FILTER_HANDLE_STATION, resultStation.DataList.toArray(new String[resultStation.DataList.size()]), "");

                // Start loading list view data
                userBelongStation = BaseClassUtil.listToString(resultStation.DataList);
                mPullRefreshListView.setRefreshing(false);
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();

    }

    private void getFlowNameList() {
        MmtBaseTask<String, Void, Results<String>> mmtBaseTask = new MmtBaseTask<String, Void, Results<String>>(CaseOverviewListActivity.this, false) {

            @Override
            protected Results<String> doInBackground(String... params) {

                Results<String> result;
                String flowUrl = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/"
                        + userID + "/GetDoingBoxFlowNameList";
                try {
                    String jsonResult = NetUtil.executeHttpGet(flowUrl);
                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取流程列表失败：网络错误");
                    }
                    result = new Gson().fromJson(jsonResult, new TypeToken<Results<String>>(){}.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                    result = new Results<>("-100", e.getMessage());
                }
                return result;
            }

            @Override
            protected void onSuccess(Results<String> result) {

                ResultData<String> resultFlowName = result.toResultData();
                if (resultFlowName.ResultCode != 200) {
                    Toast.makeText(CaseOverviewListActivity.this, resultFlowName.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                mFilterMenuBar.addMenuItem(FILTER_FLOW_NAME, resultFlowName.DataList.toArray(new String[resultFlowName.DataList.size()]), "");
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void loadData() {

        final int loadPageIndex = isLoadMoreMode ? currentPageIndex + 1 : currentPageIndex;

        final String searchKey = txtSearch.getText().toString().trim();

        // 执行更新任务,结束后刷新界面
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... params) {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseManage/GetCaseOverviewBoxWithPaging")
                        .append("?pageSize=").append(pageSize)
                        .append("&pageIndex=").append(loadPageIndex)
                        .append("&sortFields=ID0&direction=desc");

                if (!TextUtils.isEmpty(filterFlowName)) {
                    sb.append("&flowName=").append(filterFlowName);
                } else if (!TextUtils.isEmpty(existFlowNames)) {
                    sb.append("&flowName=").append(existFlowNames);
                }

                // String station = !TextUtils.isEmpty(filterStation) ? filterStation : userBelongStation;
                String station = !TextUtils.isEmpty(filterStation) ? filterStation : "";
                sb.append("&station=").append(station);

                if (!TextUtils.isEmpty(filterTime)) {
                    String[] dateRange = DateUtil.getDateSpanString(filterTime);
                    if (dateRange != null && !BaseClassUtil.isNullOrEmptyString(dateRange[0])
                            && !BaseClassUtil.isNullOrEmptyString(dateRange[1])) {

                        String timeStart = dateRange[0];
                        String timeEnd = dateRange[1];

                        try {
                            Date dateEnd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA).parse(timeEnd);
                            Date preDate = new Date(dateEnd.getTime() - (1 * 24 * 3600) * 1000);
                            timeEnd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA).format(preDate);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        sb.append("&dateFrom=").append(Uri.encode(timeStart))
                                .append("&dateTo=").append(Uri.encode(timeEnd));
                    }
                }

                if (!TextUtils.isEmpty(searchKey)) {
                    sb.append("&eventInfo=").append(searchKey);
                }

                return NetUtil.executeHttpGet(sb.toString());
            }

            @Override
            protected void onPostExecute(String jsonResult) {

                mPullRefreshListView.onRefreshComplete();

                ResultData<CaseItem> newData = Utils.json2ResultDataToast(CaseItem.class, CaseOverviewListActivity.this, jsonResult, "获取数据失败", true);
                if (newData == null) return;

                if (newData.DataList.size() == 0) {
                    if (!isLoadMoreMode) { // Refresh
                        Toast.makeText(CaseOverviewListActivity.this, "没有记录", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    } else {               // LoadMore
                        Toast.makeText(CaseOverviewListActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    if (isLoadMoreMode) {
                        currentPageIndex++; //加载更多成功更新当前页页码
                    } else {
                        Toast.makeText(CaseOverviewListActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                    }

                    data.addAll(newData.DataList);

                    adapter.notifyDataSetChanged();
                }
            }
        }.executeOnExecutor(MyApplication.executorService);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1 && requestCode == Constants.SEARCH_REQUEST_CODE) {
            // 搜索界面返回的搜索关键字
            String key = data.getStringExtra("key"); //返回空代表查全部
            txtSearch.setText(key);

            refreshData();
        }
    }

    public void refreshData() {
        if (mPullRefreshListView != null && mOnRefreshListener2 != null) {
            mOnRefreshListener2.onPullDownToRefresh(mPullRefreshListView);
            actualListView.setSelection(0);
        }
    }

    private class CaseOverviewListAdapter extends BaseAdapter implements View.OnClickListener {

        private List<CaseItem> dataList;
        private BaseActivity context;
        private LayoutInflater inflater;

        public CaseOverviewListAdapter(BaseActivity activity, List<CaseItem> data) {
            this.inflater = LayoutInflater.from(activity);
            this.context = activity;
            this.dataList = data;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.case_overview_list_item, parent, false);
            }

            final CaseItem caseItem = dataList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_index))
                    .setText(getString(R.string.string_listitem_index, (position + 1)));

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_top_left)).setText(caseItem.CaseNo);
            TextView tvIsOver = MmtViewHolder.get(convertView, R.id.tv_top_right);
            boolean isOver = "1".equals(caseItem.IsOver) || "2".equals(caseItem.IsOver);
            tvIsOver.setVisibility(isOver ? View.VISIBLE : View.INVISIBLE);

            TextView tvHandleNode = MmtViewHolder.get(convertView, R.id.tv_mid_one_left);
            tvHandleNode.getPaint().setFakeBoldText(true);
            tvHandleNode.setText(getString(R.string.string_two_with_seperator, caseItem.FlowName, caseItem.ActiveName));

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_one_right)).setText(caseItem.Station);

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_two_left))
                    .setText("上报人: " + getString(R.string.string_two_with_seperator, caseItem.ReporterName, caseItem.ReporterDepart));

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_three_left)).setText("承办人: " + caseItem.UnderTakeMan);
            ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_four_left)).setText(caseItem.Summary);
            ((TextView) MmtViewHolder.get(convertView, R.id.tv_time)).setText(caseItem.UnderTakeTime);

            TextView tvLoc = MmtViewHolder.get(convertView, R.id.tv_loc);
            tvLoc.setTag(String.valueOf(position));
            tvLoc.setOnClickListener(this);

            return convertView;
        }

        @Override
        public void onClick(View v) {

            // 定位按钮
            if (v.getId() == R.id.tv_loc) {

                CaseItem caseItem = dataList.get(Integer.parseInt(v.getTag().toString()));
                if (TextUtils.isEmpty(caseItem.XY)) {
                    Toast.makeText(CaseOverviewListActivity.this, "无坐标信息", Toast.LENGTH_SHORT).show();
                } else {
                    BaseMapCallback callback = new ShowMapPointCallback(CaseOverviewListActivity.this, caseItem.XY, caseItem.CaseNo, caseItem.EventName, -1);
                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            }
        }

        /**
         * 点击列表项CaseItem查看详情
         */
        public void onDetailClick(int position) {

            CaseItem caseItem = dataList.get(position);

            Intent intent = new Intent(context, CaseDetailActivity.class);
            intent.putExtra("ListItemEntity", caseItem);
            intent.putExtra("name", "工单总览");
            context.startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
        }
    }

}
