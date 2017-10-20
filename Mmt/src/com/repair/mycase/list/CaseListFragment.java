package com.repair.mycase.list;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;
import com.repair.common.CaseItem;
import com.repair.common.CaseSearchActivity;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.mycase.MyCaseActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CaseListFragment extends Fragment implements View.OnTouchListener, View.OnClickListener {
    private List<CaseItem> data = new ArrayList<>();
    private ProgressDialog loadingDialog;

    private PullToRefreshListView listView;
    public CaseListAdapter adapter;
    private int selectedSortItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.loadingDialog = MmtProgressDialog.getLoadingProgressDialog(getActivity(), "正在处理,请稍候...");

        View view = inflater.inflate(R.layout.wx_case_list, container, false);

        listView = (PullToRefreshListView) view.findViewById(R.id.maintenanceFormList);

        ListView actualListView = listView.getRefreshableView();

        registerForContextMenu(actualListView);

        adapter = new CaseListAdapter((BaseActivity) getActivity(), data);
        actualListView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText(getActivity().getIntent().getStringExtra("alias"));

        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(this);

        // 每项的点击事件
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                OnItemClick((int) arg3, "list");
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
                new FetchListTask(getActivity(), false) {
                    @Override
                    protected ResultData<CaseItem> doInBackground(Void... params) {
                        ResultData<CaseItem> data = super.doInBackground(params);

                        if (data != null && data.DataList != null && data.DataList.size() > 0) {
                            sort(data.DataList);
                        }

                        return data;
                    }

                    @Override
                    protected void onPostExecute(ResultData<CaseItem> result) {
                        onReceiveCaseList(result);
                        super.onPostExecute(result);
                    }
                }.executeOnExecutor(MyApplication.executorService);
            }
        });

        selectedSortItem = R.id.tvLeft;

        view.findViewById(R.id.tvLeft).setOnTouchListener(this);
        view.findViewById(R.id.tvMiddle).setOnTouchListener(this);
        view.findViewById(R.id.tvRight).setOnTouchListener(this);

        ImageButton btnSearch = (ImageButton) view.findViewById(R.id.baseActionBarRightImageView);
        btnSearch.setVisibility(View.VISIBLE);
        btnSearch.setImageResource(R.drawable.search_white);
        btnSearch.setOnClickListener(this);

        listView.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.baseActionBarRightImageView) {
            Intent intent = new Intent(getActivity(), CaseSearchActivity.class);

            intent.putExtra("key", key);

            startActivityForResult(intent, 1);
        } else if (v.getId() == R.id.baseActionBarImageView) {
            getActivity().onBackPressed();
        }
    }

    String key;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || resultCode != 1)
            return;

        key = data.getStringExtra("key");//返回空代表查全部

        search();
    }

    private void search() {
        try {
            ArrayList<CaseItem> items = new ArrayList<>();

            if (TextUtils.isEmpty(key)) {
                items.addAll(sourceItems);
            } else {
                for (CaseItem item : this.sourceItems) {
                    boolean isOk = item.CaseID.contains(key) || item.EventClass.contains(key)
                            || item.EventType.contains(key) || item.Address.contains(key)
                            || item.Description.contains(key) || item.EventCode.contains(key);

                    if (isOk)
                        items.add(item);
                }
            }

            if (items.size() > 0) {
                this.data.clear();
                this.data.addAll(items);

                sortCaseList();
            } else {
                Toast.makeText(getActivity(), "没有符合条件的结果", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ArrayList<CaseItem> sourceItems = new ArrayList<>();

    private void onReceiveCaseList(ResultData<CaseItem> result) {
        try {
            if (getActivity() == null || getActivity().isFinishing())
                return;

            listView.onRefreshComplete();

            if (result == null) {
                Toast.makeText(getActivity(), "查询数据失败", Toast.LENGTH_SHORT).show();

                return;
            }

            if (result.ResultCode < 0) {
                Toast.makeText(getActivity(), result.ResultMessage, Toast.LENGTH_SHORT).show();

                return;
            }

            sourceItems.clear();
            sourceItems.addAll(result.DataList);

            data.clear();
            data.addAll(result.DataList);

            if (!TextUtils.isEmpty(key))
                search();

            adapter.notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            selectedSortItem = v.getId();

            sortCaseList();
        }

        return false;
    }

    private void sortCaseList() {
        if (data == null || data.size() == 0)
            return;

        new MmtBaseTask<String, Integer, String>(getActivity()) {
            @Override
            protected String doInBackground(String... params) {
                sort(data);

                return "";
            }

            @Override
            protected void onPostExecute(String result) {
                adapter.notifyDataSetChanged();

                super.onPostExecute(result);
            }
        }.executeOnExecutor(MyApplication.executorService);
    }

    public void OnItemClick(int pos, String from) {
        if (adapter == null)
            return;

        CaseItem caseItem = data.get(pos);

        adapter.onDetailClick(caseItem, from);
    }

    private void sort(List<CaseItem> data) {
        final GpsXYZ xy = GpsReceiver.getInstance().getLastLocalLocation();

        Collections.sort(data, new Comparator<CaseItem>() {

            @Override
            public int compare(CaseItem lhs, CaseItem rhs) {
                try {
                    boolean lFinish = lhs.State.equals("已完工");
                    boolean rFinish = rhs.State.equals("已完工");

                    if (lFinish && !rFinish)
                        return 1;

                    if (!lFinish && rFinish)
                        return -1;

                    String lTime = TextUtils.isEmpty(lhs.DelayFinishTime) ? lhs.PredictFinishTime : lhs.DelayFinishTime;
                    String rTime = TextUtils.isEmpty(rhs.DelayFinishTime) ? rhs.PredictFinishTime : rhs.DelayFinishTime;

                    //计算完成时间
                    lhs.calculateBetTime(lTime);
                    rhs.calculateBetTime(rTime);

                    //计算距离
                    lhs.calculateDistance(xy);
                    rhs.calculateDistance(xy);

                    if (selectedSortItem == R.id.tvMiddle) {//距离由近及远排序
                        return lhs.intervalDistance >= rhs.intervalDistance ? 1 : -1;
                    } else if (selectedSortItem == R.id.tvRight) {//完成时间由近及远排序
                        return rhs.intervalTime >= lhs.intervalTime ? 1 : -1;//以超期时间倒序排列，将最急切需要完成的优先显示
                    } else {//派单时间由近及远排序
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

                        long lt = format.parse(lhs.DispatchTime).getTime();
                        long rt = format.parse(rhs.DispatchTime).getTime();

                        return lt > rt ? -1 : 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return 0;
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        try {
            super.onHiddenChanged(hidden);

            if (!hidden) {//列表重新被显示的时候，手动设置焦点，因为返回的时候，焦点会被重置为第一个
                getView().findViewById(selectedSortItem).requestFocus();

                MyCaseActivity activity = (MyCaseActivity) getActivity();

                if (activity.shouldRefresh.contains("退单,")) {//存在退单情况，本地删除内存退单对象
                    this.data.remove(activity.selectedItem);
                    this.sourceItems.remove(activity.selectedItem);

                    adapter.notifyDataSetChanged();
                } else if (activity.shouldRefresh.contains("完工,")) {//存在已完工的情况，需要重新排序
                    sortCaseList();
                } else if (activity.shouldRefresh.length() > 0) {//存在其他的变更操作情况，只需要刷新数据，无需排序
                    adapter.notifyDataSetChanged();
                }

                activity.shouldRefresh = "";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.loadingDialog.dismiss();

        this.data = null;
        this.sourceItems = null;
    }
}
