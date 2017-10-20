package com.repair.allcase.list;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.maintainproduct.entity.ResultDataWC;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.DateUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.fragment.BackHandledFragment;
import com.mapgis.mmt.entity.Nullable;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;
import com.repair.allcase.AllCaseActivity;
import com.repair.common.CaseItem;
import com.repair.common.CaseSearchActivity;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.entity.CaseCondition;
import com.repair.entity.ConditionParameter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CaseListFragment extends BackHandledFragment implements View.OnTouchListener, View.OnClickListener, AdapterView.OnItemClickListener {
    private List<CaseItem> items = new ArrayList<>();
    private ProgressDialog loadingDialog;

    private PullToRefreshListView listView;
    public CaseListAdapter adapter;
    public ConditionParameter parameter = new ConditionParameter();
    private int selectedSortItem;
    private int pageIndex = 1;
    private int total = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.loadingDialog = MmtProgressDialog.getLoadingProgressDialog(getActivity(), "正在处理,请稍候...");

        View view = inflater.inflate(R.layout.wx_all_case_list, container, false);

        view.findViewById(R.id.layoutBottomBar).setVisibility(View.GONE);

        listView = (PullToRefreshListView) view.findViewById(R.id.maintenanceFormList);
        listView.setMode(PullToRefreshBase.Mode.BOTH);

        ListView actualListView = listView.getRefreshableView();

        registerForContextMenu(actualListView);

        adapter = new CaseListAdapter((BaseActivity) getActivity(), items);
        actualListView.setAdapter(adapter);

        return view;
    }

    /**
     * 刷新整个列表，包括下拉刷新、搜索条件变更（搜索框输入和筛选框选择）的再查询
     */
    public void refreshAll(boolean showLoading) {
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

        // 更新下拉面板
        listView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel("最后更新时间：" + label);

        parameter.pageIndex = 1;

        // 执行更新任务,结束后刷新界面
        new FetchListTask(getActivity(), showLoading) {
            @Override
            protected void onPostExecute(ResultDataWC<CaseItem> result) {
                try {
                    if (getActivity() == null || getActivity().isFinishing())
                        return;

                    listView.onRefreshComplete();

                    if (result == null) {
                        Toast.makeText(getActivity(), "查询数据失败", Toast.LENGTH_SHORT).show();

                        return;
                    }

                    if (!TextUtils.isEmpty(result.say.errMsg)) {
                        Toast.makeText(getActivity(), result.say.errMsg, Toast.LENGTH_SHORT).show();

                        return;
                    }

                    items.clear();
                    items.addAll(result.getMe);

                    pageIndex = 1;
                    total = result.totalRcdNum;

                    if (total > 0)
                        Toast.makeText(getActivity(), "总计查询到" + total + "条记录", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "未查询到任何记录", Toast.LENGTH_SHORT).show();

                    showLabelForMore();

                    adapter.notifyDataSetChanged();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    super.onPostExecute(result);
                }
            }
        }.mmtExecute(parameter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.btnBack).setOnClickListener(this);

        // 每项的点击事件
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                OnItemClick((int) arg3, "list");
            }
        });

        listView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        listView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        listView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");
        // 上拉加载更多时的提示文本设置
        listView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载");
        listView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        listView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");

        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                refreshAll(false);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (pageIndex * 10 >= total) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listView.onRefreshComplete();
                            Toast.makeText(getActivity(), "没有更多数据！", Toast.LENGTH_SHORT).show();
                        }
                    });

                    return;
                }

                parameter.pageIndex = pageIndex + 1;

                // 执行更新任务,结束后刷新界面
                new FetchListTask(getActivity(), false) {
                    @Override
                    protected void onPostExecute(ResultDataWC<CaseItem> result) {
                        try {
                            if (getActivity() == null || getActivity().isFinishing())
                                return;

                            listView.onRefreshComplete();

                            if (result == null) {
                                Toast.makeText(getActivity(), "查询数据失败", Toast.LENGTH_SHORT).show();

                                return;
                            }

                            if (!TextUtils.isEmpty(result.say.errMsg)) {
                                Toast.makeText(getActivity(), result.say.errMsg, Toast.LENGTH_SHORT).show();

                                return;
                            }

                            pageIndex++;
                            items.addAll(result.getMe);

                            showLabelForMore();

                            adapter.notifyDataSetChanged();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            super.onPostExecute(result);
                        }
                    }
                }.mmtExecute(parameter);
            }

        });

        selectedSortItem = R.id.tvLeft;

        view.findViewById(R.id.tvLeft).setOnTouchListener(this);
        view.findViewById(R.id.tvMiddle).setOnTouchListener(this);
        view.findViewById(R.id.tvRight).setOnTouchListener(this);
        view.findViewById(R.id.txtSearch).setOnClickListener(this);

        listView.setRefreshing(false);

        initSearchCondition(view);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.txtSearch) {
            Intent intent = new Intent(getActivity(), CaseSearchActivity.class);

            intent.putExtra("key", parameter.reportInfo);

            startActivityForResult(intent, 1);
        } else if (v.getId() == R.id.btnBack) {
            getActivity().onBackPressed();
        } else if (v.getId() == R.id.btnFilter) {
            if (drawerLayout.isDrawerOpen(getView().findViewById(R.id.layoutOne)))
                drawerLayout.closeDrawer(getView().findViewById(R.id.layoutOne));
            else
                drawerLayout.openDrawer(getView().findViewById(R.id.layoutOne));
        } else if (v.getId() == R.id.tvClearOne) {
            buildData();
        } else if (v.getId() == R.id.ivBack) {
            drawerLayout.closeDrawer(getView().findViewById(R.id.layoutTwo));
        } else if (v.getId() == R.id.tvOkForTwo) {
            drawerLayout.closeDrawer(getView().findViewById(R.id.layoutTwo));
        } else if (v.getId() == R.id.btnCancel) {
            drawerLayout.closeDrawer(getView().findViewById(R.id.layoutOne));
        } else if (v.getId() == R.id.btnOk) {
            for (LevelItem item : data) {
                String values = item.getCheckedValues();

                values = values.equals("全部") ? "" : values;

                switch (item.name) {
                    case "派单时间":
                        String[] dates = DateUtil.getDateSpanString(values);

                        parameter.dispatchDateFrom = dates[0];
                        parameter.dispatchDateTo = dates[1];
                        break;
                    case "工单状态":
                        parameter.state = values;
                        break;
                    case "事件类型":
                        parameter.eventType = values;
                        break;
                    case "事件内容":
                        parameter.eventClass = values;
                        break;
                    case "维修人员":
                        parameter.dispatchMan = values;
                        break;
                }
            }

            drawerLayout.closeDrawer(getView().findViewById(R.id.layoutOne));

            refreshAll(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode != 1 || resultCode != 1)
            return;

        String key = intent.getStringExtra("key");//返回空代表查全部

        ((EditText) getView().findViewById(R.id.txtSearch)).setText(key);

        parameter.reportInfo = key;

        refreshAll(true);
    }

    private void showLabelForMore() {
        if (pageIndex * 10 < total) {
            int mt = (pageIndex + 1) * 10;

            String label = (mt > total ? total : mt) + "/" + total;

            listView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);
        } else {
            listView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel("全部加载完成:" + total + "条");
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
        if (items == null || items.size() == 0)
            return;

        new MmtBaseTask<String, Integer, String>(getActivity()) {
            @Override
            protected String doInBackground(String... params) {
                sort(items);

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

        CaseItem caseItem = items.get(pos);

        adapter.onDetailClick(caseItem, from);
    }

    private void sort(List<CaseItem> items) {
        final GpsXYZ xy = GpsReceiver.getInstance().getLastLocalLocation();

        Collections.sort(items, new Comparator<CaseItem>() {

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

                AllCaseActivity activity = (AllCaseActivity) getActivity();

                if (activity.shouldRefresh.contains("退单,")) {//存在退单情况，本地删除内存退单对象
                    this.items.remove(activity.selectedItem);

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
        this.items = null;
    }

    private DrawerLayout drawerLayout;
    private LevelOneAdapt oneAdapt;
    private ListView lvTwo;

    private Handler handler = new Handler();

    private static CaseCondition condition;
    private ArrayList<LevelItem> data;
    private String[][] events;

    public void initSearchCondition(View view) {
        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawerLayout);
        drawerLayout.setFocusableInTouchMode(false);

        drawerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    if (event.getAction() != MotionEvent.ACTION_UP)
                        return false;

                    boolean isOneOpen = drawerLayout.isDrawerOpen(getView().findViewById(R.id.layoutOne));
                    boolean isTwoOpen = drawerLayout.isDrawerOpen(getView().findViewById(R.id.layoutTwo));

                    if (!(isOneOpen || isTwoOpen))
                        return false;

                    if (event.getX() < drawerLayout.getChildAt(1).getX()) {
                        if (isTwoOpen)
                            drawerLayout.closeDrawer(getView().findViewById(R.id.layoutTwo));
                        else
                            drawerLayout.closeDrawer(getView().findViewById(R.id.layoutOne));

                        return true;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return false;
            }
        });

        drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                synchronized (CaseListFragment.this) {
                    if (data == null) {
                        if (condition != null) {
                            buildData();
                        } else {
                            new FetchConditionTask(getActivity()) {
                                @Override
                                protected void onPostExecute(ResultData<CaseCondition> data) {
                                    super.onPostExecute(data);

                                    if (data == null) {
                                        Toast.makeText(getActivity(), "获取查询条件失败", Toast.LENGTH_SHORT).show();

                                        return;
                                    }

                                    if (data.ResultCode < 0) {
                                        Toast.makeText(getActivity(), data.ResultMessage, Toast.LENGTH_SHORT).show();

                                        return;
                                    }

                                    condition = data.getSingleData();
                                    buildData();
                                }
                            }.mmtExecute();
                        }
                    }
                }
            }
        });

        lvTwo = (ListView) view.findViewById(R.id.lvTwo);

        view.findViewById(R.id.layoutOne).setOnClickListener(this);
        view.findViewById(R.id.layoutTwo).setOnClickListener(this);
        view.findViewById(R.id.btnFilter).setOnClickListener(this);
        view.findViewById(R.id.tvClearOne).setOnClickListener(this);
        view.findViewById(R.id.ivBack).setOnClickListener(this);
        view.findViewById(R.id.tvOkForTwo).setOnClickListener(this);
        view.findViewById(R.id.btnCancel).setOnClickListener(this);
        view.findViewById(R.id.btnOk).setOnClickListener(this);
    }

    @Override
    public boolean onBackPressed() {
        if (drawerLayout.isDrawerOpen(getView().findViewById(R.id.layoutTwo))) {
            drawerLayout.closeDrawer(getView().findViewById(R.id.layoutTwo));
        } else if (drawerLayout.isDrawerOpen(getView().findViewById(R.id.layoutOne))) {
            drawerLayout.closeDrawer(getView().findViewById(R.id.layoutOne));
        } else
            return false;

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LevelItem item = data.get(position);

        ((TextView) getView().findViewById(R.id.tvTitleForTwo)).setText(item.name);

        if (item.isSingle)
            getView().findViewById(R.id.tvOkForTwo).setVisibility(View.GONE);
        else
            getView().findViewById(R.id.tvOkForTwo).setVisibility(View.VISIBLE);

        final LevelTwoAdapt adapter = new LevelTwoAdapt(getActivity(), item) {
            @Override
            public void afterItemClick(boolean needRefresh) {
                if (item.isSingle) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawerLayout.closeDrawer(CaseListFragment.this.getView().findViewById(R.id.layoutTwo));
                        }
                    }, 200);
                }

                if (needRefresh) {
                    if (item.name.equals(data.get(2).name)) {
                        int index = data.get(2).children.indexOf(data.get(2).getCheckedItem());

                        data.get(3).setChildren(events[index >= 0 ? index : 0]);
                    }

                    oneAdapt.notifyDataSetChanged();
                }
            }
        };

        lvTwo.setAdapter(adapter);
        lvTwo.setOnItemClickListener(adapter);

        drawerLayout.openDrawer(getView().findViewById(R.id.layoutTwo));
    }

    static class LevelOneAdapt extends BaseAdapter {
        Context context;
        ArrayList<LevelItem> data;

        public LevelOneAdapt(Context context, ArrayList<LevelItem> data) {
            this.context = context;

            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            List<LevelItem> items = data.get(position).children;

            if (items == null || items.size() == 0)
                return "全部";
            else {
                ArrayList<String> names = new ArrayList<>();

                for (LevelItem li : items) {
                    if (li.isChecked)
                        names.add(li.name);
                }

                return TextUtils.join(",", names);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.one_item, parent, false);

            ((TextView) convertView.findViewById(R.id.tvName)).setText(data.get(position).name);

            String value = getItem(position);

            ((TextView) convertView.findViewById(R.id.tvOper)).setText(value);
            ((TextView) convertView.findViewById(R.id.tvOper)).setTextColor(value.equals("全部") ? Color.BLACK : Color.RED);

            return convertView;
        }
    }

    static abstract class LevelTwoAdapt extends BaseAdapter implements AdapterView.OnItemClickListener {
        Context context;
        LevelItem item;

        public LevelTwoAdapt(Context context, LevelItem item) {
            this.context = context;
            this.item = item;
        }

        @Override
        public int getCount() {
            return this.item.children.size();
        }

        @Override
        public LevelItem getItem(int position) {
            return this.item.children.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(-1, DimenTool.dip2px(context, 50));

            TextView tv = new TextView(context);

            tv.setLayoutParams(params);

            tv.setGravity(Gravity.CENTER_VERTICAL);

            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);

            LevelItem child = getItem(position);

            tv.setText(child.name);

            if (child.isChecked) {
                tv.setTextColor(Color.RED);
                tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.order_pickup_butn_seleted_icon, 0);
            } else {
                tv.setTextColor(Color.BLACK);
                tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            return tv;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LevelItem li = getItem(position);

            if (item.isSingle && li.isChecked) {
                afterItemClick(false);

                return;
            }

            if (!li.isChecked) {
                ((TextView) view).setTextColor(Color.RED);
                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.order_pickup_butn_seleted_icon, 0);

                item.setCheckedItem(getItem(position).name);
            } else {
                ((TextView) view).setTextColor(Color.BLACK);
                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                li.isChecked = false;

                if (item.getCheckedItem().isNull())
                    item.setCheckedItem("全部");
            }

            this.notifyDataSetChanged();

            afterItemClick(true);
        }

        public abstract void afterItemClick(boolean needRefresh);
    }

    private void buildData() {
        events = condition.EventContents;

        data = new ArrayList<>();

        data.add(new LevelItem("派单时间", condition.DispatchTimes));
        data.add(new LevelItem("工单状态", condition.CaseStates));
        data.add(new LevelItem("事件类型", condition.EventTypes));

        data.add(new LevelItem("事件内容", events[0]));

        data.add(new LevelItem("维修人员", condition.Repairers));

        oneAdapt = new LevelOneAdapt(getActivity(), data);

        ListView lvOne = (ListView) getView().findViewById(R.id.lvOne);

        lvOne.setAdapter(oneAdapt);
        lvOne.setOnItemClickListener(this);
    }

    static class LevelItem implements Nullable {
        public boolean isSingle;
        public String name;
        public boolean isChecked;

        public ArrayList<LevelItem> children;

        public LevelItem() {
            this.isSingle = true;
            this.name = "全部";
            this.children = new ArrayList<>();
        }

        public LevelItem(String name, String... children) {
            this();

            this.name = name;
            this.isChecked = name.equals("全部");//默认选中全部

            if (children == null || children.length == 0)
                return;

            for (String c : children) {
                this.children.add(new LevelItem(c));
            }
        }

        public void setChildren(String... children) {
            this.children = new ArrayList<>();

            for (String c : children) {
                this.children.add(new LevelItem(c));
            }
        }

        @Override
        public String toString() {
            String result = "{" + name + ":";

            for (LevelItem li : children) {
                if (!li.isChecked)
                    continue;

                result += li.name + ",";
            }

            if (result.endsWith(",")) {
                result = result.substring(0, result.length() - 1);
            } else {
                result = result + "全部";
            }

            return result + "}";
        }

        public LevelItem getCheckedItem() {
            for (LevelItem li : children) {
                if (li.isChecked)
                    return li;
            }

            return new NullLevelItem();
        }

        public String getCheckedValues() {
            if (this.children != null && this.children.size() > 0) {
                ArrayList<String> names = new ArrayList<>();

                for (LevelItem li : this.children) {
                    if (li.isChecked)
                        names.add(li.name);
                }

                if (names.size() > 0)
                    return TextUtils.join(",", names);
            }

            return "全部";
        }

        public void setCheckedItem(String name) {
            for (LevelItem item : this.children) {
                if (item.name.equals(name)) {
                    item.isChecked = true;
                } else if (isSingle || name.equals("全部") || item.name.equals("全部")) {
                    item.isChecked = false;
                }
            }
        }

        public boolean isNull() {
            return false;
        }

        private class NullLevelItem extends LevelItem {
            public NullLevelItem() {
                super();
            }

            @Override
            public boolean isNull() {
                return true;
            }
        }
    }
}
