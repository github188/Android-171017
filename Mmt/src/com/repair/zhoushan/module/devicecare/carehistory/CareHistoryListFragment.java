package com.repair.zhoushan.module.devicecare.carehistory;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.devicecare.ScheduleTask;

import java.util.ArrayList;
import java.util.UUID;

public class CareHistoryListFragment extends Fragment {

    private PullToRefreshListView mPullRefreshListView;
    private ListView actualListView;
    private CareHistoryAdapter adapter;

    private ArrayList<ScheduleTask> data = new ArrayList<>();

    // For paging loading
    private int currentPageIndex = 1; // Start from 1
    private final int pageSize = 20;
    private boolean isLoadMoreMode = false;

    //当前页面第一个可见item，用于返回列表界面时恢复列表的滑动状态
    public int firstVisivleItemPos = 0;

    private String bizName;
    private String gisCode;
    private int deviceID;

    public static Fragment getInstance(String bizName, String gisCode, int deviceID) {
        CareHistoryListFragment fragment = new CareHistoryListFragment();
        Bundle args = new Bundle();
        args.putString("BizName", bizName);
        args.putString("GisCode", gisCode);
        args.putInt("DeviceID", deviceID);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            this.bizName = args.getString("BizName");
            this.gisCode = args.getString("GisCode");
            this.deviceID = args.getInt("DeviceID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.pulltorefresh_both, container, false);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.mainFormList);
        actualListView = mPullRefreshListView.getRefreshableView();

        mPullRefreshListView.setEmptyView(inflater.inflate(com.mapgis.mmt.R.layout.empty_default, null));

        adapter = new CareHistoryAdapter(getActivity(), data);
        registerForContextMenu(actualListView);
        actualListView.setAdapter(adapter);

        mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 每项的点击事件
        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                adapter.onItemClick(arg2 - 1);
            }
        });

        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 更新下拉面板
                String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(label);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                currentPageIndex = 1;
                isLoadMoreMode = false;
                loadData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 更新上拉面板
                String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                isLoadMoreMode = true;
                loadData();

            }
        });

        mPullRefreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 不滚动时保存当前滚动到的位置
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    firstVisivleItemPos = actualListView.getFirstVisiblePosition();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mPullRefreshListView.setRefreshing(false);
    }

    private void loadData() {

        final int loadPageIndex = isLoadMoreMode ? currentPageIndex + 1 : currentPageIndex;

        new MmtBaseTask<String, Void, ResultData<ScheduleTask>>(getActivity(), false, new MmtBaseTask.OnWxyhTaskListener<ResultData<ScheduleTask>>() {
            @Override
            public void doAfter(ResultData<ScheduleTask> resultData) {

                if (resultData.ResultCode != 200) {
                    Toast.makeText(getActivity(), resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<ScheduleTask> newData = resultData.DataList;

                if (!isLoadMoreMode) {

                    if (newData == null || newData.size() == 0) {
                        Toast.makeText(getActivity(), "没有记录", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "刷新成功", Toast.LENGTH_SHORT).show();
                    }

                    data.clear();
                    data.addAll(newData);
                    adapter.notifyDataSetChanged();

                } else {

                    if (newData == null || newData.size() == 0) {
                        Toast.makeText(getActivity(), "没有更多数据", Toast.LENGTH_SHORT).show();
                    } else {
                        currentPageIndex++; //加载更多成功更新当前页页码
                        data.addAll(newData);
                        adapter.notifyDataSetChanged();
                    }
                }

                mPullRefreshListView.onRefreshComplete();
            }
        }) {
            @Override
            protected ResultData<ScheduleTask> doInBackground(String... params) {

                ResultData<ScheduleTask> resultData;

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                        .append(userID).append("/ScheduleTasks")
                        .append("?_mid=").append(UUID.randomUUID().toString())
                        .append("&pageIndex=").append(loadPageIndex)
                        .append("&pageSize=").append(pageSize)
                        .append("&sortFields=开始时间&direction=desc")
                        .append("&bizName=").append(bizName)
                        .append("&gisCode=").append(gisCode)
                        .append("&deviceID=").append(deviceID);

                try {
                    String jsonResult = NetUtil.executeHttpGet(sb.toString());

                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取历史记录信息失败：网络错误");
                    }

                    Results<ScheduleTask> results = new Gson().fromJson(jsonResult, new TypeToken<Results<ScheduleTask>>() {
                    }.getType());
                    resultData = results.toResultData();

                } catch (Exception e) {
                    e.printStackTrace();
                    resultData = new ResultData<>();
                    resultData.ResultCode = -100;
                    resultData.ResultMessage = e.getMessage();
                }
                return resultData;
            }

        }.mmtExecute();

    }
}
