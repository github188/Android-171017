package com.repair.zhoushan.module.casemanage.mydonecase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.repair.common.CaseSearchActivity;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.entity.CaseItem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;

public class DoneCaseListFragment extends Fragment {

    protected final ArrayList<CaseItem> data = new ArrayList<>();

    public MultiSwitchButton switchButton;
    public int sortType = 0;
    protected PullToRefreshListView mPullRefreshListView;
    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener2;
    public DoneCaseListAdapter adapter;

    private EditText txtSearch; // ActionBar搜索框

    private boolean isUseAutoFresh = false;

    //按工单距离和日期 默认升序
    protected boolean isTimeAsc = false;
    protected boolean isDisAsc = true;
    protected ListView actualListView;

    //当前页面第一个可见item，用于返回列表界面时恢复列表的滑动状态
    public int firstVisivleItemPos = 0;

    // For paging loading
    private int currentPageIndex = 1; // Start from 1
    private final int pageSize = 20;
    private boolean isLoadMoreMode = false;

    private String defaultFlowNames;

    private final RefreahThread thread = new RefreahThread();

    public void setIsUseAutoFresh(boolean isUseAutoFresh) {
        this.isUseAutoFresh = isUseAutoFresh;
    }

    public static DoneCaseListFragment newInstance(@Nullable String defaultFlowNames) {
        DoneCaseListFragment fragment = new DoneCaseListFragment();
        Bundle args = new Bundle();
        args.putString("DefaultFlowNames", defaultFlowNames);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey("DefaultFlowNames")) {
            this.defaultFlowNames = args.getString("DefaultFlowNames");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.case_list_activity, container, false);

        initActionBar(view);

        switchButton = (MultiSwitchButton) view.findViewById(R.id.maintenanceListTitle);
        switchButton.setContent(new String[]{"按承办日期 ↓", "按工单距离"});

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.maintenanceFormList);
        actualListView = mPullRefreshListView.getRefreshableView();
        registerForContextMenu(actualListView);

        adapter = new DoneCaseListAdapter(data, (BaseActivity) getActivity(), "撤回");
        actualListView.setAdapter(adapter);

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
                intent.putExtra("searchHint", "事件编号、工单编号、上报人、摘要");
                intent.putExtra("searchHistoryKey", "DoneCaseListSearchHistory_");

                DoneCaseListFragment.this.startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // 滚动选择条件
        switchButton.setOnScrollListener(new MultiSwitchButton.OnScrollListener() {
            @Override
            public void OnScrollComplete(int index) {
                sortType = index;
                handler.sendEmptyMessage(MaintenanceConstant.CASE_LIST_REFREASH);
            }
        });

        // 每项的点击事件
        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                adapter.onDetailClick(arg2 - 1);
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

                data.clear();
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
        };

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
        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(mOnRefreshListener2);

        mPullRefreshListView.setRefreshing(false);
    }

    private boolean showLoading = false;

    private void loadData() {

        int loadPageIndex = isLoadMoreMode ? currentPageIndex + 1 : currentPageIndex;

        final String searchKey = txtSearch.getText().toString().trim();

        // 执行更新任务,结束后刷新界面
        MmtBaseTask<String, Void, ResultData<CaseItem>> mmtBaseTask = new MmtBaseTask<String, Void, ResultData<CaseItem>>(getActivity(), showLoading) {
            @Override
            protected ResultData<CaseItem> doInBackground(String... params) {

                ResultData<CaseItem> resultData;

                StringBuilder sb = new StringBuilder();
                sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                        .append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/")
                        .append(String.valueOf(userID))
                        .append("/StardEventDoneBox?_mid=").append(params[0])
                        .append("&pageIndex=").append(params[1])
                        .append("&pageSize=").append(params[2])
                        .append("&eventInfo=").append(Uri.encode(params[3]))
                        .append("&sortFields=办结时间&direction=desc");

                if (!TextUtils.isEmpty(defaultFlowNames)) {
                    sb.append("&flowName=").append(Uri.encode(defaultFlowNames));
                }

                try {
                    String jsonResult = NetUtil.executeHttpGet(sb.toString());

                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取数据失败：网络错误");
                    }

                    Results<CaseItem> results = new Gson().fromJson(jsonResult, new TypeToken<Results<CaseItem>>() {
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
            protected void onSuccess(ResultData<CaseItem> resultData) {

                showLoading = false;

                if (resultData.ResultCode != 200) {
                    Toast.makeText(getActivity(), resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    mPullRefreshListView.onRefreshComplete();
                    return;
                }

                Message msg = handler.obtainMessage();
                msg.obj = resultData.DataList == null ? new ArrayList<CaseItem>() : resultData.DataList;
                msg.what = MaintenanceConstant.SERVER_GET_LIST_SUCCESS;
                handler.sendMessage(msg);
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute(UUID.randomUUID().toString(), String.valueOf(loadPageIndex), String.valueOf(pageSize), searchKey);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thread.isRun = false;
    }

    //将移交、结案的记录从list从移除
    public void updateView() {
        if (actualListView != null) {
            data.remove(adapter.curClickPos);
            adapter.notifyDataSetChanged();
            actualListView.setSelection(firstVisivleItemPos);
        }
    }

    protected Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            mPullRefreshListView.onRefreshComplete();

            switch (msg.what) {
                case MaintenanceConstant.SERVER_GET_LIST_SUCCESS:

                    ArrayList<CaseItem> newData = (ArrayList<CaseItem>) msg.obj;

                    if (newData.size() == 0) {
                        if (!isLoadMoreMode) { // Refresh
                            Toast.makeText(getActivity(), "没有记录!", Toast.LENGTH_SHORT).show();
                        } else { // LoadMore
                            Toast.makeText(getActivity(), "没有更多数据！", Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        if (isLoadMoreMode) {
                            currentPageIndex++; //加载更多成功更新当前页页码
                        } else {
                            Toast.makeText(getActivity(), "刷新成功！", Toast.LENGTH_SHORT).show();
                        }
                        data.addAll(newData);

                        handler.sendEmptyMessage(MaintenanceConstant.CASE_LIST_AutoREFREASH);
                    }

                    if (isUseAutoFresh && !thread.isAlive()) {
                        thread.start();
                    }
                    break;
                case MaintenanceConstant.SERVER_GET_LIST_FAIL:
                    break;
                case MaintenanceConstant.CASE_LIST_REFREASH:
                    switch (sortType) {
                        case 0:// 按承办日期
                            if (isTimeAsc) {
                                switchButton.setContent(new String[]{"按承办日期 ↑", "按工单距离"});
                            } else {
                                switchButton.setContent(new String[]{"按承办日期 ↓", "按工单距离"});
                            }
                            break;
                        case 1:// 按工单距离
                            if (isDisAsc) {
                                switchButton.setContent(new String[]{"按承办日期", "按工单距离 ↑"});
                            } else {
                                switchButton.setContent(new String[]{"按承办日期", "按工单距离 ↓",});
                            }
                            break;
                    }
                    sortByCriteria(1);
                    break;

                case MaintenanceConstant.CASE_LIST_AutoREFREASH:
                    sortByCriteria(0);
                    break;
            }
        }
    };

    /**
     * 获取并刷新数据
     */
    public void updateData() {
        mPullRefreshListView.setRefreshing(false);
    }

    class RefreahThread extends Thread {
        public boolean isRun = true;

        @Override
        public void run() {

            Thread.currentThread().setName(this.getClass().getName());

            while (isRun) {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.sendEmptyMessage(MaintenanceConstant.CASE_LIST_AutoREFREASH);
            }

        }
    }

    /**
     * @param refreshType 0: AutoRefresh (不需要更改正序倒序标志) , 1: Refresh
     */
    private void sortByCriteria(final int refreshType) {

        if (data.size() == 0) {
            return;
        }

        final GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

        if (!getActivity().isFinishing()) {
            new MmtBaseTask<Void, Void, Void>(getActivity()) {
                @Override
                protected Void doInBackground(Void... params) {

                    // 依据当前位置刷新所有记录的距离值
                    if (xyz.isUsefull()) {
                        for (CaseItem caseItem : data) {
                            putDistance(caseItem, xyz);
                        }
                    } else {
                        for (CaseItem caseItem : data) {
                            caseItem.Distance = 0;
                            caseItem.DistanceStr = "未能获取当前坐标";
                        }
                    }

                    switch (sortType) {
                        case 0: // 按承办日期
                            if (isTimeAsc) {
                                Collections.sort(data, Collections.reverseOrder(undertakeTimeComparator));
                            } else {
                                Collections.sort(data, undertakeTimeComparator);
                            }

                            if (refreshType == 1) {
                                isTimeAsc = !isTimeAsc;
                            }
                            break;

                        case 1: // 按工单距离
                            if (isDisAsc) {
                                Collections.sort(data, distanceComparator);
                            } else {
                                Collections.sort(data, Collections.reverseOrder(distanceComparator));
                            }

                            if (refreshType == 1) {
                                isDisAsc = !isDisAsc;
                            }
                            break;
                    }

                    return null;
                }

                @Override
                protected void onSuccess(Void aVoid) {
                    adapter.refresh(sortType);
                }
            }.mmtExecute();
        }
    }

    private final DecimalFormat decimalFormat = new DecimalFormat(".0");

    /**
     * 计算当前坐标与目标位置的距离，并设置当当前工单信息中
     */
    private void putDistance(CaseItem caseItem, GpsXYZ xyz) {
        caseItem.Distance = 0;
        caseItem.DistanceStr = "未含有坐标信息";

        try {
            if (!TextUtils.isEmpty(caseItem.XY)) {

                double x = Double.valueOf(caseItem.XY.split(",")[0]);
                double y = Double.valueOf(caseItem.XY.split(",")[1]);
                double gpsX = xyz.getX();
                double gpsY = xyz.getY();

                double distance = Math.sqrt((gpsX - x) * (gpsX - x) + (gpsY - y) * (gpsY - y));

                caseItem.Distance = distance;

                if (distance >= 1000) {
                    caseItem.DistanceStr = (decimalFormat.format(distance / 1000)) + "千米";
                } else {
                    caseItem.DistanceStr = ((int) distance) + "米";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 距离升序
     */
    private Comparator<CaseItem> distanceComparator = new Comparator<CaseItem>() {
        @Override
        public int compare(CaseItem lhs, CaseItem rhs) {
            return lhs.Distance > rhs.Distance ? 1 : (lhs.Distance < rhs.Distance ? -1 : 0);
        }
    };

    /**
     * 时间降序
     */
    private Comparator<CaseItem> undertakeTimeComparator = new Comparator<CaseItem>() {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();

        @Override
        public int compare(CaseItem lhs, CaseItem rhs) {

            try {
                calendar.setTime(format.parse(lhs.UnderTakeTime));
                long lUndertackTime = calendar.getTimeInMillis();
                calendar.setTime(format.parse(rhs.UnderTakeTime));
                long rUndertackTime = calendar.getTimeInMillis();

                // 与默认的升序相反，此处为降序
                return lUndertackTime > rUndertackTime ? -1 : (lUndertackTime < rUndertackTime ? 1 : 0);

            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    };

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
}
