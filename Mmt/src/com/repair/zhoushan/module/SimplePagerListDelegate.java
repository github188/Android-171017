package com.repair.zhoushan.module;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;

import java.util.List;

public abstract class SimplePagerListDelegate<T> {

    private List<T> dataList;

    private Class<T> dataItemClass;
    private BaseActivity mBaseActivity;

    private boolean loadByOutside = false;


    public SimplePagerListDelegate(BaseActivity baseActivity, List<T> dataList, Class<T> dataItemClass) {

        this.mBaseActivity = baseActivity;
        this.dataList = dataList;
        this.dataItemClass = dataItemClass;
    }

    // 界面标题
    private String titleName;
    private String userIdStr;

    private PullToRefreshListView mPullRefreshListView;
    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener2;
    protected ListView actualListView;
    protected SimpleBaseAdapter adapter;

    // 当前页面第一个可见item，用于返回列表界面时恢复列表的滑动状态
    public int firstVisivleItemPos = 0;

    // 用于分页用的几个变量
    private int currentPageIndex = 1; // Start from 1
    private int pageSize = 10;
    private boolean isLoadMoreMode = false;
    private int loadPageIndex;

    public PullToRefreshListView getmPullRefreshListView() {
        return mPullRefreshListView;
    }

    public PullToRefreshBase.OnRefreshListener2<ListView> getmOnRefreshListener2() {
        return mOnRefreshListener2;

    }

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public void setCurrentPageIndex(int currentPageIndex) {
        this.currentPageIndex = currentPageIndex;
    }

    public boolean getIsLoadMoreMode() {
        return isLoadMoreMode;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getUserIdStr() {
        return userIdStr;
    }

    public int getLoadPageIndex() {
        return loadPageIndex;
    }

    void setDefaultContentView(OnViewCreatedListener onViewCreatedListener) {

        this.userIdStr = String.valueOf(MyApplication.getInstance().getUserId());
        this.titleName = mBaseActivity.getIntent().getStringExtra("Title");
        this.mOnViewCreatedListener = onViewCreatedListener;

        initContentView();

        if (mOnViewCreatedListener != null) {
            mOnViewCreatedListener.onViewCreated();
        }

        afterViewCreated();
    }

    protected void initRootView() {
        mBaseActivity.setContentView(R.layout.eventreport_history_list);

    }

    protected void initContentView() {

        initRootView();

        mBaseActivity.setSwipeBackEnable(false);

        TextView baseTv = mBaseActivity.getBaseTextView();
        if (baseTv != null) {
            baseTv.setText(titleName);
        }
        ImageButton baseLeftIv = mBaseActivity.getBaseLeftImageView();

        if (baseLeftIv != null) {
            baseLeftIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBaseActivity.onBackPressed();
                }
            });
        }

        this.mPullRefreshListView = (PullToRefreshListView) mBaseActivity.findViewById(R.id.mainFormList);
        this.actualListView = mPullRefreshListView.getRefreshableView();

        // mPullRefreshListView.setEmptyView(LayoutInflater.from(this).inflate(R.layout.empty_default, null));

        mBaseActivity.registerForContextMenu(actualListView);

        mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");
        // 上拉加载更多时的提示文本设置
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");

        this.adapter = generateAdapter();
        actualListView.setAdapter(adapter);

        addViewListener();
    }

    void afterViewCreated() {
        mPullRefreshListView.setRefreshing(false);
    }

    protected abstract SimpleBaseAdapter generateAdapter();

    protected abstract String generateUrl();

    void addViewListener() {

        // 每项的点击事件
        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                adapter.onItemClick(arg2 - 1);
            }
        });

        this.mOnRefreshListener2 = new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 更新下拉面板
                String label = DateUtils.formatDateTime(mBaseActivity, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(label);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                isLoadMoreMode = false;
                loadPageIndex = currentPageIndex = 1;
                loadData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 更新上拉面板
                String label = DateUtils.formatDateTime(mBaseActivity, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                isLoadMoreMode = true;
                loadPageIndex = currentPageIndex + 1;
                loadData();
            }
        };

        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(mOnRefreshListener2);

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

    }

    protected void loadData() {

        // 执行更新任务,结束后刷新界面
        MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(mBaseActivity, loadByOutside) {
            @Override
            protected String doInBackground(String... params) {
                return NetUtil.executeHttpGet(generateUrl());
            }

            @Override
            protected void onSuccess(String jsonResult) {

                mPullRefreshListView.onRefreshComplete();
                loadByOutside = false;

                final String defErrMsg = "获取数据失败";
                if (TextUtils.isEmpty(jsonResult)) {
                    Toast.makeText(mBaseActivity, defErrMsg, Toast.LENGTH_SHORT).show();
                    return;
                }

                ResultData<T> newData;
                if (jsonResult.contains("getMe")) { // Results
                    newData = Utils.json2ResultDataToast(dataItemClass, mBaseActivity, jsonResult, defErrMsg, true);
                } else { // ResultData
                    newData = Utils.resultDataJson2ResultDataToast(dataItemClass, mBaseActivity, jsonResult, defErrMsg, true);
                }

                if (newData == null) return;

                adapterAddData(newData);
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    protected void adapterAddData(ResultData<T> newData) {
        if (newData.DataList.size() == 0) {
            if (!isLoadMoreMode) { // Refresh
                Toast.makeText(mBaseActivity, "没有记录", Toast.LENGTH_SHORT).show();
                dataList.clear();
                adapter.notifyDataSetChanged();
            } else {               // LoadMore
                Toast.makeText(mBaseActivity, "没有更多数据", Toast.LENGTH_SHORT).show();
            }
        } else {

            if (isLoadMoreMode) {
                currentPageIndex++; //加载更多成功更新当前页页码
            } else {
                Toast.makeText(mBaseActivity, "刷新成功", Toast.LENGTH_SHORT).show();
                dataList.clear();
            }
            dataList.addAll(newData.DataList);

            adapter.notifyDataSetChanged();
        }

    }

    public void updateView() {
        try {
            if (actualListView != null) {
                if (dataList != null && dataList.size() > 0)
                    dataList.remove(adapter.getLastClickPos());

                adapter.notifyDataSetChanged();
                actualListView.setSelection(firstVisivleItemPos);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateData() {
        if (mPullRefreshListView != null && mOnRefreshListener2 != null) {
            loadByOutside = true;
            mOnRefreshListener2.onPullDownToRefresh(mPullRefreshListView);
            actualListView.setSelection(0);
        }
    }

    private OnViewCreatedListener mOnViewCreatedListener;

    interface OnViewCreatedListener {
        void onViewCreated();
    }

}
