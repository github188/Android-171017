package com.repair.zhoushan.module.eventmanage.eventoverview;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DateUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.filtermenubar.FilterMenuBar;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;
import com.repair.common.CaseSearchActivity;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.EventItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class EventOverviewListActivity extends BaseActivity {

    private FilterMenuBar mFilterMenuBar;

    protected PullToRefreshListView mPullRefreshListView;
    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener2;
    protected ListView actualListView;
    protected EventAdapter adapter;
    private EditText txtSearch; // ActionBar搜索框

    private ArrayList<EventItem> eventItemList = new ArrayList<EventItem>();

    // For paging loading
    private int currentPageIndex = 1; // Start from 1
    private final int pageSize = 10;
    private boolean isLoadMoreMode = false;

    private final String UPDATE_TIME = "更新时间";
    private final String EVENT_STATUS = "事件状态";
    private final String EVENT_TYPE = "事件类型";
    private final String STATION = "处理站点";

    private String filterUpdateTime = "本周";
    private String filterEventStatus = "";
    private String filterEventType = "";
    private String filterStation = "";

    private String userIdStr;

    /**
     * 区分处理站点与上报站点
     */
    private int isSameStation = 1; // TODO: 2016/4/25 区分处理站点与上报站点，未完

    @Override
    protected void setDefaultContentView() {

        setSwipeBackEnable(false);
        setContentView(R.layout.event_overview_list_view);

        this.userIdStr = String.valueOf(MyApplication.getInstance().getUserId());

        initView();
        initListener();
    }

    private void initView() {

        initActionBar();

        this.mFilterMenuBar = (FilterMenuBar) findViewById(R.id.mMenuBar);
        mFilterMenuBar.setMenuItems(new String[]{UPDATE_TIME, EVENT_STATUS},
                new String[][]{{"昨天", "今天", "本周", "上周", "本月", "上月"}, {"待处理", "处理中", "无效", "已处理"}},
                new String[]{filterUpdateTime, filterEventStatus});

        this.mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.listViewEvents);
        this.actualListView = mPullRefreshListView.getRefreshableView();
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        // mPullRefreshListView.setEmptyView(inflater.inflate(R.layout.empty_default, null));
        registerForContextMenu(actualListView);

        mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");
        // 上拉加载更多时的提示文本设置
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");

        this.adapter = new EventAdapter(EventOverviewListActivity.this, eventItemList);
        actualListView.setAdapter(adapter);

    }

    private void initActionBar() {

        addBackBtnListener(findViewById(R.id.btnBack));

        findViewById(R.id.btnOtherAction).setVisibility(View.GONE);

        this.txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.setVisibility(View.VISIBLE);
        txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(EventOverviewListActivity.this, CaseSearchActivity.class);

                // key 为搜索框的当前值
                intent.putExtra("key", txtSearch.getText().toString());
                intent.putExtra("searchHint", "事件编号、上报人、摘要");
                intent.putExtra("searchHistoryKey", "EventListSearchHistory_Overview");

                startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });

    }

    private void initListener() {

        mFilterMenuBar.setOnMenuItemSelectedListener(new FilterMenuBar.OnMenuItemSelectedListener() {
            @Override
            public void onItemSelected(Map<String, String> selectResult) {
                String tempStr;

                if (!TextUtils.isEmpty(tempStr = selectResult.get(UPDATE_TIME))) {
                    filterUpdateTime = tempStr;
                } else {
                    filterUpdateTime = "";
                }

                if (!TextUtils.isEmpty(tempStr = selectResult.get(EVENT_STATUS))) {
                    filterEventStatus = tempStr;
                } else {
                    filterEventStatus = "";
                }

                if (!TextUtils.isEmpty(tempStr = selectResult.get(EVENT_TYPE))) {
                    filterEventType = tempStr;
                } else {
                    filterEventType = "";
                }

                if (!TextUtils.isEmpty(tempStr = selectResult.get(STATION))) {
                    filterStation = tempStr;
                } else {
                    filterStation = "";
                }

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
                String label = DateUtils.formatDateTime(EventOverviewListActivity.this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(label);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                eventItemList.clear();
                currentPageIndex = 1;
                isLoadMoreMode = false;

                loadData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 更新上拉面板
                String label = DateUtils.formatDateTime(EventOverviewListActivity.this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                isLoadMoreMode = true;
                loadData();
            }
        };
        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(mOnRefreshListener2);

        mPullRefreshListView.setRefreshing(false);

        getFilterCriteria();
    }

    public void getFilterCriteria() {

        MmtBaseTask<String, Void, String[]> mmtBaseTask = new MmtBaseTask<String, Void, String[]>(EventOverviewListActivity.this, false) {

            @Override
            protected String[] doInBackground(String... params) {

                String[] result = new String[2];

                String url0 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/GetEventBoxType?userID="
                        + "0" + "&_mid=" + UUID.randomUUID().toString();
                        // + userIdStr + "&_mid=" + UUID.randomUUID().toString();

                String url1 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CommonModule/GetStationListByUserID?userID="
                        + userIdStr;

                result[0] = NetUtil.executeHttpGet(url0);
                result[1] = NetUtil.executeHttpGet(url1);

                return result;
            }

            @Override
            protected void onSuccess(String[] jsonResult) {

                ResultData<String> resultEventtype = Utils.json2ResultDataToast(String.class, EventOverviewListActivity.this,
                        jsonResult[0], "获取事件类型列表失败", false);

                ResultData<String> resultStation = Utils.json2ResultDataToast(String.class, EventOverviewListActivity.this,
                        jsonResult[1], "获取站点列表失败", false);

                if (resultEventtype != null) {
                    mFilterMenuBar.addMenuItem(EVENT_TYPE, resultEventtype.DataList.toArray(new String[resultEventtype.DataList.size()]), "");
                }

                if (resultStation != null) {
                    mFilterMenuBar.addMenuItem(STATION, resultStation.DataList.toArray(new String[resultStation.DataList.size()]), "");
                }

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
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/GetEventOverviewWithPaging")
                        .append("?pageSize=").append(pageSize)
                        .append("&pageIndex=").append(loadPageIndex)
                        .append("&userID=").append(userIdStr);

                if (!TextUtils.isEmpty(filterEventType)) {
                    sb.append("&eventName=").append(filterEventType);
                }
                if (!TextUtils.isEmpty(filterEventStatus)) {
                    sb.append("&eventState=").append(filterEventStatus);
                }
                if (!TextUtils.isEmpty(filterStation)) {
                    sb.append("&station=").append(filterStation);
                }

                if (!TextUtils.isEmpty(filterUpdateTime)) {
                    String[] dateRange = DateUtil.getDateSpanString(filterUpdateTime);
                    if (dateRange != null && !BaseClassUtil.isNullOrEmptyString(dateRange[0])
                            && !BaseClassUtil.isNullOrEmptyString(dateRange[1])) {
                        sb.append("&dateFrom=").append(Uri.encode(dateRange[0]))
                                .append("&dateTo=").append(Uri.encode(dateRange[1]));
                    }
                }

                if (!TextUtils.isEmpty(searchKey)) {
                    sb.append("&eventInfo=").append(searchKey);
                }

                sb.append("&sortFields=更新时间&direction=desc");

                sb.append("&sameStation=").append(String.valueOf(isSameStation));

                return NetUtil.executeHttpGet(sb.toString());
            }

            @Override
            protected void onPostExecute(String jsonResult) {

                mPullRefreshListView.onRefreshComplete();

                ResultData<EventItem> newData = Utils.json2ResultDataToast(EventItem.class, EventOverviewListActivity.this, jsonResult, "获取数据失败", true);
                if (newData == null) return;

                if (newData.DataList.size() == 0) {
                    if (!isLoadMoreMode) { // Refresh
                        Toast.makeText(EventOverviewListActivity.this, "没有记录", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    } else {               // LoadMore
                        Toast.makeText(EventOverviewListActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    if (isLoadMoreMode) {
                        currentPageIndex++; //加载更多成功更新当前页页码
                    } else {
                        Toast.makeText(EventOverviewListActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                    }
                    eventItemList.addAll(newData.DataList);

                    updateDistanceInfo();
                }
            }
        }.executeOnExecutor(MyApplication.executorService);
    }

    private void updateDistanceInfo() {

        final GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

        new MmtBaseTask<Void, Void, Void>(EventOverviewListActivity.this, false) {

            @Override
            protected Void doInBackground(Void... params) {

                if (xyz.isUsefull()) {
                    for (EventItem eventItem : eventItemList) {
                        calcDistance(eventItem, xyz);
                    }
                } else {
                    for (EventItem eventItem : eventItemList) {
                        eventItem.Distance = 0;
                        eventItem.DistanceStr = "未能获取当前坐标";
                    }
                }

                return null;
            }

            @Override
            protected void onSuccess(Void aVoid) {
                adapter.notifyDataSetChanged();
            }

        }.mmtExecute();
    }

    private final DecimalFormat decimalFormat = new DecimalFormat(".0");

    /**
     * 计算当前坐标与目标位置的距离，并设置当当前工单信息中
     */
    private void calcDistance(EventItem eventItem, GpsXYZ xyz) {

        if (BaseClassUtil.isNullOrEmptyString(eventItem.XY)) {
            eventItem.Distance = 0;
            eventItem.DistanceStr = "未含有坐标信息";
            return;
        }

        try {
            double x = Double.valueOf(eventItem.XY.split(",")[0]);
            double y = Double.valueOf(eventItem.XY.split(",")[1]);

            double gpsX = xyz.getX();
            double gpsY = xyz.getY();

            double distance = Math.sqrt((gpsX - x) * (gpsX - x) + (gpsY - y) * (gpsY - y));

            eventItem.Distance = distance;

            if (distance > 1000) {
                eventItem.DistanceStr = (decimalFormat.format(distance / 1000)) + "千米";
            } else {
                eventItem.DistanceStr = String.valueOf((int) distance) + "米";
            }

        } catch (Exception e) {
            eventItem.Distance = 0;
            eventItem.DistanceStr = "未含有坐标信息";
        }
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
}
