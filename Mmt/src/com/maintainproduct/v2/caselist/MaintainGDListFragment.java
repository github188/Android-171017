package com.maintainproduct.v2.caselist;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.maintainproduct.module.maintenance.list.MaintenanceListUtil;
import com.maintainproduct.v2.callback.GDAllLocationOnMapCallback;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton.OnScrollListener;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MaintainGDListFragment extends Fragment {

    public ArrayList<GDItem> dataList;

    private PullToRefreshListView mPullRefreshListView;
    private MaintainGDListAdapter adapter;

    private MultiSwitchButton switchButton;
    private final String[] rule = new String[]{"按工单距离", "按承办日期"};
    private int sortType = 0;

    private final RefreahThread thread = new RefreahThread();

    public MaintainGDListFragment() {
        dataList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.maintenance_list_view, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.maintenanceFormList);
        adapter = new MaintainGDListAdapter(dataList, getActivity().getLayoutInflater());
        mPullRefreshListView.setAdapter(adapter);

        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                GetGDListTask task = new GetGDListTask();
                task.executeOnExecutor(MyApplication.executorService);
            }
        });
        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GDItem gdItem = (GDItem) parent.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(), MaintainGDDetailActivity.class);
                intent.putExtra("OneLocTitle", gdItem.ReportType + "/" + gdItem.ReportContent);
                intent.putExtra("OneLocDescription", gdItem.CaseCode);
                intent.putExtra("GDState", gdItem.State);
                intent.putExtra("CaseID", gdItem.CaseID);
                intent.putExtra("ReadGDTime", gdItem.ReadGDTime);
                gdItem.ReadGDTime = BaseClassUtil.getSystemTime(); // 防止 多次阅单
                intent.putExtra("ListItemEntity", gdItem.build());
                getActivity().startActivityForResult(intent, MaintainConstant.DEFAULT_REQUEST_CODE);
                MyApplication.getInstance().startActivityAnimation(getActivity());
            }
        });
        mPullRefreshListView.setRefreshing(false);

        switchButton = (MultiSwitchButton) view.findViewById(R.id.maintenanceListTitle);
        switchButton.setContent(rule);

        // 滚动选择条件
        switchButton.setOnScrollListener(new OnScrollListener() {
            @Override
            public void OnScrollComplete(int index) {
                sortType = index;
                handler.sendEmptyMessage(MaintenanceConstant.CASE_LIST_REFREASH);
            }
        });
    }

    @Override
    public void onDestroy() {
        thread.isRun = false;
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String CaseNo = "";
        switch (resultCode) {
            case MaintainConstant.SERVER_READ_GD_DONE: // 　阅单成功
                CaseNo = data.getStringExtra("CaseNo");
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).CaseNO.toString().equals(CaseNo)) {
                        dataList.get(i).State = "已阅读";
                        adapter.refreash(sortType);
                        break;
                    }
                }
                break;
            case MaintainConstant.SERVER_RECEIVE_GD_DONE:
                CaseNo = data.getStringExtra("CaseNo");
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).CaseNO.toString().equals(CaseNo)) {
                        dataList.get(i).State = "待处理";
                        adapter.refreash(sortType);
                        break;
                    }
                }
                break;
            case MaintainConstant.DaoChang:
                CaseNo = data.getStringExtra("CaseNo");
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).CaseNO.toString().equals(CaseNo)) {
                        dataList.get(i).State = "处理中";
                        adapter.refreash(sortType);
                        break;
                    }
                }
                break;
            case MaintainConstant.WanCheng:
                CaseNo = data.getStringExtra("CaseNo");
                int deleteIndex = -1;
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).CaseNO.toString().equals(CaseNo)) {
                        deleteIndex = i;
                        break;
                    }
                }
                dataList.remove(deleteIndex);
                adapter.refreash(sortType);
                break;
            // 退单后 立即 删除 内存中 的 此工单 数据，并刷新 列表
            case MaintainConstant.TuiDan:
                CaseNo = data.getStringExtra("CaseNo");
                int deleteTuiDanIndex = -1;
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).CaseNO.toString().equals(CaseNo)) {
                        deleteTuiDanIndex = i;
                        break;
                    }
                }
                dataList.remove(deleteTuiDanIndex);
                adapter.refreash(sortType);
                break;
            default:
                break;
        }

    }

    private final Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 0:
                        adapter.refreash(sortType);
                        break;
                    case 1:
                        if (msg.obj != null) {
                            dataList.clear();
                            dataList.addAll(((ResultData<GDItem>) msg.obj).DataList);
                            if (dataList.size() == 0) {
                                MyApplication.getInstance().showMessageWithHandle("未分派维修任务");
                            } else {
                                for (GDItem item : dataList) {
                                    MaintenanceListUtil.calculateBetTime(item);
                                    MaintenanceListUtil.calculateDistance(item, GpsReceiver.getInstance().getLastLocalLocation());
                                }

                                if (!thread.isAlive()) {
                                    thread.start();
                                }
                            }
                        }
                        mPullRefreshListView.onRefreshComplete();
                        adapter.refreash(sortType);
                        break;
                    case MaintenanceConstant.CASE_LIST_REFREASH:
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
                    default:
                        break;
                }
            } catch (Exception e) {

            }
        }
    };

    private class GetGDListTask extends AsyncTask<String, Integer, ResultData<GDItem>> {
        @Override
        protected ResultData<GDItem> doInBackground(String... params) {
            ResultData<GDItem> resultObj = null;
            try {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/services/MapgisCity_WXYH_GL_MOBILE/REST/CaseManageREST.svc/GetWXCaseList";
                String resultStr = NetUtil.executeHttpGet(url, "UserID", String.valueOf(MyApplication.getInstance().getUserId()));
                if (resultStr != null) {
                    resultObj = new Gson().fromJson(resultStr.replaceFirst("\"resultContent\"", "\"DataList\""),
                            new TypeToken<ResultData<GDItem>>() {
                            }.getType());
                }
            } catch (Exception e) {
                return null;
            }
            return resultObj;
        }

        @Override
        protected void onPostExecute(ResultData<GDItem> result) {
            if (result == null) {
                mPullRefreshListView.onRefreshComplete();
                return;
            }
            try {
                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = result;
                msg.sendToTarget();
            } catch (Exception e) {

            }
        }
    }

    protected void showOnMap() {
        MyApplication.getInstance().sendToBaseMapHandle(new GDAllLocationOnMapCallback(dataList, MaintainGDListActivity.class));
        Intent intent = new Intent(getActivity(), MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    /**
     * 按当前距离远近进行排序
     */
    private void sortByDistance() {
        if (dataList == null || dataList.size() <= 1) {
            return;
        }

        final GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

        // 未获获取到定位坐标
        if (xyz.getX() == 0.0 && xyz.getY() == 0.0) {
            return;
        }

        Collections.sort(dataList, new Comparator<GDItem>() {

            @Override
            public int compare(GDItem lhs, GDItem rhs) {
                MaintenanceListUtil.calculateDistance(lhs, xyz);
                MaintenanceListUtil.calculateDistance(rhs, xyz);

                if (BaseClassUtil.isNullOrEmptyString(lhs.Distance) || !BaseClassUtil.isNum(lhs.Distance)) {
                    return 1;
                }

                if (BaseClassUtil.isNullOrEmptyString(rhs.Distance) || !BaseClassUtil.isNum(rhs.Distance)) {
                    return -1;
                }

                double distance1 = Double.valueOf(lhs.Distance);
                double distance2 = Double.valueOf(rhs.Distance);

                return distance1 >= distance2 ? 1 : -1;
            }
        });
    }

    /**
     * 按时间大小进行排序
     */
    private void sortByDeadline() {
        if (dataList == null || dataList.size() <= 1) {
            return;
        }

        Collections.sort(dataList, new Comparator<GDItem>() {
            @Override
            public int compare(GDItem lhs, GDItem rhs) {
                MaintenanceListUtil.calculateBetTime(lhs);
                MaintenanceListUtil.calculateBetTime(rhs);

                if (BaseClassUtil.isNullOrEmptyString(lhs.BetTime) || !BaseClassUtil.isNum(lhs.BetTime)) {
                    return 1;
                }

                if (BaseClassUtil.isNullOrEmptyString(lhs.BetTime) || !BaseClassUtil.isNum(rhs.BetTime)) {
                    return -1;
                }

                long betTime1 = Long.valueOf(lhs.BetTime);
                long betTime2 = Long.valueOf(rhs.BetTime);

                return betTime1 >= betTime2 ? 1 : -1;
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

                handler.sendEmptyMessage(MaintenanceConstant.CASE_LIST_REFREASH);
            }
        }
    }
}
