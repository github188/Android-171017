package com.maintainproduct.module.maintenance.list;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton.OnScrollListener;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class MaintenanceListFragment extends Fragment {
    protected final ArrayList<LinkedHashMap<String, String>> data = new ArrayList<LinkedHashMap<String, String>>();

    public MultiSwitchButton switchButton;
    private final String[] rule = new String[]{"按工单距离 ↑", "按承办日期"};
    public int sortType = 0;
    protected PullToRefreshListView listView;
    public MaintenanceItemAdapter adapter;
    private BaseActivity deatilActivity = null;
    private boolean isUseAutoFresh = true;
    //按工单距离和日期 默认升序
    protected boolean isTimeAsc = true;
    protected boolean isDisAsc = true;
    protected ListView actualListView;
    //当前页面第一个可见item
    public int firstVisivleItemPos = 0;
    private final RefreahThread thread = new RefreahThread();

    public void setIsUseAutoFresh(boolean isUseAutoFresh) {
        this.isUseAutoFresh = isUseAutoFresh;
    }

    public MaintenanceListFragment() {
    }

    public MaintenanceListFragment(BaseActivity deatilActivity) {
        this.deatilActivity = deatilActivity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.maintenance_list_view, container, false);

        switchButton = (MultiSwitchButton) view.findViewById(R.id.maintenanceListTitle);
        switchButton.setContent(rule);

        listView = (PullToRefreshListView) view.findViewById(R.id.maintenanceFormList);

        actualListView = listView.getRefreshableView();

        registerForContextMenu(actualListView);

        adapter = new MaintenanceItemAdapter(data, (BaseActivity) getActivity());
        if (this.deatilActivity != null) {
            adapter = new MaintenanceItemAdapter(data, (BaseActivity) getActivity(), this.deatilActivity);

        }
        actualListView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // 滚动选择条件
        switchButton.setOnScrollListener(new OnScrollListener() {
            @Override
            public void OnScrollComplete(int index) {
                sortType = index;
                handler.sendEmptyMessage(MaintenanceConstant.CASE_LIST_REFREASH);
            }
        });

        // 每项的点击事件
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                adapter.onDetailClick(arg2 - 1);
            }
        });

        // 给listview添加刷新监听器
        listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // 更新下拉面板
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                // 执行更新任务,结束后刷新界面
                new MaintenanceListTask(handler).executeOnExecutor(MyApplication.executorService, "");
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
        listView.setRefreshing(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thread.isRun = false;
    }
    //将移交、结案的记录从list从移除
    public void updateView() {
        if(actualListView!=null) {
            data.remove(adapter.curClickPos);
            adapter.notifyDataSetChanged();
            actualListView.setSelection(firstVisivleItemPos);
        }
    }
    protected  Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            listView.onRefreshComplete();

            switch (msg.what) {
                case MaintenanceConstant.SERVER_GET_LIST_SUCCESS:

                    if (msg.obj != null && (msg.obj instanceof ArrayList)) {
                        data.clear();
                        data.addAll((ArrayList<LinkedHashMap<String, String>>) msg.obj);
                    }

                    // 去除缓存在本地还未上报的数据
                    data.removeAll(unReporterdData());

                    GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

                    for (LinkedHashMap<String, String> hashMap : data) {
                        MaintenanceListUtil.putBetTime(hashMap);
                        MaintenanceListUtil.putDistance(hashMap, xyz);
                    }

                    handler.sendEmptyMessage(MaintenanceConstant.CASE_LIST_AutoREFREASH);

                    if (isUseAutoFresh && !thread.isAlive()) {
                        thread.start();
                    }

                    break;
                case MaintenanceConstant.SERVER_GET_LIST_FAIL:
                    break;
                case MaintenanceConstant.CASE_LIST_REFREASH:
                    switch (sortType) {
                        case 0:// 按工单距离
                            if (isDisAsc) {
                                switchButton.setContent(new String[]{"按工单距离 ↑", "按承办日期"});
                            } else {
                                switchButton.setContent(new String[]{"按工单距离 ↓", "按承办日期"});
                            }
                            sortByDistance();
                            isDisAsc = !isDisAsc;
                            break;
                        case 1:// 按承办日期
                            if (isTimeAsc) {
                                switchButton.setContent(new String[]{"按工单距离", "按承办日期 ↑"});
                            } else {
                                switchButton.setContent(new String[]{"按工单距离", "按承办日期 ↓"});
                            }
                            sortByDeadline();
                            isTimeAsc = !isTimeAsc;
                            break;
                    }

                    adapter.refreash(sortType);
                    break;
                case MaintenanceConstant.CASE_LIST_AutoREFREASH:
                    switch (sortType) {
                        case 0:// 按工单距离
                            sortByDistance();
                            break;
                        case 1:// 按承办日期
                            sortByDeadline();
                            break;
                    }
                    adapter.refreash(sortType);
                    break;
            }
        }
    };

    /**
     * 获取缓存在本地的数据。本地还未上报的数据，不显示在列表中
     */
    protected List<LinkedHashMap<String, String>> unReporterdData() {
        List<LinkedHashMap<String, String>> removeData = new ArrayList<LinkedHashMap<String, String>>();

        List<ReportInBackEntity> reportInBackEntities = DatabaseHelper.getInstance().query(ReportInBackEntity.class,
                new SQLiteQueryParameters("status=" + ReportInBackEntity.REPORTING));

        for (LinkedHashMap<String, String> map : data) {
            for (ReportInBackEntity reportInBackEntity : reportInBackEntities) {
                if (map.get("案件编号").equals(reportInBackEntity.getKey())) {
                    removeData.add(map);
                }
            }
        }
        return removeData;
    }

    /**
     * 获取并刷新数据
     */
    public void updateData() {
        listView.setRefreshing(false);
    }

    /**
     * 按当前距离远近进行排序
     */
    private void sortByDistance() {
        if (data == null || data.size() == 0) {
            return;
        }

        final GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

        // 未获获取到定位坐标
        if (xyz.getX() == 0.0 && xyz.getY() == 0.0) {
            return;
        }

        Collections.sort(data, new Comparator<LinkedHashMap<String, String>>() {

            @Override
            public int compare(LinkedHashMap<String, String> lhs, LinkedHashMap<String, String> rhs) {

                MaintenanceListUtil.putDistance(lhs, xyz);
                double distance1 = Double.valueOf(lhs.get(MaintenanceListUtil.Distance));

                MaintenanceListUtil.putDistance(rhs, xyz);
                double distance2 = Double.valueOf(rhs.get(MaintenanceListUtil.Distance));
                if (isDisAsc) {
                    return distance1 >= distance2 ? 1 : -1;
                } else {
                    return distance1 >= distance2 ? -1 : 1;
                }
            }
        });
    }

    /**
     * 默认升序
     */
    protected void sortByDeadline() {
        if (data == null || data.size() == 0) {
            return;
        }

        Collections.sort(data, new Comparator<LinkedHashMap<String, String>>() {

            @Override
            public int compare(LinkedHashMap<String, String> lhs, LinkedHashMap<String, String> rhs) {
                try {

                    MaintenanceListUtil.putBetTime(lhs);
                    long betTime1 = Long.valueOf(lhs.get(MaintenanceListUtil.BetTime));

                    MaintenanceListUtil.putBetTime(rhs);
                    long betTime2 = Long.valueOf(rhs.get(MaintenanceListUtil.BetTime));
                    if (isTimeAsc) {
                        return betTime2 >= betTime1 ? 1 : -1;
                    } else {
                        return betTime2 >= betTime1 ? -1 : 1;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
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
}
