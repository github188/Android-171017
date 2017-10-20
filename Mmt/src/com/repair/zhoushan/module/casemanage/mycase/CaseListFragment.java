package com.repair.zhoushan.module.casemanage.mycase;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.filtermenubar.FilterMenuBar;
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
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CaseListFragment extends Fragment implements CaseListAdapter.OnListItemClickListener {

    public static final String TAG = "CaseListFragment";

    //region Data
    private static final String EMPTY_STRING = "";
    private final ArrayList<CaseItem> data = new ArrayList<>();

    private final String SORT_MODE = "列表排序"; // 排序方式
    private final String SCAN_STATUS = "阅览状态"; // 是否查看
    private final String FLOW_NAME = "流程名称"; // 按流程过滤

    private String filterSortMode = "时间由近到远";
    private String filterScanStatus = EMPTY_STRING;
    private String filterFlowName = EMPTY_STRING;

    private boolean isTimeAsc = false; //按工单距离和日期 默认升序
    private boolean isDisAsc = true;
    private int sortType = 0;

    // For paging loading
    private int currentPageIndex = 1; // Start from 1
    private final int pageSize = 20;
    private boolean isLoadMoreMode = false;

    private int userID;
    private String defaultFlowNames;

    //endregion

    //region View
    private EditText txtSearch; // ActionBar搜索框
    private FilterMenuBar mFilterMenuBar;
    private XRecyclerView mRecyclerView;
    private CaseListAdapter adapter;
    //endregion

    public static CaseListFragment newInstance(@NonNull String defaultFlowNames, boolean showEventCode) {
        CaseListFragment fragment = new CaseListFragment();
        Bundle args = new Bundle();
        args.putString("DefaultFlowNames", defaultFlowNames);
        args.putBoolean("ShowEventCode", showEventCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.userID = MyApplication.getInstance().getUserId();

        Bundle args = getArguments();
        if (args != null && args.containsKey("DefaultFlowNames")) {
            this.defaultFlowNames = args.getString("DefaultFlowNames");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_case_doingbox, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initListener();

        requestRefreshData();
    }

    private void initView(final View view) {

        // action bar
        initActionBar(view);

        // filter bar
        this.mFilterMenuBar = (FilterMenuBar) view.findViewById(R.id.mMenuBar);
        mFilterMenuBar.setMenuItems(new String[]{SORT_MODE, SCAN_STATUS},
                new String[][]{{"时间由近到远", "距离由近到远"}, {"已读", "未读"}},
                new String[]{filterSortMode, filterScanStatus});

        if (!TextUtils.isEmpty(defaultFlowNames)) {
            String[] flowNames = defaultFlowNames.split(",");
            mFilterMenuBar.addMenuItem(FLOW_NAME, flowNames, "");
        } else {
            getFilterFlowNameList();
        }

        // recycler view
        this.mRecyclerView = (XRecyclerView) view.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        // mRecyclerView.setArrowImageView(R.drawable.iconfont_downgrey);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator(){
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
                return true;
            }
        });

        boolean showEventCode = false;
        Bundle args = getArguments();
        if (args != null && args.containsKey("ShowEventCode")) {
            showEventCode = args.getBoolean("ShowEventCode", false);
        }
        this.adapter = new CaseListAdapter(getActivity(), data, showEventCode);
        adapter.setOnListItemClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    private void initListener() {

        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                isLoadMoreMode = false;
                loadData();
            }

            @Override
            public void onLoadMore() {
                isLoadMoreMode = true;
                loadData();
            }
        });

        // 过滤条件
        mFilterMenuBar.setOnMenuItemSelectedListener(new FilterMenuBar.OnMenuItemSelectedListener() {
            @Override
            public void onItemSelected(Map<String, String> selectResult) {
                String tempStr;

                // 本地排序，不需要请求服务，单独处理
                tempStr = selectResult.get(SORT_MODE);
                tempStr = TextUtils.isEmpty(tempStr) ? EMPTY_STRING : tempStr;
                if (!filterSortMode.equals(tempStr)) {
                    if (TextUtils.isEmpty(tempStr)) {
                        sortType = 0;
                        isTimeAsc = false;
                    } else if ("距离由近到远".equals(tempStr)) {
                        sortType = 1;
                        isDisAsc = true;
                    } else if ("时间由近到远".equals(tempStr)) {
                        sortType = 0;
                        isTimeAsc = false;
                    }
                    filterSortMode = tempStr;

                    List<CaseItem> tempData = new ArrayList<>(data);
                    refreshListView(tempData);
                    return;
                }

                // 查阅状态需要请求服务
                tempStr = selectResult.get(SCAN_STATUS);
                filterScanStatus = TextUtils.isEmpty(tempStr) ? EMPTY_STRING : tempStr;

                // 查阅状态需要请求服务
                tempStr = selectResult.get(FLOW_NAME);
                filterFlowName = TextUtils.isEmpty(tempStr) ? EMPTY_STRING : tempStr;

                requestRefreshData();
            }
        });
    }

    //region Properties

    protected int getPageSize() {
        return pageSize;
    }

    protected int getLoadingPageIndex() {
        return isLoadMoreMode ? currentPageIndex + 1 : 1;
    }

    protected String getSearchKey() {
        return txtSearch.getText().toString().trim();
    }

    protected String getFilterFlowName() {
        if (!TextUtils.isEmpty(filterFlowName)) {
            return filterFlowName;
        } else if (!TextUtils.isEmpty(defaultFlowNames)) {
            return defaultFlowNames;
        } else {
            return EMPTY_STRING;
        }
    }

    protected String isRead() {
        String isRead = "";
        if ("已读".equals(filterScanStatus)) {
            isRead = "1";
        } else if ("未读".equals(filterScanStatus)) {
            isRead = "0";
        }
        return isRead;
    }

    protected int getUserID() {
        return userID;
    }

    //endregion

    private void loadData() {

        BaseTaskResults<String, Void, CaseItem> baseTask = new BaseTaskResults<String, Void, CaseItem>(getActivity()) {
            @NonNull
            @Override
            protected String getRequestUrl() {
                return generateRequestUrl();
            }

            @Override
            protected void onSuccess(Results<CaseItem> results) {
                if (isLoadMoreMode) {
                    mRecyclerView.loadMoreComplete();
                } else {
                    mRecyclerView.refreshComplete();
                }

                final ResultData<CaseItem> newData = results.toResultData();

                // Load failed
                if (newData.ResultCode != 200) {
                    Toast.makeText(getActivity(), newData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Load successful
                ArrayList<CaseItem> freshData = new ArrayList<>();

                if (isLoadMoreMode) { // LoadMore
                    freshData.addAll(data);

                    if (newData.DataList.size() > 0) {
                        currentPageIndex++;
                        freshData.addAll(newData.DataList);
                    }

                } else { // Refresh
                    currentPageIndex = 1;
                    freshData.addAll(newData.DataList);
                }

                if (currentPageIndex * pageSize >= results.totalRcdNum) {
                    mRecyclerView.setNoMore(true);
                }

                refreshListView(freshData);
            }
        };
        baseTask.setCancellable(false);
        baseTask.mmtExecute();
    }

    protected String generateRequestUrl() {

        StringBuilder sb = new StringBuilder();
        sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                .append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/")
                .append(String.valueOf(getUserID()))
                .append("/StardEventDoingBox?_mid=").append(UUID.randomUUID().toString())
                .append("&pageIndex=").append(String.valueOf(getLoadingPageIndex()))
                .append("&pageSize=").append(String.valueOf(getPageSize()))
                .append("&sortFields=ID0&direction=desc");

        String searchKey = getSearchKey();
        if (!TextUtils.isEmpty(searchKey)) {
            sb.append("&eventInfo=").append(Uri.encode(searchKey));
        }

        String flowName = getFilterFlowName();
        if (!TextUtils.isEmpty(flowName)) {
            sb.append("&flowName=").append(Uri.encode(flowName));
        }

        String isReadStr = isRead();
        if (!TextUtils.isEmpty(isReadStr)) {
            sb.append("&isReaded=").append(isReadStr);
        }

        return sb.toString();
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
                intent.putExtra("searchHistoryKey", "CaseListSearchHistory_DoingBox");

                startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });
    }

    public void getFilterFlowNameList() {

        MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(getActivity(), false) {

            @Override
            protected String doInBackground(String... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/" + userID + "/GetDoingBoxFlowNameList";

                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String jsonResult) {

                ResultData<String> resultFlowName = Utils.json2ResultDataToast(String.class, getActivity(),
                        jsonResult, "获取流程名称列表失败", true);
                if (resultFlowName != null) {
                    mFilterMenuBar.addMenuItem(FLOW_NAME, resultFlowName.DataList.toArray(new String[resultFlowName.DataList.size()]), "");
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();

    }

    private void refreshListView(final List<CaseItem> freshData) {

        if (freshData == null || freshData.isEmpty()) {
            data.clear();
            adapter.refresh(sortType);
            return;
        }

        final GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();
        
        if (!xyz.isUsefull()) {
            Toast.makeText(getActivity(), "无法定位，请检查设置", Toast.LENGTH_SHORT).show();
        }

        if (!getActivity().isFinishing()) {
            new MmtBaseTask<Void, Void, Void>(getActivity()) {
                @Override
                protected Void doInBackground(Void... params) {

                    // 依据当前位置刷新所有记录的距离值
                    if (xyz.isUsefull()) {
                        for (CaseItem caseItem : freshData) {
                            putDistance(caseItem, xyz);
                        }
                    } else {
                        for (CaseItem caseItem : freshData) {
                            caseItem.Distance = 0;
                            caseItem.DistanceStr = ""; // "未能获取当前坐标"
                        }
                    }

                    switch (sortType) {
                        case 0: // 按承办日期
                            if (isTimeAsc) {
                                Collections.sort(freshData, Collections.reverseOrder(undertakeTimeComparator));
                            } else {
                                Collections.sort(freshData, undertakeTimeComparator);
                            }

                            break;
                        case 1: // 按工单距离
                            if (isDisAsc) {
                                Collections.sort(freshData, distanceComparator);
                            } else {
                                Collections.sort(freshData, Collections.reverseOrder(distanceComparator));
                            }

                            break;
                    }

                    return null;
                }

                @Override
                protected void onSuccess(Void aVoid) {
                    data.clear();
                    data.addAll(freshData);
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
        caseItem.DistanceStr = ""; // 未含有坐标信息

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

    public void requestRefreshData() {
        mRecyclerView.setRefreshing(true);
    }

    // 本地更新
    public void refreshItemView(boolean isRead, boolean isFeedback) {

        int position = adapter.getCurrentClickPosition();
        CaseItem caseItem = adapter.getItem(position);
        if (isRead) {
            caseItem.ReadCaseTime = BaseClassUtil.getSystemTime();
        }
        if (isFeedback) {
            caseItem.IsFeedback = 1;
        }
        adapter.notifyDataSetChanged();

        // TODO: 11/8/16
        // adapter.notifyItemChanged(position, new Object());
    }

    protected void removeLastClickedItem() {
        int position = adapter.getCurrentClickPosition();
        adapter.removeItem(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1 && requestCode == Constants.SEARCH_REQUEST_CODE) {
            // 搜索界面返回的搜索关键字
            String key = data.getStringExtra("key"); //返回空代表查全部
            txtSearch.setText(key);

            requestRefreshData();

        } else if (requestCode == Constants.DEFAULT_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                // 工单第一次被阅读，返回时在本地更新阅览时间
                refreshItemView(true, false);
            } else if (resultCode == 100) {
                // 多次反馈反馈成功，返回列表界面后本地更新数据的反馈标志字段
                refreshItemView(false, true);
            } else if (resultCode == 200) {
                // 置反馈标志并更新阅览时间
                refreshItemView(true, true);
            }

        }
    }

    @Override
    public void onItemClicked(CaseItem caseItem) {
        Intent intent = new Intent(getActivity(), CaseDetailActivity.class);
        intent.putExtra("ListItemEntity", caseItem);
        startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
        MyApplication.getInstance().startActivityAnimation(getActivity());
    }
}
