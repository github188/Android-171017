package com.repair.zhoushan.module.devicecare.platfromadd;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.filtermenubar.FilterMenuBar;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.repair.common.CaseSearchActivity;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.ConfigFieldsAdapter;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.TableColumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyunfan on 2016/7/18.
 */
public class PlatfromListActivity extends SimplePagerListActivity {

    private final String STATE_CONDITION = "状态";
    private final String AREA_CONDITION = "区域";

    private String bizName = "";
    private String area = "";
    private String planStatus = "启用";

    private String dateFrom = "";
    private String dateTo = "";
    private String taskInfo = "";


    private ArrayList<List<TableColumn>> adapterdataList = new ArrayList<List<TableColumn>>();

    private ArrayList<ScheduleTask> scheduleTasks = new ArrayList<>();


    @Override
    public void init() {

        if (!initParams()) {

            return;
        }

        mSimplePagerListDelegate = new SimplePagerListDelegate<ScheduleTask>(PlatfromListActivity.this, scheduleTasks, ScheduleTask.class) {
            @Override
            protected SimpleBaseAdapter generateAdapter() {

                return new ConfigFieldsAdapter(PlatfromListActivity.this, adapterdataList);
            }

            @Override
            protected String generateUrl() {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                        .append(MyApplication.getInstance().getUserId() + "")
                        .append("/ScheduleList")
                        .append("?pageSize=").append(getPageSize())
                        .append("&pageIndex=").append(getLoadPageIndex())
                        .append("&sortFields=ID&direction=desc")
                        .append("&bizName=").append(bizName)
                        .append("&area=").append(area)
                        .append("&planStatus=").append(planStatus)
                        .append("&dateFrom=").append(dateFrom)
                        .append("&dateTo=").append(dateTo)
                        .append("&taskInfo=").append(taskInfo);
                return sb.toString();
            }

            @Override
            protected void initRootView() {
                setContentView(R.layout.platfrom_list_activity_xa);
            }

            @Override
            protected void adapterAddData(ResultData<ScheduleTask> newData) {

                if (newData == null) {
                    return;
                }

                if (newData.DataList.size() == 0) {
                    if (!getIsLoadMoreMode()) { // Refresh
                        Toast.makeText(PlatfromListActivity.this, "没有记录", Toast.LENGTH_SHORT).show();
                        scheduleTasks.clear();
                        adapterdataList.clear();
                        adapter.notifyDataSetChanged();
                    } else {               // LoadMore
                        Toast.makeText(PlatfromListActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    if (getIsLoadMoreMode()) {
                        int curIdx = getCurrentPageIndex();
                        setCurrentPageIndex(curIdx + 1);
                    } else {
                        Toast.makeText(PlatfromListActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                        scheduleTasks.clear();
                        adapterdataList.clear();
                    }
                    scheduleTasks.addAll(newData.DataList);

                    for (ScheduleTask scheduleTask : newData.DataList) {
                        adapterdataList.add(scheduleTask.WebRow);
                    }

                    adapter.notifyDataSetChanged();
                }

            }
        };
    }


    private void initMenuBarView() {

        String[] titles = menulist.toArray(new String[menulist.size()]);

        if (titles == null || titles.length == 0) {
            return;
        }

        String[] defValueArr = defValues.toArray(new String[defValues.size()]);

        String[][] contents = new String[menuValList.size()][];

        for (int i = 0; i < menuValList.size(); i++) {
            List<String> vals = menuValList.get(i);
            contents[i] = vals.toArray(new String[vals.size()]);
        }

        FilterMenuBar mFilterMenuBar = (FilterMenuBar) findViewById(R.id.mMenuBar);
        mFilterMenuBar.setMenuItems(titles,
                contents,
                defValueArr);

        mFilterMenuBar.setOnMenuItemSelectedListener(new FilterMenuBar.OnMenuItemSelectedListener() {
                                                         @Override
                                                         public void onItemSelected(Map<String, String> selectResult) {

                                                             if (selectResult.containsKey(STATE_CONDITION)) {
                                                                 String planStatusCur = selectResult.get(STATE_CONDITION);

                                                                 if ("全部".equals(planStatusCur)) {
                                                                     planStatusCur = "";
                                                                 }

                                                                 planStatus = planStatusCur;

                                                             }
                                                             if (selectResult.containsKey(AREA_CONDITION)) {
                                                                 String areaCur = selectResult.get(AREA_CONDITION);

                                                                 if ("全部".equals(areaCur)) {
                                                                     areaCur = "";
                                                                 }

                                                                 area = areaCur;
                                                             }

                                                             refreshData();
                                                         }
                                                     }

        );
    }

    private void initMenuBar() {

        //状态数据
        menulist.add(STATE_CONDITION);
        menuValList.add(Arrays.asList("全部", "启用", "停用"));
        defValues.add("启用");

        //区域数据
        new MmtBaseTask<Void, Void, String>(this) {
            @Override
            protected String doInBackground(Void... params) {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                        .append("GetMaintenceConfig")
                        .append("?bizName=").append(bizName);

                return NetUtil.executeHttpGet(sb.toString());

            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);

                ResultData<MaintanceConfig> maintanceConfig = Utils.json2ResultDataToast(MaintanceConfig.class, context, s, "GetMaintenceConfig服务调用错误", true);

                if (maintanceConfig == null) {
                    return;
                }

                if (maintanceConfig.getSingleData().IsAreaShow == 1) {
                    initArea();
                } else {
                    initMenuBarView();
                }
            }
        }.mmtExecute();


    }

    private void refreshData() {
        PullToRefreshListView listView = mSimplePagerListDelegate.getmPullRefreshListView();
        PullToRefreshBase.OnRefreshListener2<ListView> listener = mSimplePagerListDelegate.getmOnRefreshListener2();
        if (listener != null && listView != null) {
            listener.onPullDownToRefresh(listView);
        }
    }

    List<String> menulist = new ArrayList<>();

    List<List<String>> menuValList = new ArrayList<List<String>>();

    List<String> defValues = new ArrayList<>();

    @Override
    protected void afterViewCreated() {
        super.afterViewCreated();

        initMenuBar();

        initSearchBar();

        addBottomBtn();

        // 每项的点击事件
        mSimplePagerListDelegate.getmPullRefreshListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ScheduleTask scheduleTask = scheduleTasks.get(arg2 - 1);

                Intent intent = new Intent(PlatfromListActivity.this, PlatfromDetailActivity.class);
                intent.putExtra("mode", "view");
                intent.putExtra("bizName", bizName);
                intent.putExtra("GisCode", scheduleTask.GisCode);
                intent.putExtra("ID", scheduleTask.ID);
                PlatfromListActivity.this.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(PlatfromListActivity.this);
            }
        });
    }

    private void addBottomBtn() {
        //添加底部按钮
        View rootView = findViewById(android.R.id.content);

        BottomUnitView backUnitView = new BottomUnitView(this);

        backUnitView.setContent("新增台账");
        backUnitView.setImageResource(R.drawable.handoverform_report);
        backUnitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlatfromListActivity.this, PlatfromDetailActivity.class);
                intent.putExtra("mode", "add");
                intent.putExtra("bizName", bizName);
                intent.putExtra("Area",area);
                PlatfromListActivity.this.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(PlatfromListActivity.this);
            }
        });
        RelativeLayout bottomBtn = (RelativeLayout) rootView.findViewById(R.id.bottombtn);
        bottomBtn.setVisibility(View.VISIBLE);
        bottomBtn.addView(backUnitView);

    }

    EditText txtSearch;

    private void initSearchBar() {
        txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.setHint("GIS编号、位置");
        txtSearch.setVisibility(View.VISIBLE);
        txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(PlatfromListActivity.this, CaseSearchActivity.class);

                // key 为搜索框的当前值
                intent.putExtra("key", txtSearch.getText().toString());
                intent.putExtra("searchHint", "GIS编号、位置");
                intent.putExtra("searchHistoryKey", "CaseListSearchHistory_CaseOverview");

                startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCustomBack();
            }
        });
        findViewById(R.id.btnOtherAction).setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1 && requestCode == Constants.SEARCH_REQUEST_CODE) {
            // 搜索界面返回的搜索关键字
            taskInfo = data.getStringExtra("key"); //返回空代表查全部
            txtSearch.setText(taskInfo);

            refreshData();
        }
    }


    private void initArea() {
        new MmtBaseTask<Void, Void, String>(this) {
            @Override
            protected String doInBackground(Void... params) {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                        .append(String.valueOf(MyApplication.getInstance().getUserId()))
                        .append("/BizAreaName")
                        .append("?bizName=").append(bizName);

                return NetUtil.executeHttpGet(sb.toString());
            }

            @Override
            protected void onSuccess(String s) {

                ResultData<String> resultData = Utils.json2ResultDataToast(String.class, context, s, "区域获取错误", true);

                if (resultData == null) {
                    return;
                }

                menulist.add(AREA_CONDITION);
                List<String> areas = new ArrayList<String>();
                areas.add("全部");
                areas.addAll(resultData.DataList);

                menuValList.add(areas);

                defValues.add("全部");

                initMenuBarView();

            }
        }.mmtExecute();


    }

    private boolean initParams() {

        Intent intent = getIntent();

        bizName = intent.getStringExtra("bizName");

        if (TextUtils.isEmpty(bizName)) {
            showErrorMsg("台账类型错误");
            return false;
        }

        return true;
    }

}
