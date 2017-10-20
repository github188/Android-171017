package com.repair.zhoushan.module.eventmanage.eventbox;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.filtermenubar.FilterMenuBar;
import com.filtermenubar.Node;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.maintainproduct.v2.module.EventTypeItem;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DateUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;
import com.repair.common.BaseTaskResults;
import com.repair.common.CaseSearchActivity;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.entity.EventItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.mapgis.mmt.common.util.BaseClassUtil.isNullOrEmptyString;

public class EventListFragment extends Fragment {

    public static final String TAG = "EventListFragment";
    private static final String FLAG_ALL = "全部";

    // 两种模式：领单模式、调度箱模式
    private int mMode;
    private String title;
    // 事件箱配置模块参数指定事件名称
    private String defaultEventNames;

    protected PullToRefreshListView mPullRefreshListView;
    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener2;
    protected ListView actualListView;
    protected EventAdapter adapter;
    private EditText txtSearch; // ActionBar搜索框

    private ArrayList<EventItem> eventItemList = new ArrayList<>();
    private FilterMenuBar mFilterMenuBar;

    // For paging loading
    private int currentPageIndex = 1; // Start from 1
    private final int pageSize = 10;
    private boolean isLoadMoreMode = false;

    // 当前页面第一个可见item，用于返回列表界面时恢复列表的滑动状态
    public int firstVisibleItemPosition = 0;
    private boolean isSwipeToRefresh = true;

    private final String filterGroupEventType = "事件类型";
    private final String filterGroupUpdateTime = "更新时间";
    private final String filterGroupEventStatus = "事件状态";
    private final String filterGroupBelongArea = "所属区域";

    private String filterValueEventStatus = FLAG_ALL;
    private String filterValueEventType = FLAG_ALL;
    private String filterValueUpdateTime = FLAG_ALL;
    private String filterValueBelongArea = FLAG_ALL;

    // 领单箱仅展示待处理、待审核的事件，并且没有事件状态过滤功能
    private final String eventStatusInReceiveMode = "待处理,待审核";

    public static EventListFragment newInstance(int workMode, String showTitle, String defaultEventNames) {
        EventListFragment fragment = new EventListFragment();
        Bundle bundle = new Bundle();
        // 设置工作模式，默认"调度箱模式"
        bundle.putInt("MODE", workMode);
        bundle.putString("Title", showTitle);
        bundle.putString("DefaultEventNames", defaultEventNames);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        this.mMode = args.getInt("MODE", EventListActivity.Mode.DISPATCH);
        this.title = args.getString("Title");
        this.defaultEventNames = args.getString("DefaultEventNames");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.event_manage_list, container, false);

        this.mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listViewEvents);
        this.actualListView = mPullRefreshListView.getRefreshableView();
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        // mPullRefreshListView.setEmptyView(inflater.inflate(R.layout.empty_default, null));
        registerForContextMenu(actualListView);

        initActionBar(view);
        initFilterMenuBar(view);

        mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");
        // 上拉加载更多时的提示文本设置
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");

        this.adapter = new EventAdapter(getActivity(), eventItemList, mMode);
        adapter.setHostFragment(this);
        actualListView.setAdapter(adapter);

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
                String label = DateUtils.formatDateTime(getContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
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
                String label = DateUtils.formatDateTime(getContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                isLoadMoreMode = true;
                loadData();
            }
        };
        // 给 listView添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(mOnRefreshListener2);
        mPullRefreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 不滚动时保存当前滚动到的位置
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    firstVisibleItemPosition = actualListView.getFirstVisiblePosition();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPullRefreshListView.setRefreshing(false);
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
        txtSearch.setHint("事件编号、摘要、上报人、上报部门");
        txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CaseSearchActivity.class);
                // key 为搜索框的当前值
                intent.putExtra("key", txtSearch.getText().toString());
                intent.putExtra("searchHint", "事件编号、摘要、上报人、上报部门");
                intent.putExtra("searchHistoryKey", "EventListSearchHistory_" + title);
                EventListFragment.this.startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });

    }

    private void initFilterMenuBar(View view) {
        this.mFilterMenuBar = (FilterMenuBar) view.findViewById(R.id.filterView);

        if (mMode == EventListActivity.Mode.RECEIVE) {
            filterValueEventStatus = eventStatusInReceiveMode;
        } else {
            filterValueEventStatus = "待处理";
            Node stateNode = Node.createSimpleTree(filterGroupEventStatus,
                    Arrays.asList(FLAG_ALL, "待处理", "处理中", "待审核", "无效", "已处理"), filterValueEventStatus);
            mFilterMenuBar.appendMenuItem(stateNode);
        }
        Node timeNode = Node.createSimpleTree(filterGroupUpdateTime,
                Arrays.asList(FLAG_ALL, "昨天", "本周", "上周", "本月", "上月"), null);
        mFilterMenuBar.appendMenuItem(timeNode);

        mFilterMenuBar.setOnFilterItemSelectedListener(new FilterMenuBar.OnFilterItemSelectedListener() {
            @Override
            public void onFilterItemSelected(List<List<Node>> selectedGroups, int invokedGroupIndex) {
                resolveFilter(selectedGroups);
                refreshData();

            }
        });

        getFilterCriteria();
    }

    private void resolveFilter(List<List<Node>> selectedGroups) {

        // reset filter value
        filterValueEventType = FLAG_ALL;
        filterValueUpdateTime = FLAG_ALL;
        filterValueEventStatus = FLAG_ALL;
        filterValueBelongArea = FLAG_ALL;

        // resolve selection
        for (List<Node> group : selectedGroups) {
            String groupName = group.get(0).getShowName();
            String groupValue = group.get(group.size() - 1).getShowName();
            switch (groupName) {
                case filterGroupEventStatus:
                    filterValueEventStatus = groupValue;
                    break;
                case filterGroupUpdateTime:
                    filterValueUpdateTime = groupValue;
                    break;
                case filterGroupEventType:
                    filterValueEventType = groupValue;
                    break;
                case filterGroupBelongArea:
                    filterValueBelongArea = groupValue;
                    break;
            }
        }
    }

    private void getFilterCriteria() {

        // 事件类型列表
        if (!TextUtils.isEmpty(defaultEventNames)) {
            List<String>  eventNames = new ArrayList<>(BaseClassUtil.StringToList(defaultEventNames, ","));
            eventNames.add(0, FLAG_ALL);
            Node eventTypeNode = Node.createSimpleTree(filterGroupEventType, eventNames, null);
            mFilterMenuBar.appendMenuItem(eventTypeNode);
        } else {
            BaseTaskResults<String, Void, String> eventTypeTask = new BaseTaskResults<String, Void, String>(getContext()) {
                @NonNull
                @Override
                protected String getRequestUrl() throws Exception {
                    return ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/GetEventBoxType"
                            + "?userID=" + userID + "&_mid=" + UUID.randomUUID().toString();
                }

                @Override
                protected void onSuccess(Results<String> results) {
                    ResultData<String> resultData = results.toResultData();
                    if (resultData.ResultCode != ResultData.SUCCEED || resultData.DataList.size() == 0) {
                        Toast.makeText(context, "获取事件类型列表失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    resultData.DataList.add(0, FLAG_ALL);
                    Node eventTypeNode = Node.createSimpleTree(filterGroupEventType, resultData.DataList, null);
                    mFilterMenuBar.appendMenuItem(eventTypeNode);
                }
            };
            eventTypeTask.mmtExecute();
        }

        // 领单箱才有所属区域列表
        if (mMode == EventListActivity.Mode.RECEIVE) {
            BaseTaskResults<String, Void, EventTypeItem> belongAreaTask = new BaseTaskResults<String, Void, EventTypeItem>(getContext()) {
                @NonNull
                @Override
                protected String getRequestUrl() throws Exception {
                    return ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc"
                            + "/EventManage/GetEventType?pName=所属区域&cName=所属区域内容";
                }

                @Override
                protected void onSuccess(Results<EventTypeItem> results) {
                    ResultData<EventTypeItem> resultData = results.toResultData();

                    // 只有当"数据字典"中存在该配置项时才给予显示
                    if (resultData.ResultCode != ResultData.SUCCEED || resultData.DataList.size() == 0) {
                        return;
                    }
                    Node belongAreaNode = new Node(filterGroupBelongArea, filterGroupBelongArea);
                    belongAreaNode.addChild(new Node(FLAG_ALL, FLAG_ALL));
                    for (EventTypeItem item : resultData.DataList) {
                        Node node = new Node(item.NODENNAME, item.NODEID);
                        belongAreaNode.addChild(node);
                        for (EventTypeItem subItem : item.SubItem) {
                            node.addChild(new Node(subItem.NODENNAME, subItem.NODEID));
                        }
                    }
                    mFilterMenuBar.appendMenuItem(belongAreaNode);
                }
            };
            belongAreaTask.mmtExecute();
        }
    }

    private void loadData() {

        final int loadPageIndex = isLoadMoreMode ? currentPageIndex + 1 : currentPageIndex;
        final String searchKey = txtSearch.getText().toString().trim();

        // 非滑动刷新需要显示进度条
        MmtBaseTask<String, Void, ResultData<EventItem>> mmtBaseTask = new MmtBaseTask<String, Void, ResultData<EventItem>>(getActivity(), !isSwipeToRefresh) {
            @Override
            protected ResultData<EventItem> doInBackground(String... params) {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/GetEventBoxWithPaging")
                        .append("?pageSize=").append(pageSize)
                        .append("&pageIndex=").append(loadPageIndex)
                        .append("&userID=").append(userID);

                if (filterValueEventType.equals(FLAG_ALL)) {
                    if (!TextUtils.isEmpty(defaultEventNames)) {
                        sb.append("&eventName=").append(defaultEventNames);
                    }
                } else {
                    sb.append("&eventName=").append(filterValueEventType);
                }
                if (!filterValueUpdateTime.equals(FLAG_ALL)) {
                    String[] dateRange = DateUtil.getDateSpanString(filterValueUpdateTime);
                    if (dateRange != null && !isNullOrEmptyString(dateRange[0])
                            && !isNullOrEmptyString(dateRange[1])) {
                        sb.append("&dateFrom=").append(Uri.encode(dateRange[0]))
                                .append("&dateTo=").append(Uri.encode(dateRange[1]));
                    }
                }
                if (mMode == EventListActivity.Mode.RECEIVE) {
                    sb.append("&eventState=").append(eventStatusInReceiveMode);
                } else if (!filterValueEventStatus.equals(FLAG_ALL)) {
                    sb.append("&eventState=").append(filterValueEventStatus);
                }
                if (!filterValueBelongArea.equals(FLAG_ALL)) {
                    sb.append("&area=").append(filterValueBelongArea);
                }
                if (!TextUtils.isEmpty(searchKey)) {
                    sb.append("&eventInfo=").append(searchKey);
                }
                sb.append("&sortFields=更新时间&direction=desc");

                ResultData<EventItem> resultData;
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
                isSwipeToRefresh = true;
                mPullRefreshListView.onRefreshComplete();

                if (resultData.DataList == null || resultData.DataList.size() == 0) {
                    if (!isLoadMoreMode) { // Refresh
                        Toast.makeText(getContext(), "没有记录", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    } else {               // LoadMore
                        Toast.makeText(getContext(), "没有更多数据", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (isLoadMoreMode) {
                        currentPageIndex++; //加载更多成功更新当前页页码
                    } else {
                        Toast.makeText(getContext(), "刷新成功", Toast.LENGTH_SHORT).show();
                    }
                    eventItemList.addAll(resultData.DataList);

                    updateDistanceInfo();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void updateDistanceInfo() {
        final GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();
        new MmtBaseTask<Void, Void, Void>(getContext(), false) {
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
                Collections.sort(eventItemList, setTopComparator);
                return null;
            }

            @Override
            protected void onSuccess(Void aVoid) {
                adapter.notifyDataSetChanged();
            }
        }.mmtExecute();
    }

    // 置顶排序用的比较器
    private final Comparator<EventItem> setTopComparator = new Comparator<EventItem>() {
        @Override
        public int compare(EventItem lhs, EventItem rhs) {
            return lhs.IsStick == rhs.IsStick ? 0 : (lhs.IsStick - rhs.IsStick < 0 ? 1 : -1);
        }
    };

    private final DecimalFormat decimalFormat = new DecimalFormat(".0");

    /**
     * 计算当前坐标与目标位置的距离，并设置当当前工单信息中
     */
    private void calcDistance(EventItem eventItem, GpsXYZ xyz) {

        if (isNullOrEmptyString(eventItem.XY)) {
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

    public void refreshData() {
        if (mPullRefreshListView != null && mOnRefreshListener2 != null) {
            isSwipeToRefresh = false;
            mOnRefreshListener2.onPullDownToRefresh(mPullRefreshListView);
            actualListView.setSelection(0);
        }
    }

    public void refreshView() {
        if (actualListView != null) {
            eventItemList.remove(adapter.getCurClickPos());
            adapter.notifyDataSetChanged();
            actualListView.setSelection(firstVisibleItemPosition);
        }
    }
}
