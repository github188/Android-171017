package com.repair.shaoxin.water.hotlinetask;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.List;

public class HotlineTaskListActivity extends BaseActivity {

    private static final int DEFAULT_REQUEST_CODE = 0x111;

    private final List<HotlineTaskEntity> dataLists = new ArrayList<>();

    private PullToRefreshListView mPullRefreshListView;
    protected ListView actualListView;
    private HotlineTaskAdapter adapter;

    private int userID;

    @Override
    protected void setDefaultContentView() {
        setSwipeBackEnable(false);
        setContentView(R.layout.repair_task_list);

        this.userID = MyApplication.getInstance().getUserId();

        initView();
    }

    private void initView() {

        getBaseTextView().setText("热线工单");

        addBackBtnListener(getBaseLeftImageView());

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.mainFormList);
        actualListView = mPullRefreshListView.getRefreshableView();
        registerForContextMenu(actualListView);

        adapter = new HotlineTaskAdapter(HotlineTaskListActivity.this, dataLists);
        mPullRefreshListView.setAdapter(adapter);

        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                HotlineTaskEntity hotlineTask = (HotlineTaskEntity) parent.getItemAtPosition(position);

                if (hotlineTask.stateName.equals("未阅读")) {
                    setToRead(hotlineTask); // 未查看状态修改为已查看状态
                }

                Intent intent = new Intent(HotlineTaskListActivity.this, HotlineTaskDetailActivity.class);
                intent.putExtra("HotlineTask", hotlineTask);
                startActivityForResult(intent, DEFAULT_REQUEST_CODE);
            }
        });

        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(HotlineTaskListActivity.this, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // 更下下拉面板
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                // 执行更新任务,结束后刷新界面
                getHotlineTaskList();
            }
        });

        mPullRefreshListView.setRefreshing(false);

    }

    private void setToRead(final HotlineTaskEntity hotlineTask) {

        new AsyncTask<String, Void, Void>() {

            @Override
            protected Void doInBackground(String... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/CallCenterREST.svc/SetCaseRead?caseNo="
                        + params[0];

                NetUtil.executeHttpGet(url);

                hotlineTask.stateName = "已查阅"; // 修改当前缓存数据的查看状态

                return null;
            }
        }.executeOnExecutor(MyApplication.executorService, hotlineTask.workTaskSeq);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DEFAULT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            refreshData();
        }
    }

    public void refreshData() {
        if (mPullRefreshListView != null) {
            mPullRefreshListView.setRefreshing(true);
            actualListView.setSelection(0);
        }
    }

    private AsyncTask<String, Void, ResultData<HotlineTaskEntity>> loadListTask;

    private void getHotlineTaskList() {

        loadListTask = new AsyncTask<String, Void, ResultData<HotlineTaskEntity>>() {

            @Override
            protected ResultData<HotlineTaskEntity> doInBackground(String... params) {

                ResultData<HotlineTaskEntity> resultData;

                try {
                    // String url = "http://60.190.213.242:8082/CityInterface"
                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/CallCenterREST.svc/GetCaseListByUserIdAndType?type=1&userId="
                            + userID;

                    String jsonResult = NetUtil.executeHttpGet(url);

                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取任务列表失败：网络错误");
                    }

                    resultData = new Gson().fromJson(jsonResult, new TypeToken<ResultData<HotlineTaskEntity>>() {
                    }.getType());

                } catch (Exception e) {
                    e.printStackTrace();
                    resultData = new ResultData<>();
                    resultData.ResultCode = -100;
                    resultData.ResultMessage = e.getMessage();
                }

                return resultData;
            }

            @Override
            protected void onPostExecute(ResultData<HotlineTaskEntity> resultData) {

                mPullRefreshListView.onRefreshComplete();

                if (resultData.ResultCode != 200) {
                    Toast.makeText(HotlineTaskListActivity.this, resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Empty
                if (resultData.DataList.size() == 0) {
                    Toast.makeText(HotlineTaskListActivity.this, "暂无热线工单", Toast.LENGTH_SHORT).show();
                }

                dataLists.clear();
                dataLists.addAll(resultData.DataList);
                adapter.notifyDataSetChanged();
            }
        }.executeOnExecutor(MyApplication.executorService);
    }

    @Override
    protected void onDestroy() {
        if (loadListTask != null && !loadListTask.isCancelled()) {
            loadListTask.cancel(true);
        }
        super.onDestroy();
    }

    private class HotlineTaskAdapter extends BaseAdapter {

        private Context context;
        private final LayoutInflater inflater;
        private final List<HotlineTaskEntity> lists;

        HotlineTaskAdapter(BaseActivity activity, List<HotlineTaskEntity> lists) {
            this.context = activity;
            this.inflater = LayoutInflater.from(activity);
            this.lists = lists;
        }

        @Override
        public int getCount() {
            return lists.size();
        }

        @Override
        public Object getItem(int position) {
            return lists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.base_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.hotlineListItemIndex = (TextView) convertView.findViewById(R.id.itemIndex);
                viewHolder.hotlineListItemWorkTaskSeq = (TextView) convertView.findViewById(R.id.desc_top_left);
                viewHolder.hotlineListItemSaveDate = (TextView) convertView.findViewById(R.id.desc_mid_left);
                viewHolder.hotlineListItemComplainTypeName = (TextView) convertView.findViewById(R.id.desc_mid_bottom_left);
                viewHolder.hotlineListItemServiceTime = (TextView) convertView.findViewById(R.id.desc_bottom_left);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final HotlineTaskEntity data = lists.get(position);

            viewHolder.hotlineListItemIndex.setText(
                    context.getString(R.string.string_listitem_index, position + 1));
            viewHolder.hotlineListItemWorkTaskSeq.setText(
                    context.getString(R.string.string_two_with_colon, "工单编号", data.workTaskSeq));
            viewHolder.hotlineListItemSaveDate.setText(
                    context.getString(R.string.string_two_with_colon, "下单时间", data.saveDate));
            viewHolder.hotlineListItemComplainTypeName.setText(
                    context.getString(R.string.string_two_with_colon, "反应类别", data.complainTypeName));
            viewHolder.hotlineListItemServiceTime.setTextColor(Color.BLACK);
            viewHolder.hotlineListItemServiceTime.setText(
                    context.getString(R.string.string_two_with_colon, "20/30 分", data.serviceTime));

            return convertView;
        }

        class ViewHolder {
            TextView hotlineListItemIndex;
            TextView hotlineListItemWorkTaskSeq;
            TextView hotlineListItemSaveDate;
            TextView hotlineListItemComplainTypeName;
            TextView hotlineListItemServiceTime;
        }
    }
}
