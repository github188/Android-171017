package com.mapgis.mmt.module.systemsetting.backgruoundinfo.items;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.filtermenubar.FilterMenuBar;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.NetLogInfo;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.common.CaseSearchActivity;
import com.repair.zhoushan.common.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * zhouxixiang
 */

public class NetRequestListActivity extends BaseActivity {

    private ArrayList<NetLogInfo> mNetLogInfos = new ArrayList<>();
    private String[] requestInterfaces;//数据通过异步接口回调

    private EditText txtSearch; // ActionBar搜索框

    private FilterMenuBar mFilterMenuBar;

    //4个filterMenu项目
    private String filterTime = "";
    private String filterType = "";
    private String filterStatus = "";
    private String filterInterface = "";

    //4个filterMenu标题
    private final String FILTER_UNDERTAKE_TIME = "列表排序";
    private final String FILTER_TYPE = "请求类型";
    private final String FILTER_STATUS = "请求状态";
    private final String FILTER_INTERFACE = "请求接口";

    private PullToRefreshListView refreshListView;
    private ListView actuallyListView;
    private NetRequestAdapter adapter;


    private int currentIndex = 1;//从1开始
    private boolean isLoadMoreMode = false;//加载数据状态
    private final int pageCount = 20;//每页显示20行
    private int totalPageCount = 0;//总页数

    public int firstVisibleItemPos = 0;
    private StringBuilder querySql;//查询语句
    private int loadPageIndex;//每次查询加载

    private PullToRefreshBase.OnRefreshListener2<ListView> onRefreshListener2;

    private static final int MESSAGE_TOTALCOUNT = 1;//总页数显示
    private static final int MESSAGE_LOADCOMPLETED = 2;//当前页码显示
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_TOTALCOUNT:
                    loadCompleted();
                    break;
                case MESSAGE_LOADCOMPLETED:
                    modifyPagefooter();
                    break;
            }
        }
    };

    /**
     * 修改pulltoRefreshListview 的下部标签
     */
    private void modifyPagefooter() {

        try {
            String labelfooter = "第" + currentIndex + "页,共" + totalPageCount + "页";
            refreshListView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(labelfooter);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void setDefaultContentView() {

        try {
            setSwipeBackEnable(false);
            setContentView(R.layout.netrequest_list_activity);

            initView();
            initInterfaceData();
            initListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        initActionBar();//标题栏

        this.mFilterMenuBar = (FilterMenuBar) findViewById(R.id.mMenuBar);
        mFilterMenuBar.setFocusable(true);
        mFilterMenuBar.setFocusableInTouchMode(true);

        refreshListView = (PullToRefreshListView) findViewById(R.id.listViewNet);
        actuallyListView = refreshListView.getRefreshableView();
        refreshListView.setMode(PullToRefreshBase.Mode.BOTH);

        refreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        refreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        refreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");
        // 上拉加载更多时的提示文本设置
        refreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多");
        refreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        refreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");

        adapter = new NetRequestAdapter(this, mNetLogInfos);
        actuallyListView.setAdapter(adapter);

    }

    private void initInterfaceData() {
        new MmtBaseTask<String, Void, List<NetLogInfo>>(this) {
            @Override
            protected List<NetLogInfo> doInBackground(String... params) {

                return DatabaseHelper.getInstance().queryBySql(NetLogInfo.class, params[0]);

            }

            @Override
            protected void onSuccess(List<NetLogInfo> netLogInfos) {
                requestInterfaces = new String[netLogInfos.size()];
                for (int i = 0; i < netLogInfos.size(); i++) {
                    if (netLogInfos.get(i).requestInterface == "") {
                        requestInterfaces[i] = "空";
                    } else {
                        requestInterfaces[i] = netLogInfos.get(i).requestInterface;
                    }
                }

                mFilterMenuBar.setMenuItems(new String[]{FILTER_UNDERTAKE_TIME, FILTER_TYPE, FILTER_STATUS, FILTER_INTERFACE},
                        new String[][]{{"时间降序", "时间升序", "请求耗时", "发送流量", "接收流量"}, {"POST", "GET"}, {"成功", "失败"}, requestInterfaces},
                        new String[]{filterTime, filterType, filterStatus, filterInterface});
            }

        }.mmtExecute("select * from NetLogInfo group by requestInterface");
    }

    private void initListener() {

        mFilterMenuBar.setOnMenuItemSelectedListener(new FilterMenuBar.OnMenuItemSelectedListener() {
            @Override
            public void onItemSelected(Map<String, String> selectResult) {
                filterTime = selectResult.get(FILTER_UNDERTAKE_TIME);
                filterType = selectResult.get(FILTER_TYPE);
                filterStatus = selectResult.get(FILTER_STATUS);
                filterInterface = selectResult.get(FILTER_INTERFACE);
                refreshData();
            }
        });

        refreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.showDetail(position - 1);
            }
        });

        //上下拉刷新数据的监听
        onRefreshListener2 = new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //下拉刷新
                String label = DateUtils.formatDateTime(NetRequestListActivity.this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(label);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                mNetLogInfos.clear();
                currentIndex = 1;
                isLoadMoreMode = false;

                loadData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //上拉加载
                String label = DateUtils.formatDateTime(NetRequestListActivity.this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                isLoadMoreMode = true;
                loadData();
            }
        };
        refreshListView.setOnRefreshListener(onRefreshListener2);
        //滚动监听
        refreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    firstVisibleItemPos = actuallyListView.getFirstVisiblePosition();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        refreshListView.setRefreshing(false);
//        refreshData();

    }

    private void refreshData() {
        //下拉即可，数据刷新工作于监听器中
        if (refreshListView != null && onRefreshListener2 != null) {
            onRefreshListener2.onPullDownToRefresh(refreshListView);
            actuallyListView.setSelection(0);
        }
    }


    private void initActionBar() {

        findViewById(R.id.btnOtherAction).setVisibility(View.GONE);

        addBackBtnListener(findViewById(R.id.btnBack));

        this.txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.setVisibility(View.VISIBLE);
        txtSearch.setHint("搜索条件");
        txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(NetRequestListActivity.this, CaseSearchActivity.class);

                // key 为搜索框的当前值
                intent.putExtra("key", txtSearch.getText().toString());
                intent.putExtra("searchHint", "请求接口、请求方式、响应码");
                intent.putExtra("searchHistoryKey", "CaseListSearchHistory_netRequest");

                startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });
    }

    private void loadData() {

        querySql = new StringBuilder();
        //下拉时候currentIndex=1,isLoadMoreMode=false;上拉时候,currentIndex每次加1,isLoadMoreMode=true;
        loadPageIndex = isLoadMoreMode ? currentIndex : 0;
        boolean flagType = false;//默认sql语句type不添加
        boolean flagStatus = false;//默认sql语句status不添加
        boolean flagInterface = false;//默认sql语句interface不添加
        //增加tvSearch的刷选
        final String searchKey = txtSearch.getText().toString().trim();

        querySql.append("select * from NetLogInfo ");

        switch (filterType) {
            case "":
                flagType = false;
                break;
            case "POST":
                flagType = true;
                querySql.append(" where requestType='POST'");
                break;
            case "GET":
                flagType = true;
                querySql.append(" where requestType='GET'");
                break;

        }
        switch (filterStatus) {
            case "":
                flagStatus = false;
                break;
            case "成功":
                flagStatus = true;
                if (flagType) {
                    querySql.append(" and isSuccess=1");
                } else {
                    querySql.append(" where isSuccess=1");
                }
                break;
            case "失败":
                flagStatus = true;
                if (flagType) {
                    querySql.append(" and isSuccess=0");
                } else {
                    querySql.append(" where isSuccess=0");
                }
                break;
        }
        switch (filterInterface) {
            case "":
                flagInterface = false;
                break;
            case "空":
                flagInterface = true;
                if (!flagStatus && !flagType) {
                    querySql.append(" where requestInterface=''");
                } else {
                    querySql.append(" and requestInterface=''");
                }

                break;
            default:
                flagInterface = true;
                if (!flagStatus && !flagType) {
                    querySql.append(" where requestInterface='" + filterInterface + "'");
                } else {
                    querySql.append(" and requestInterface='" + filterInterface + "'");
                }
                break;
        }
        switch (searchKey) {
            case "":
                break;
            default:
                if (!flagStatus && !flagType && !flagInterface) {
                    querySql.append(" where requestInterface like '%" + searchKey + "%'" +
                            " or requestType like '%" + searchKey + "%'" +
                            " or responseCode like '%" + searchKey + "%'");
                } else {
                    querySql.append(" and (requestInterface like '%" + searchKey + "%'" +
                            " or requestType like '%" + searchKey + "%'" +
                            " or responseCode like '%" + searchKey + "%')");
                }
                break;
        }
        //开启线程计算查询信息的总数量，计算总页数，发消息
        new Thread() {
            @Override
            public void run() {

                try {
                    List<NetLogInfo> infoList = DatabaseHelper.getInstance().queryBySql(NetLogInfo.class, querySql.toString().trim());
                    int size = infoList.size();
                    totalPageCount = (int) Math.ceil(size * 1.0 / pageCount);
                    handler.sendEmptyMessage(MESSAGE_TOTALCOUNT);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }

    private void loadCompleted() {
        try {
            //order和limit放最后
            switch (filterTime) {
                case "":
                    querySql.append(" order by id desc");
                    break;
                case "时间升序":
                    querySql.append(" order by startTime asc ");
                    break;
                case "时间降序":
                    querySql.append(" order by startTime desc ");
                    break;
                case "请求耗时":
                    querySql.append(" order by timeSpan desc");
                    break;
                case "发送流量":
                    querySql.append(" order by sendBytes desc ");
                    break;
                case "接收流量":
                    querySql.append(" order by receiveBytes desc ");
                    break;
            }

            //limit.每次取pageCount条数据,从0,20,40,60等节点开始取
            querySql.append(" limit " + pageCount + " offset " + loadPageIndex * pageCount);

            new MmtBaseTask<Void, Void, ArrayList<NetLogInfo>>(this) {

                @Override
                protected ArrayList<NetLogInfo> doInBackground(Void... params) {
                    ArrayList<NetLogInfo> result =
                            (ArrayList<NetLogInfo>) DatabaseHelper.getInstance().queryBySql(NetLogInfo.class, querySql.toString().trim());

                    return result;
                }

                @Override
                protected void onSuccess(ArrayList<NetLogInfo> netLogInfos) {
                    refreshListView.onRefreshComplete();
                    if (netLogInfos == null || netLogInfos.size() == 0) {
                        if (!isLoadMoreMode) { // Refresh
                            Toast.makeText(NetRequestListActivity.this, "没有记录", Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                        } else {               // LoadMore
                            Toast.makeText(NetRequestListActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        if (isLoadMoreMode) {
                            currentIndex++; //加载更多成功更新当前页页码
                        } else {
                            Toast.makeText(NetRequestListActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                        }

                        handler.sendEmptyMessage(MESSAGE_LOADCOMPLETED);
                        mNetLogInfos.addAll(netLogInfos);
                        adapter.notifyDataSetChanged();

                    }

                }
            }.mmtExecute();
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public void onBackPressed() {
        String searchKey = txtSearch.getText().toString();
        if (!TextUtils.isEmpty(searchKey)) {
            txtSearch.setText("");
            txtSearch.setHint("搜索条件");
            refreshData();
        } else {
            super.onBackPressed();
        }
    }
}
