package com.repair.zhoushan.module.devicecare.careoverview;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DateUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.filtermenubar.FilterMenuBar;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.module.devicecare.ScheduleTask;

import java.util.ArrayList;
import java.util.Map;

public class CareListFragment extends Fragment {

    private boolean isViewInitialized;
    private boolean isDataInitialized;
    private boolean isVisibleToUser;

    private int showListFlag = 0;

    private PullToRefreshListView mPullRefreshListView;
    private ListView actualListView;
    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener2;
    private CareOverviewListAdapter adapter;

    private FilterMenuBar mFilterMenuBar;

    private View mProgressContainer;
    private View mListContainer;
    private boolean mListShown = true;

    private View emptyView;
    private TextView txtEmpty;

    // For paging loading
    private int currentPageIndex = 1; // Start from 1
    private final int pageSize = 20;
    private boolean isLoadMoreMode = false;

    private ArrayList<ScheduleTask> dataList = new ArrayList<>();
    private String bizName;

    private String filterTime = ""; // 开始时间
    private String filterState = ""; // 任务状态
    private String filterStation = ""; // 场站名称
    private String filterDevice = ""; // 设备类型
    private String filterArea = ""; // 区域
    private String filterCarePerson = ""; // 养护人
    private String filterCompType = ""; // 部件类型

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        this.isVisibleToUser = isVisibleToUser;
        prepareFetchData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isViewInitialized = true;
        prepareFetchData();
    }

    private void prepareFetchData() {
        if (isViewInitialized && isVisibleToUser && !isDataInitialized) {
            isDataInitialized = true;

            getFilterCriteria();
            refresh();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Searchable) {
            this.searchable = (Searchable) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey("BizName")) {
            bizName = args.getString("BizName");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_care_overview_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFilterMenuBar = (FilterMenuBar) view.findViewById(R.id.mMenuBar);
        mFilterMenuBar.setMenuItems(new String[]{FetchFilterCriteriaTask.FILTER_START_TIME, FetchFilterCriteriaTask.FILTER_TASK_STATE},
                new String[][]{{"昨天", "今天", "本周", "上周", "本月", "上月"}, {"未审核", "待审核", "待分派", "已分派", "已完成", "退单", "未通过"}},
                new String[]{filterTime, filterState});

        mProgressContainer = view.findViewById(R.id.progressContainer);
        mListContainer = view.findViewById(R.id.listContainer);

        emptyView = view.findViewById(R.id.ptrEmptyView);
        emptyView.setVisibility(View.GONE);
        txtEmpty = (TextView) view.findViewById(android.R.id.text1);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.ptrListView);
        actualListView = mPullRefreshListView.getRefreshableView();
        mPullRefreshListView.setEmptyView(emptyView);

        adapter = new CareOverviewListAdapter(getActivity(), dataList);
        mPullRefreshListView.setAdapter(adapter);

        mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");


        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.onItemSelected(position);
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

                refresh();
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
        };
        mPullRefreshListView.setOnRefreshListener(mOnRefreshListener2);

        mFilterMenuBar.setOnMenuItemSelectedListener(new FilterMenuBar.OnMenuItemSelectedListener() {
            @Override
            public void onItemSelected(Map<String, String> selectResult) {
                filterTime = selectResult.get(FetchFilterCriteriaTask.FILTER_START_TIME); // 开始时间
                filterState = selectResult.get(FetchFilterCriteriaTask.FILTER_TASK_STATE); // 任务状态
                filterStation = selectResult.get(FetchFilterCriteriaTask.FILTER_STATION_NAME); // 场站名称
                filterDevice = selectResult.get(FetchFilterCriteriaTask.FILTER_DEVICE_TYPE); // 设备类型
                filterArea = selectResult.get(FetchFilterCriteriaTask.FILTER_AREA_NAME); // 区域
                filterCarePerson = selectResult.get(FetchFilterCriteriaTask.FILTER_CARE_PERSON); // 养护人
                filterCompType = selectResult.get(FetchFilterCriteriaTask.FILTER_COMP_TYPE); // 部件类型

                refreshData();
            }
        });

        setListShown(false);
    }

    private void refresh() {
        currentPageIndex = 1;
        isLoadMoreMode = false;
        loadData();
    }

    // 被动触发刷新时显示进度条
    private boolean showLoading = false;

    private void loadData() {

        final int loadPageIndex = isLoadMoreMode ? currentPageIndex + 1 : currentPageIndex;

        final String searchKey = searchable != null ? searchable.getSearchKey() : "";

        MmtBaseTask<String, Void, Results<ScheduleTask>> mmtBaseTask =
                new MmtBaseTask<String, Void, Results<ScheduleTask>>(getActivity(), showLoading) {

                    @Override
                    protected Results<ScheduleTask> doInBackground(String... params) {
                        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());

                        switch (bizName) {
                            case "工商户安检":
                            case "调压器养护":
                            case "阀门养护":
                            case "防腐层检测":
                            case "阴极保护桩检测":
                            case "工商户抄表":
                            case "工商户表具检定":
                                sb.append(String.format("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/%s/ScheduleTasks", userID));
                                break;
                            case "场站设备检定":
                            case "车用设备检定":
                            case "场站设备":
                            case "车用设备":
                                sb.append(String.format("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/%s/StationScheduleTasks", userID));
                                break;
                        }

                        sb.append("?bizName=").append(bizName)
                                .append("&pageIndex=").append(loadPageIndex)
                                .append("&pageSize=").append(pageSize)
                                .append("&sortFields=开始时间&direction=desc")
                                .append("&taskInfo=").append(searchKey)
                                .append(getRequestParams());

                        Results<ScheduleTask> resultData;

                        try {
                            String jsonResult = NetUtil.executeHttpGet(sb.toString());

                            if (TextUtils.isEmpty(jsonResult)) {
                                throw new Exception("获取列表数据失败");
                            }

                            resultData = new Gson().fromJson(jsonResult, new TypeToken<Results<ScheduleTask>>() {
                            }.getType());

                        } catch (Exception e) {
                            e.printStackTrace();
                            resultData = new Results<>("1001", e.getMessage());
                        }

                        return resultData;
                    }

                    @Override
                    protected void onSuccess(Results<ScheduleTask> resultData) {
                        mPullRefreshListView.onRefreshComplete();
                        showLoading = false;

                        showListFlag |= 1;
                        if (showListFlag == 3) {
                            setListShown(true);
                        }

                        ResultData<ScheduleTask> result = resultData.toResultData();
                        if (result.ResultCode != 200) {
                            Toast.makeText(getActivity(), result.ResultMessage, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (result.DataList.size() == 0) {
                            if (!isLoadMoreMode) { // Refresh
                                Toast.makeText(getActivity(), "没有记录", Toast.LENGTH_SHORT).show();
                                setEmptyText("没有记录");
                                dataList.clear();
                                adapter.notifyDataSetChanged();
                            } else {               // LoadMore
                                Toast.makeText(getActivity(), "没有更多数据", Toast.LENGTH_SHORT).show();
                            }
                        } else {

                            if (isLoadMoreMode) {
                                currentPageIndex++; //加载更多成功更新当前页页码
                            } else {
                                dataList.clear();
                                Toast.makeText(getActivity(), "刷新成功", Toast.LENGTH_SHORT).show();
                            }

                            dataList.addAll(result.DataList);

                            adapter.notifyDataSetChanged();
                        }
                    }
                };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private String getRequestParams() {

        StringBuilder sb = new StringBuilder();

        // 开始时间
        if (!TextUtils.isEmpty(filterTime)) {
            String[] dateRange = DateUtil.getDateSpanString(filterTime);
            if (dateRange != null && !BaseClassUtil.isNullOrEmptyString(dateRange[0])
                    && !BaseClassUtil.isNullOrEmptyString(dateRange[1])) {
                sb.append("&dateFrom=").append(Uri.encode(dateRange[0]))
                        .append("&dateTo=").append(Uri.encode(dateRange[1]));
            }
        }
        if (!TextUtils.isEmpty(filterState)) {
            sb.append("&checkState=").append(filterState); // 任务状态
        }
        if (!TextUtils.isEmpty(filterStation)) {
            sb.append("&stationName=").append(filterStation); // 场站名称
        }
        if (!TextUtils.isEmpty(filterDevice)) {
            sb.append("&equipmentType=").append(filterDevice); // 设备类型
        }
        if (!TextUtils.isEmpty(filterArea)) {
            sb.append("&area=").append(filterArea); // 区域
        }
        if (!TextUtils.isEmpty(filterCarePerson)) {
            sb.append("&cureMan=").append(filterCarePerson); // 养护人
        }
        if (!TextUtils.isEmpty(filterCompType)) {
            sb.append("&partType=").append(filterCompType); // 部件类型
        }

        return sb.toString();
    }

    private void getFilterCriteria() {

        new FetchFilterCriteriaTask(getActivity(), false, false, new MmtBaseTask.OnWxyhTaskListener<String[]>() {
            @Override
            public void doAfter(String[] jsonResult) {

                ResultData<String> resultStationName = null;
                ResultData<String> resultDeviceType = null;
                ResultData<String> resultAreaName = null;
                ResultData<UserInfo> resultCarePerson = null;

                for (int i = jsonResult.length - 2; i >= 0; i -= 2) {

                    switch (jsonResult[i]) {
                        case FetchFilterCriteriaTask.FILTER_STATION_NAME:
                            resultStationName = Utils.json2ResultDataToast(String.class,
                                    getActivity(), jsonResult[i + 1], "获取场站名称列表失败", false);
                            break;
                        case FetchFilterCriteriaTask.FILTER_DEVICE_TYPE:
                            resultDeviceType = Utils.json2ResultDataToast(String.class,
                                    getActivity(), jsonResult[i + 1], "获取设备类型列表失败", false);
                            break;
                        case FetchFilterCriteriaTask.FILTER_AREA_NAME:
                            resultAreaName = Utils.json2ResultDataToast(String.class,
                                    getActivity(), jsonResult[i + 1], "获取区域列表失败", false);
                            break;
                        case FetchFilterCriteriaTask.FILTER_CARE_PERSON:
                            resultCarePerson = Utils.json2ResultDataToast(UserInfo.class,
                                    getActivity(), jsonResult[i + 1], "获取养护人列表失败", false);
                            break;
                    }
                }

                if (resultStationName != null) {
                    mFilterMenuBar.addMenuItem(FetchFilterCriteriaTask.FILTER_STATION_NAME,
                            resultStationName.DataList.toArray(new String[resultStationName.DataList.size()]), filterStation);
                }

                if (resultDeviceType != null) {
                    mFilterMenuBar.addMenuItem(FetchFilterCriteriaTask.FILTER_DEVICE_TYPE,
                            resultDeviceType.DataList.toArray(new String[resultDeviceType.DataList.size()]), filterDevice);
                }

                if (resultAreaName != null) {
                    mFilterMenuBar.addMenuItem(FetchFilterCriteriaTask.FILTER_AREA_NAME,
                            resultAreaName.DataList.toArray(new String[resultAreaName.DataList.size()]), filterArea);
                }

                if (resultCarePerson != null) {
                    String[] carePersonNames = new String[resultCarePerson.DataList.size()];
                    for (int i = 0; i < carePersonNames.length; i++) {
                        carePersonNames[i] = resultCarePerson.DataList.get(i).userName;
                    }
                    mFilterMenuBar.addMenuItem(FetchFilterCriteriaTask.FILTER_CARE_PERSON, carePersonNames, filterCarePerson);
                }

                showListFlag |= 2;
                if (showListFlag == 3) {
                    setListShown(true);
                }
            }
        }).mmtExecute(bizName);
    }

    private void setEmptyText(String emptyText) {
        if (txtEmpty != null) {
            txtEmpty.setText(emptyText);
        }
    }

    private void setListShown(boolean shown) {

        if (mListShown == shown) {
            return;
        }
        mListShown = shown;

        if (shown) {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), android.R.anim.fade_out));
            mListContainer.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), android.R.anim.fade_in));

            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), android.R.anim.fade_in));
            mListContainer.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), android.R.anim.fade_out));

            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
    }

    public void refreshData() {
        if (mPullRefreshListView != null && mOnRefreshListener2 != null) {
            showLoading = true;
            mOnRefreshListener2.onPullDownToRefresh(mPullRefreshListView);
            actualListView.setSelection(0);
        }
    }

    public interface Searchable {
        String getSearchKey();
    }

    private Searchable searchable;

}