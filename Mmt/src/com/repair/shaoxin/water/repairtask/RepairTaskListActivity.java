package com.repair.shaoxin.water.repairtask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.Constants;

import java.util.ArrayList;
import java.util.List;

public class RepairTaskListActivity extends BaseActivity {

    private PullToRefreshListView mPullRefreshListView;
    protected ListView actualListView;
    private RepairTaskListAdapter adapter;

    private final List<RepairTaskItemEntity> repairTasks = new ArrayList<>();

    private int userID;

    @Override
    protected void setDefaultContentView() {
        setSwipeBackEnable(false);
        setContentView(R.layout.repair_task_list);

        this.userID = MyApplication.getInstance().getUserId();

        initView();
    }

    private void initView() {

        initActionBar();

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.mainFormList);
        actualListView = mPullRefreshListView.getRefreshableView();
        registerForContextMenu(actualListView);

        adapter = new RepairTaskListAdapter(RepairTaskListActivity.this, repairTasks);
        mPullRefreshListView.setAdapter(adapter);

        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.onDetailClick(position - 1);
            }
        });

        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(RepairTaskListActivity.this, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // 更下下拉面板
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                // 执行更新任务,结束后刷新界面
                getRepairTaskList();
            }
        });

        mPullRefreshListView.setRefreshing(false);
    }

    private void getRepairTaskList() {

        new AsyncTask<String, Void, ResultData<RepairTaskItemEntity>>() {

            @Override
            protected ResultData<RepairTaskItemEntity> doInBackground(String... params) {

                ResultData<RepairTaskItemEntity> resultData = new ResultData<>();

                try {
                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_Mobile/REST/MobileREST.svc/MobileService/GetRepairTask?userID="
                            + userID;

                    String rawJsonResult = NetUtil.executeHttpGet(url);

                    if (TextUtils.isEmpty(rawJsonResult)) {
                        throw new Exception("获取任务列表失败：网络错误");
                    }

                    ArrayList<RepairTaskItemEntity> taskList;

                    if (rawJsonResult.equals("\"]\"")) {
                        taskList = new ArrayList<>();

                    } else {
                        if (rawJsonResult.startsWith("\"") && rawJsonResult.endsWith("\"") && rawJsonResult.length() > 1
                                && (rawJsonResult.charAt(1) == '[' || rawJsonResult.charAt(1) == '{')) {
                            rawJsonResult = rawJsonResult.substring(1, rawJsonResult.length() - 1);
                        }
                        rawJsonResult = rawJsonResult.replace("\\", "");

                        taskList = new Gson().fromJson(rawJsonResult, new TypeToken<ArrayList<RepairTaskItemEntity>>(){}.getType());
                    }

                    resultData.ResultCode = 200;
                    resultData.DataList.addAll(taskList);

                } catch (Exception e) {
                    e.printStackTrace();
                    resultData.ResultCode = -100;
                    resultData.ResultMessage = e.getMessage();
                }

                return resultData;
            }

            @Override
            protected void onPostExecute(ResultData<RepairTaskItemEntity> resultData) {

                mPullRefreshListView.onRefreshComplete();

                if (resultData.ResultCode != 200) {
                    Toast.makeText(RepairTaskListActivity.this, resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Empty
                if (resultData.DataList.size() == 0) {
                    Toast.makeText(RepairTaskListActivity.this, "无新抢修工单", Toast.LENGTH_SHORT).show();
                }

                repairTasks.clear();
                repairTasks.addAll(resultData.DataList);
                adapter.notifyDataSetChanged();
            }
        }.executeOnExecutor(MyApplication.executorService);
    }

    private void initActionBar() {

        getBaseTextView().setText("抢修工单");

        addBackBtnListener(getBaseLeftImageView());

        ImageButton btnLoc = getBaseRightImageView();
        btnLoc.setVisibility(View.VISIBLE);
        btnLoc.setImageResource(R.drawable.navigation_locate);
        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final List<RepairTaskItemEntity> taskList = adapter.getDataList();
                MyApplication.getInstance().sendToBaseMapHandle(
                        new ShowAllTaskOnMapCallback(RepairTaskListActivity.this, taskList));
            }
        });
    }

    public void refreshData() {
        if (mPullRefreshListView != null) {
            mPullRefreshListView.setRefreshing(true);
            actualListView.setSelection(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.DEFAULT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            refreshData();
        }
    }

    private class RepairTaskListAdapter extends BaseAdapter implements View.OnClickListener {

        private List<RepairTaskItemEntity> dataList;
        private BaseActivity context;
        private LayoutInflater inflater;

        public List<RepairTaskItemEntity> getDataList() {
            return dataList;
        }

        RepairTaskListAdapter(BaseActivity activity, List<RepairTaskItemEntity> data) {
            this.inflater = LayoutInflater.from(activity);
            this.context = activity;
            this.dataList = data;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        int stateColor;
        String stateDesc;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.case_overview_list_item, parent, false);
            }

            final RepairTaskItemEntity task = dataList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_index))
                    .setText(getString(R.string.string_listitem_index, (position + 1)));

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_top_left)).setText(task.no);

            TextView tvHandleNode = MmtViewHolder.get(convertView, R.id.tv_mid_one_left);
            tvHandleNode.getPaint().setFakeBoldText(true);
            tvHandleNode.setText(task.eventSource); // 事件类型

            TextView state = MmtViewHolder.get(convertView, R.id.tv_mid_one_right);
            if (task.nodeType == -1) {
                stateColor = Color.RED;
                stateDesc = "未查看";
            } else if (task.nodeType == 0) {
                stateColor = Color.BLACK;
                stateDesc = "已查看";
            } else {
                stateColor = Color.BLUE;
                stateDesc = "处理中";
            }
            state.setTextColor(stateColor);
            state.setText(stateDesc);

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_two_left))
                    .setText(task.eventAddress); // 事件来源

            MmtViewHolder.get(convertView, R.id.tv_mid_three_left).setVisibility(View.GONE);
            MmtViewHolder.get(convertView, R.id.tv_mid_four_left).setVisibility(View.GONE);
            MmtViewHolder.get(convertView, R.id.tv_mid_four_right).setVisibility(View.GONE);

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_time)).setText(task.undertakeTime);
            TextView tvLoc = MmtViewHolder.get(convertView, R.id.tv_loc);

//            tvLoc.setTag(task.patrolPosition);
            tvLoc.setTag(String.valueOf(position));
            tvLoc.setOnClickListener(this);

            return convertView;
        }

        @Override
        public void onClick(View v) {

            // 定位按钮
            if (v.getId() == R.id.tv_loc) {

                RepairTaskItemEntity task = dataList.get(Integer.parseInt(v.getTag().toString()));

                if (TextUtils.isEmpty(task.patrolPosition)) {
                    Toast.makeText(RepairTaskListActivity.this, "无坐标信息", Toast.LENGTH_SHORT).show();
                } else {
                    BaseMapCallback callback = new ShowMapPointCallback(RepairTaskListActivity.this, task.patrolPosition, task.no, task.eventSource, -1);
                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            }
        }

        public void onDetailClick(int position) {

            RepairTaskItemEntity task = dataList.get(position);

            Intent intent = new Intent(context, RepairTaskDetailActivity.class);
            intent.putExtra("Type", task.eventSource);
            intent.putExtra("TaskID", task.taskID);
            intent.putExtra("CurrentNodeType", task.nodeType);
            intent.putExtra("OrderID", task.orderID);

            // "未查看"状态的工单查看详情的时候会标志为"已查看",返回来时需要刷新列表
            if (task.nodeType == -1) {
                intent.putExtra("IsRead", false);
            }
            startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
            MyApplication.getInstance().startActivityAnimation(context);
        }
    }


}
