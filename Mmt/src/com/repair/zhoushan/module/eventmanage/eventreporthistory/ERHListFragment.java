package com.repair.zhoushan.module.eventmanage.eventreporthistory;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.filtermenubar.FilterMenuBar;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.R;
import com.repair.common.CaseSearchActivity;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.entity.EventItem;

import java.util.ArrayList;
import java.util.Map;

public class ERHListFragment extends Fragment {

    public static final String TAG = "ERHListFragment";
    public static final String PARAM_NAME_DEFAULT_EVENT_NAME = "DefaultEventNames";
    private static final String EMPTY_STRING = "";

    protected final ArrayList<EventItem> data = new ArrayList<>();
    private String defaultEventNames;

    private final String EVENT_STATE = "事件状态";
    private final String EVENT_TYPE = "事件类型";
    private final String EVENT_SORT_TYPE = "排序方式";
    private String filterEventSortType = "上报时间由近到远";
    private String filterEventState = EMPTY_STRING;
    private String filterEventType = EMPTY_STRING;

    private FilterMenuBar mFilterMenuBar;

    protected PullToRefreshListView mPullRefreshListView;
    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener2;
    protected ListView actualListView;
    public ERHAdapter adapter;

    private EditText txtSearch; // ActionBar搜索框

    // For paging loading
    private int currentPageIndex = 1; // Start from 1
    private final int pageSize = 20;

    // 当前页面第一个可见item，用于返回列表界面时恢复列表的滑动状态
    public int firstVisibleItemPos = 0;
    private boolean showLoading = false;

    public static ERHListFragment newInstance(String defaultEventNames) {
        Bundle args = new Bundle();
        args.putString(PARAM_NAME_DEFAULT_EVENT_NAME, defaultEventNames);
        ERHListFragment fragment = new ERHListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(PARAM_NAME_DEFAULT_EVENT_NAME)) {
            this.defaultEventNames = args.getString(PARAM_NAME_DEFAULT_EVENT_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.doingbox_list_activity, container, false);

        initActionBar(view);

        this.mFilterMenuBar = (FilterMenuBar) view.findViewById(R.id.mMenuBar);
        mFilterMenuBar.setMenuItems(
                new String[]{EVENT_SORT_TYPE, EVENT_STATE},
                new String[][]{{"上报时间由近到远"}, {"待处理", "处理中", "无效", "已关闭"}},
                new String[]{filterEventSortType, filterEventState});

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.maintenanceFormList);
        actualListView = mPullRefreshListView.getRefreshableView();
        registerForContextMenu(actualListView);

        adapter = new ERHAdapter(getActivity(), data);
        actualListView.setAdapter(adapter);

        // 下拉刷新时的提示文本设置
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");
        // 上拉加载更多时的提示文本设置
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");

        return view;
    }

    private void initActionBar(View view) {

        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.btnOtherAction).setVisibility(View.GONE);

        this.txtSearch = (EditText) view.findViewById(R.id.txtSearch);
        txtSearch.setVisibility(View.VISIBLE);
        txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), CaseSearchActivity.class);

                // key 为搜索框的当前值
                intent.putExtra("key", txtSearch.getText().toString());
                intent.putExtra("searchHint", "事件编号、上报部门、上报人、摘要");
                intent.putExtra("searchHistoryKey", "EventReportHistoryList");

                startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        getFilterCriteria();

        // 过滤条件
        mFilterMenuBar.setOnMenuItemSelectedListener(new FilterMenuBar.OnMenuItemSelectedListener() {
            @Override
            public void onItemSelected(Map<String, String> selectResult) {

                filterEventSortType = selectResult.get(EVENT_SORT_TYPE); // 列表排序排序方式
                filterEventType = selectResult.get(EVENT_TYPE); // 事件类型
                filterEventState = selectResult.get(EVENT_STATE); // 事件状态

                refreshData();
            }
        });

        // 每项的点击事件
        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                adapter.onItemClick(arg2 - 1);
            }
        });

        mOnRefreshListener2 = new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 更新下拉面板
                String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(label);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                loadData(false);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 更新上拉面板
                String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                loadData(true);
            }
        };

        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(mOnRefreshListener2);

        mPullRefreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 不滚动时保存当前滚动到的位置
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    firstVisibleItemPos = actualListView.getFirstVisiblePosition();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mPullRefreshListView.setRefreshing(false);
    }

    public void getFilterCriteria() {

        if (TextUtils.isEmpty(defaultEventNames)) {
            MmtBaseTask<String, Void, ResultData<String>> mmtBaseTask = new MmtBaseTask<String, Void, ResultData<String>>(getActivity(), false) {

                @Override
                protected ResultData<String> doInBackground(String... params) {

                    ResultData<String> resultData;

                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/GetEventBoxType?userID=0"; // 传userID为0取出所有的类型

                    try {
                        String jsonResult = NetUtil.executeHttpGet(url);
                        if (TextUtils.isEmpty(jsonResult)) {
                            throw new Exception("获取事件类型列表失败：网络错误");
                        }

                        Results<String> results = new Gson().fromJson(jsonResult, new TypeToken<Results<String>>() {
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

                @Override
                protected void onSuccess(ResultData<String> resultData) {

                    if (resultData.ResultCode != 200) {
                        Toast.makeText(getActivity(), resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        mFilterMenuBar.addMenuItem(EVENT_TYPE, resultData.DataList.toArray(new String[resultData.DataList.size()]), "");
                    }
                }
            };
            mmtBaseTask.setCancellable(false);
            mmtBaseTask.mmtExecute();
        } else {
            String[] eventNames = defaultEventNames.split(",");
            mFilterMenuBar.addMenuItem(EVENT_TYPE, eventNames, "");
        }
    }

    private void loadData(final boolean isLoadMoreMode) {

        final int loadPageIndex = isLoadMoreMode ? currentPageIndex + 1 : 1;

        final String searchKey = txtSearch.getText().toString().trim();

        // 滑动刷新不显示进度条、外部调用刷新需要显示进度条
        MmtBaseTask<String, Void, ResultData<EventItem>> mmtBaseTask = new MmtBaseTask<String, Void, ResultData<EventItem>>(getActivity(), showLoading) {
            @Override
            protected ResultData<EventItem> doInBackground(String... params) {

                ResultData<EventItem> resultData;

                StringBuilder sb = new StringBuilder();
                sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                        .append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/GetEventHistoryWithPaging")
                        .append("?pageSize=").append(pageSize)
                        .append("&pageIndex=").append(loadPageIndex)
                        .append("&userID=").append(userID);

                if (!TextUtils.isEmpty(searchKey)) {
                    sb.append("&eventInfo=").append(Uri.encode(searchKey));
                }
                if (!TextUtils.isEmpty(filterEventType)) {
                    sb.append("&eventName=").append(filterEventType);
                } else if (!TextUtils.isEmpty(defaultEventNames)) {
                    sb.append("&eventName=").append(defaultEventNames);
                }
                if (!TextUtils.isEmpty(filterEventState)) {
                    sb.append("&eventState=").append(filterEventState);
                }
                if ("上报时间由近到远".equals(filterEventSortType)) {
                    sb.append("&sortFields=上报时间&direction=desc");
                } else {
                    sb.append("&sortFields=上报时间&direction=desc");
                }

                try {
                    String jsonResult = NetUtil.executeHttpGet(sb.toString());

                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取事件列表失败：网络错误");
                    }

                    Results<EventItem> results = new Gson().fromJson(jsonResult, new TypeToken<Results<EventItem>>() {
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

            @Override
            protected void onSuccess(ResultData<EventItem> resultData) {
                mPullRefreshListView.onRefreshComplete();
                showLoading = false;

                if (resultData.ResultCode != 200) {
                    Toast.makeText(getActivity(), resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isLoadMoreMode) { // Refresh

                    currentPageIndex = 1;
                    data.clear();
                    data.addAll(resultData.DataList);
                    adapter.notifyDataSetChanged();

                    if (resultData.DataList == null || resultData.DataList.size() == 0) {
                        Toast.makeText(getActivity(), "没有记录", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "刷新成功", Toast.LENGTH_SHORT).show();
                    }
                } else {  // LoadMore

                    if (resultData.DataList == null || resultData.DataList.size() == 0) {
                        Toast.makeText(getActivity(), "没有更多数据", Toast.LENGTH_SHORT).show();
                    } else {

                        currentPageIndex++; //加载更多成功更新当前页页码
                        data.addAll(resultData.DataList);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    /**
     * 刷新列表重新请求数据
     */
    public void refreshData() {
        if (mPullRefreshListView != null && mOnRefreshListener2 != null) {
            showLoading = true;
            mOnRefreshListener2.onPullDownToRefresh(mPullRefreshListView);
            actualListView.setSelection(0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 有数据处理完毕后,会到该界刷新数据
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.DEFAULT_REQUEST_CODE) {

            refreshData();

        } else if (resultCode == 1 && requestCode == Constants.SEARCH_REQUEST_CODE) {
            // 搜索界面返回的搜索关键字
            String key = data.getStringExtra("key"); //返回空代表查全部
            txtSearch.setText(key);

            refreshData();
        }
    }
}
