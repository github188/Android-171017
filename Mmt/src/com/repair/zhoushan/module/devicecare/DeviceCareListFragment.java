package com.repair.zhoushan.module.devicecare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.filtermenubar.FilterMenuBar;
import com.filtermenubar.Node;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultTableData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.repair.common.CaseSearchActivity;
import com.repair.zhoushan.module.QRCodeResolver;
import com.repair.zhoushan.module.devicecare.qrcodefeedback.ZSQRCodeResolver;
import com.zbar.lib.CaptureActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceCareListFragment extends Fragment {

    public static final int TASK_SEARCH_REQUEST_CODE = 0x110;
    public static final int TASK_DETAIL_REQUEST_CODE = 0x101;
    public static final int TASK_QRCODE_SCAN_REQUEST_CODE = 0x102;

    private int userId;

    private String mQRCodeScanConfig;
    private QRCodeResolver mQRCodeResolver;

    protected PullToRefreshListView mPullRefreshListView;

    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener2;
    protected ListView actualListView;
    protected ScheduleTaskAdapter adapter;
    private EditText txtSearch;

    private ArrayList<ScheduleTask> dataList = new ArrayList<ScheduleTask>();

    // For paging loading
    private int currentPageIndex = 1; // Start from 1
    private final int pageSize = 50;
    private boolean isLoadMoreMode = false;

    private String bizName;

    //region 排序相关字段

    // 当前坐标
    private GpsXYZ currentCoordinate;
    // 被动触发刷新时显示进度条
    private boolean loadByOutside = false;

    //endregion

    //region Variable for Filter

    private final String filterGroupTaskState = "任务状态";
    private final String filterGroupSortType = "任务排序";

    private static final String TASK_STATE_DISPATCHED = "已分派";
    private static final String TASK_STATE_DONE = "已完成";

    private String filterValueTaskState = TASK_STATE_DISPATCHED;
    private String filterValueSortType = "距离由近到远";

    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.userId = MyApplication.getInstance().getUserId();
        this.bizName = getArguments().getString("BizName");
        this.mQRCodeScanConfig = MyApplication.getInstance().getConfigValue("QRCodeScanInFeedback");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.devicecare_list, container, false);
        initActionBar(view);
        initFilterView(view);
        initContentView(view);
        return view;
    }

    private void initFilterView(View view) {

        // 初始化 ExpandTabView
        FilterMenuBar filterTabView = (FilterMenuBar) view.findViewById(R.id.filterView);

        filterValueTaskState = TASK_STATE_DISPATCHED;
        Node taskStateNode = Node.createSimpleTree(filterGroupTaskState,
                Arrays.asList(TASK_STATE_DISPATCHED, TASK_STATE_DONE), filterValueTaskState);

        filterValueSortType = "距离由近到远";
        Node sortTypeNode = Node.createSimpleTree(filterGroupSortType,
                Arrays.asList("距离由近到远", "时间由近到远", "时间由远到近", "上次完成时间由远及近"),
                filterValueSortType);

        filterTabView.setMenuItems(Arrays.asList(taskStateNode, sortTypeNode));
        filterTabView.setOnFilterItemSelectedListener(new FilterMenuBar.OnFilterItemSelectedListener() {
            @Override
            public void onFilterItemSelected(List<List<Node>> selectedGroups, int invokedGroupIndex) {

                // resolve selection
                for (List<Node> group : selectedGroups) {
                    String groupName = group.get(0).getShowName();
                    String groupValue = group.get(group.size() - 1).getShowName();
                    switch (groupName) {
                        case filterGroupTaskState:
                            filterValueTaskState = groupValue;
                            break;
                        case filterGroupSortType:
                            filterValueSortType = groupValue;
                            break;
                    }
                }
                // refresh listView
                updateData();
            }
        });
    }

    private void initActionBar(final View view) {

        view.findViewById(R.id.btnBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        ImageButton btnLoc = (ImageButton) view.findViewById(R.id.btnSorter);
        btnLoc.setVisibility(View.VISIBLE);
        btnLoc.setImageResource(R.drawable.navigation_locate);
        btnLoc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<ScheduleTask> taskList = adapter.getDataList();
                MyApplication.getInstance().sendToBaseMapHandle(
                        new ShowAllTaskLocCallback((BaseActivity) getActivity(),
                                taskList, TASK_STATE_DONE.equals(filterValueTaskState)));
            }
        });

        this.txtSearch = (EditText) view.findViewById(R.id.txtSearch);
        txtSearch.setVisibility(View.VISIBLE);
        txtSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), CaseSearchActivity.class);
                // key 为搜索框的当前值
                intent.putExtra("key", txtSearch.getText().toString());
                intent.putExtra("searchHint", "搜索条件");
                intent.putExtra("searchHistoryKey", "DeviceCareSearchHistory_" + bizName);
                startActivityForResult(intent, TASK_SEARCH_REQUEST_CODE);
            }
        });

        ImageView ivQRCode = (ImageView) view.findViewById(R.id.iv_qrcode);
        if (TextUtils.isEmpty(mQRCodeScanConfig)) {
            ivQRCode.setVisibility(View.GONE);
        } else {
            ivQRCode.setVisibility(View.VISIBLE);
            ivQRCode.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CaptureActivity.class);
                    startActivityForResult(intent, TASK_QRCODE_SCAN_REQUEST_CODE);
                }
            });
        }
    }

    private void initContentView(View view) {

        this.mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.contentListView);
        this.actualListView = mPullRefreshListView.getRefreshableView();
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        registerForContextMenu(actualListView);

        // 下拉刷新时的提示文本设置
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");

        // 上拉加载更多时的提示文本设置
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");

        this.mOnRefreshListener2 = new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(label);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                // dataList.clear();
                currentPageIndex = 1;
                isLoadMoreMode = false;
                currentCoordinate = GpsReceiver.getInstance().getLastLocalLocation();
                loadData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                isLoadMoreMode = true;
                loadData();
            }
        };
        mPullRefreshListView.setOnRefreshListener(mOnRefreshListener2);

        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
                intent.putExtra("ListItemEntity", (ScheduleTask) adapter.getItem(position - 1));
                intent.putExtra("ComeFrom", TaskDetailActivity.Source.FromList);
                intent.putExtra("IsDone", TASK_STATE_DONE.equals(filterValueTaskState));
                startActivityForResult(intent, TASK_DETAIL_REQUEST_CODE);
            }
        });
    }

    private void loadData() {

        final int loadPageIndex = isLoadMoreMode ? currentPageIndex + 1 : currentPageIndex;

        final String gisNoSearchCriteria = txtSearch.getText().toString().trim();

        MmtBaseTask<String, Void, ResultTableData<ScheduleTask>> mmtBaseTask =
                new MmtBaseTask<String, Void, ResultTableData<ScheduleTask>>(getActivity(), loadByOutside) {
                    @Override
                    protected ResultTableData<ScheduleTask> doInBackground(String... params) {
                        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FetchYHTaskList");

                        // GIS编号搜索
                        String searchCriteria = "";
                        if (!TextUtils.isEmpty(gisNoSearchCriteria)) {
                            searchCriteria = gisNoSearchCriteria;
                        }

                        // 坐标参数
                        String coordinate = "";
                        if (currentCoordinate != null && currentCoordinate.isUsefull()) {
                            coordinate = currentCoordinate.toXY();
                        }

                        // 排序
                        String sortType;
                        switch (filterValueSortType) {
                            case "距离由近到远":
                                sortType = "距离 asc";
                                break;
                            case "时间由近到远":
                                sortType = "开始时间 desc";
                                break;
                            case "时间由远到近":
                                sortType = "开始时间 asc";
                                break;
                            case "上次完成时间由远及近":
                                sortType = "完成时间间距 desc";
                                break;
                            default:
                                sortType = "距离 asc";
                                break;
                        }

                        // 任务状态
                        String taskState = !TextUtils.isEmpty(filterValueTaskState) ? filterValueTaskState : TASK_STATE_DISPATCHED;

                        String jsonResult = NetUtil.executeHttpGet(sb.toString(), "bizName", bizName, "userID", String.valueOf(userId),
                                "pageSize", String.valueOf(pageSize), "pageIndex", String.valueOf(loadPageIndex),
                                "condition", searchCriteria, "position", coordinate, "sort", sortType, "taskType", taskState);

                        if (TextUtils.isEmpty(jsonResult)) {
                            return null;
                        }

                        ResultTableData<ScheduleTask> resultData = new Gson().fromJson(jsonResult, new TypeToken<ResultTableData<ScheduleTask>>() {
                        }.getType());

                        return resultData;
                    }

                    @Override
                    protected void onSuccess(ResultTableData<ScheduleTask> resultData) {
                        mPullRefreshListView.onRefreshComplete();
                        loadByOutside = false;

                        String defErrMsg = "获取任务列表失败";
                        if (resultData == null) {
                            Toast.makeText(getActivity(), defErrMsg, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (resultData.ResultCode != 200) {
                            Toast.makeText(getActivity(),
                                    TextUtils.isEmpty(resultData.ResultMessage) ? defErrMsg : resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (resultData.DataList.size() == 0) {
                            if (!isLoadMoreMode) { // Refresh
                                Toast.makeText(getContext(), "没有记录", Toast.LENGTH_SHORT).show();
                                dataList.clear();
                                adapter.notifyDataSetChanged();
                            } else {               // LoadMore
                                Toast.makeText(getContext(), "没有更多数据", Toast.LENGTH_SHORT).show();
                            }
                        } else {

                            if (isLoadMoreMode) {
                                currentPageIndex++; //加载更多成功更新当前页页码
                            } else {
                                dataList.clear();
                                Toast.makeText(getContext(), "刷新成功", Toast.LENGTH_SHORT).show();
                            }

                            dataList.addAll(resultData.DataList);

                            adapter.notifyDataSetChanged();
                        }
                    }
                };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new ScheduleTaskAdapter(getActivity(), dataList);
        mPullRefreshListView.setAdapter(adapter);
        mPullRefreshListView.setRefreshing(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1 && requestCode == TASK_SEARCH_REQUEST_CODE) {
            String key = data.getStringExtra("key"); //返回空代表查全部
            txtSearch.setText(key);
            updateData();
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TASK_QRCODE_SCAN_REQUEST_CODE) {
                String code = data.getExtras().getString("code");
                resolveQRCode(code);
            }
        }
    }

    public void updateData() {
        if (mPullRefreshListView != null && mOnRefreshListener2 != null) {
            loadByOutside = true;
            mOnRefreshListener2.onPullDownToRefresh(mPullRefreshListView);
            actualListView.setSelection(0);
        }
    }

    private void resolveQRCode(final String code) {

        if (mQRCodeResolver == null) {
            switch (mQRCodeScanConfig) {
                case "ZS":
                    ZSQRCodeResolver.QRCodeResolverParams params
                            = new ZSQRCodeResolver.QRCodeResolverParams(this, bizName, userId, TASK_STATE_DISPATCHED);
                    mQRCodeResolver = new ZSQRCodeResolver(params);
                    break;
                default:
                    Toast.makeText(getContext(), "解析器配置错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        if (mQRCodeResolver != null) {
            mQRCodeResolver.resolve(code);
        }
    }

}
