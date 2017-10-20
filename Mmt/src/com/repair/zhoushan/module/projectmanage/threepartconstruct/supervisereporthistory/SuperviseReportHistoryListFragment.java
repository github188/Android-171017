package com.repair.zhoushan.module.projectmanage.threepartconstruct.supervisereporthistory;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.CaseItem;

import java.util.ArrayList;

public class SuperviseReportHistoryListFragment extends Fragment {

    private PullToRefreshListView mPullRefreshListView;
    private ListView actualListView;

    private SuperviseReportHistoryAdapter adapter;

    private ArrayList<ConstructSupervision> data = new ArrayList<ConstructSupervision>();

    // For paging loading
    private int currentPageIndex = 1; // Start from 1
    private final int pageSize = 20;
    private boolean isLoadMoreMode = false;
    //当前页面第一个可见item，用于返回列表界面时恢复列表的滑动状态
    public int firstVisivleItemPos = 0;

    private CaseItem caseItemEntity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.caseItemEntity = getArguments().getParcelable("CaseItemEntity");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.supervise_report_list, null);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.superviseReportListView);
        actualListView = mPullRefreshListView.getRefreshableView();

        mPullRefreshListView.setEmptyView(inflater.inflate(com.mapgis.mmt.R.layout.empty_default, null));

        adapter = new SuperviseReportHistoryAdapter(data, (BaseActivity) getActivity());
        registerForContextMenu(actualListView);
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 每项的点击事件
        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                adapter.onDetailClick(arg2 - 1);
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

        int loadPageIndex = isLoadMoreMode ? currentPageIndex + 1 : currentPageIndex;

        new MmtBaseTask<String, Void, String>(getActivity(), false, new MmtBaseTask.OnWxyhTaskListener<String>() {
            @Override
            public void doAfter(String result) {

                mPullRefreshListView.onRefreshComplete();

                // 禁分页功能
                if (isLoadMoreMode) {
                    Toast.makeText(getActivity(), "没有更多数据！", Toast.LENGTH_SHORT).show();
                    return;
                }

                Results<ConstructSupervision> rawData = new Gson().fromJson(result, new TypeToken<Results<ConstructSupervision>>() {
                }.getType());

                String errorMsg = "获取列表失败";
                if (rawData == null) {
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                    return;
                }

                ResultData<ConstructSupervision> mData = rawData.toResultData();
                if (mData.ResultCode != 200) {
                    if (!BaseClassUtil.isNullOrEmptyString(mData.ResultMessage))
                        Toast.makeText(getActivity(), mData.ResultMessage, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<ConstructSupervision> newData = mData.DataList;
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

                    // 查询数据成功刷新列表
                    adapter.refreash();
                }
            }
        }) {
            @Override
            protected String doInBackground(String... params) {

                try {
                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetSupervisionDataByEventCode";
                    return NetUtil.executeHttpGet(url, "eventCode", params[0]);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }.mmtExecute(caseItemEntity.EventCode);

    }

}
