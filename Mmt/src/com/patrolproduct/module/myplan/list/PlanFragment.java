package com.patrolproduct.module.myplan.list;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.SavedReportInfo;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.map.pager.MmtMapBottomFragment;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.mapgis.mmt.R;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.MyPlanUtil;
import com.patrolproduct.module.myplan.SessionManager;
import com.patrolproduct.module.myplan.entity.PatrolTask;
import com.patrolproduct.module.myplan.map.PlanPageFragment;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

/**
 * 今日计划
 */
public class PlanFragment extends MmtMapBottomFragment {
    private final NavigationItem item;
    private UpdateStateTask updateStateTask;

    public PlanFragment(MapGISFrame mapGISFrame, NavigationItem item) {
        super(mapGISFrame);
        this.item = item;
    }

    private PullToRefreshListView mPullRefreshListView;
    private PlanAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pull_list, container, false);
        view.setBackgroundResource(R.color.white);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.order_form_list);

        adapter = new PlanAdapter(getActivity(), SessionManager.patrolTaskList);

        mPullRefreshListView.setAdapter(adapter);

        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity().getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // 更下下拉面板
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                // 执行更新任务,结束后刷新界面
                updateStateTask = new UpdateStateTask();
                updateStateTask.executeOnExecutor(MyApplication.executorService);
            }
        });

        ListView actualListView = mPullRefreshListView.getRefreshableView();

        registerForContextMenu(actualListView);

        actualListView.setAdapter(adapter);

        if (myItemClickListener != null) {
            mPullRefreshListView.setOnItemClickListener(myItemClickListener);
        }

        // 判断有无计划或新计划,有则先更新状态,在更新计划信息
        if (SessionManager.patrolTaskList.size() == 0 || !item.doneNewMsg) {
            item.doneNewMsg = true;
            mPullRefreshListView.setRefreshing(false);
        } else {
            adapter.notifyDataSetChanged();
            addViewPager();
        }
    }

    @Override
    public void onDestroy() {
        if (updateStateTask != null && !updateStateTask.isCancelled()) {
            updateStateTask.cancel(true);
        }
        super.onDestroy();
    }

    /**
     * 更新计划状态，主要是计划的到位状态以及反馈状态
     */
    class UpdateStateTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/PatrolREST.svc/GetTaskState";

            String userID = String.valueOf(MyApplication.getInstance().getUserId());
            String flowID = String.valueOf(MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).PatrolPlanID);

            String result = NetUtil.executeHttpGetAppointLastTime(60, url, "userID", userID, "flowid", flowID);

            if (isCancelled() || BaseClassUtil.isNullOrEmptyString(result)) {
                return null;
            }

            ArrayList<Hashtable<String, String>> data = MyPlanUtil.dataToList(result);

            if (data.size() > 0) {
                SessionManager.taskStateTable.clear();
                SessionManager.taskStateTable.addAll(data);

                ArrayList<SavedReportInfo> infos = DatabaseHelper.getInstance().query(SavedReportInfo.class,
                        "reportType='arrive' or reportType='feedback'");

                for (SavedReportInfo savedReportInfo : infos) {
                    // 反馈数据
                    ArrayList<LinkedHashMap> items = new Gson().fromJson(savedReportInfo.getReportContent(), new TypeToken<ArrayList<LinkedHashMap>>() {
                    }.getType());

                    String[] args = new String[3];

                    args[0] = savedReportInfo.getTaskId();

                    // 防止遇到已到位点未上传成功时保存在本地情况时，args[1]和args[2]为空时引发的异常问题
                    args[1] = "";
                    args[2] = "";

                    for (LinkedHashMap item : items) {
                        if (item.get("Type").equals("0") && item.get("Name").equals("equiptype")) {
                            args[1] = String.valueOf(item.get("Value"));
                        } else if (item.get("Type").equals("0") && item.get("Name").equals("equipentity")) {
                            args[2] = String.valueOf(item.get("Value"));
                        }
                    }

                    MyPlanUtil.updateArriveOrFeedbackState(args[0], args[1], args[2],
                            savedReportInfo.getReportType().equals("arrive"));
                }

//                MyApplication.getInstance().submitExecutorService(new Runnable() {
//                    @Override
//                    public void run() {
//                        saveIntoDb4o(SessionManager.taskStateTable);
//                    }
//                });

                return "sucess";
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            mPullRefreshListView.onRefreshComplete();
            if (s == null) {
                Toast.makeText(getActivity(), "我的计划：计划状态获取失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (0 == s.length()) {
                Toast.makeText(getActivity(), "我的计划：没有计划任务", Toast.LENGTH_SHORT).show();
                return;
            }
            if (s.equals("sucess")) {
                new UpdatePlanListTask().executeOnExecutor(MyApplication.executorService);
            }
        }

        private void saveIntoDb4o(ArrayList<Hashtable<String, String>> table) {
            try {
                for (Hashtable<String, String> row : table) {
                    final PatrolDevice device = new PatrolDevice();

                    device.TaskId = Integer.valueOf(row.get("taskId"));
                    device.LayerName = row.get("layerName");
                    device.PipeNo = row.get("pipeId");

                    device.IsArrived = !BaseClassUtil.isNullOrEmptyString(row.get("isArrive"))
                            && Integer.valueOf(row.get("isArrive")) > 0;
                    device.IsFeedbacked = !BaseClassUtil.isNullOrEmptyString(row.get("isFeedback"))
                            && Integer.valueOf(row.get("isFeedback")) > 0;

                    PatrolDevice deviceDB = device.fromDB();

                    if (deviceDB != null) {
                        ContentValues cv = new ContentValues();

                        if (device.IsArrived && !deviceDB.IsArrived) {
                            cv.put("isArrived", true);
                        }

                        if (device.IsFeedbacked && !deviceDB.IsFeedbacked) {
                            cv.put("isFeedbacked", true);
                        }

                        if (cv.size() > 0) {
                            DatabaseHelper.getInstance().update(PatrolDevice.class, cv, "id=" + deviceDB.ID);
                        }
                    } else {
                        DatabaseHelper.getInstance().insert(device);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新计划，获取的是计划的文本信息
     */
    class UpdatePlanListTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/PatrolREST.svc/GetPlan";
            String userID = String.valueOf(MyApplication.getInstance().getUserId());
            String flowID = String.valueOf(MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).PatrolPlanID);

            return NetUtil.executeHttpGet(url, "userID", userID, "flowid", flowID);
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                if (!BaseClassUtil.isNullOrEmptyString(result)) {

                    ResultData<PatrolTask> jsonResult = new Gson().fromJson(result, new TypeToken<ResultData<PatrolTask>>() {
                    }.getType());

                    SessionManager.patrolTaskList.clear();
                    SessionManager.patrolTaskList.addAll(jsonResult.DataList);

                    adapter.notifyDataSetChanged();

                    addViewPager();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mPullRefreshListView.onRefreshComplete();
            }

        }
    }

    private OnItemClickListener myItemClickListener;

    public void setOnMyItemClickListener(OnItemClickListener myItemClickListener) {
        this.myItemClickListener = myItemClickListener;
    }

    @Override
    public PlanPageFragment[] createPageFragment() {
        PlanPageFragment[] fragments = new PlanPageFragment[SessionManager.patrolTaskList.size()];

        for (int i = 0; i < SessionManager.patrolTaskList.size(); i++) {
            fragments[i] = new PlanPageFragment(SessionManager.patrolTaskList.get(i));

            if (i == 0) {
                fragments[i].isPreArrowShow = false;
            }

            if (i == SessionManager.patrolTaskList.size() - 1) {
                fragments[i].isNextArrowShow = false;
            }

        }
        return fragments;
    }

    public void onHiddenChanged(boolean hidden) {
        try {
            super.onHiddenChanged(hidden);

            if (!hidden) {
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
