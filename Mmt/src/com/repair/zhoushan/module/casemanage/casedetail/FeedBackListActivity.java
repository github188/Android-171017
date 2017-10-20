package com.repair.zhoushan.module.casemanage.casedetail;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.ConfigFieldsAdapter;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;
import com.repair.zhoushan.module.devicecare.MaintenanceFeedBack;
import com.repair.zhoushan.module.devicecare.TableColumn;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordActivity;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by liuyunfan on 2016/7/21.
 */
public class FeedBackListActivity extends SimplePagerListActivity {
    private String bizName = "";
    private String caseNo = "";
    private String type = "手持列表字段";

    private String bizTableName = "";

    private ArrayList<MaintenanceFeedBack> maintenanceFeedBacks = null;

    //完整的
    private List<List<TableColumn>> adapterdataList2 = new ArrayList<List<TableColumn>>();

    //排除了ID
    private List<List<TableColumn>> adapterdataList = new ArrayList<List<TableColumn>>();

    private List<String> dataList = new ArrayList<>();

    @Override
    public void init() {

        Intent intent = getIntent();

        bizName = intent.getStringExtra("bizName");

        if (TextUtils.isEmpty(bizName)) {
            showErrorMsg("业务名称异常");
            return;
        }

        caseNo = intent.getStringExtra("caseNo");

        if (TextUtils.isEmpty(caseNo)) {
            showErrorMsg("caseNo异常");
            return;
        }


        mSimplePagerListDelegate = new SimplePagerListDelegate<String>(this, dataList, String.class) {
            @Override
            protected SimpleBaseAdapter generateAdapter() {
                return new ConfigFieldsAdapter(FeedBackListActivity.this, adapterdataList);
            }

            @Override
            protected String generateUrl() {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc")
                        .append("/GetFeedbackTableList")
                        .append("?bizName=").append(bizName)
                        .append("&caseNo=").append(caseNo)
                        .append("&type=").append(type);
                return sb.toString();
            }

            @Override
            protected void loadData() {
                // 执行更新任务,结束后刷新界面
                MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(FeedBackListActivity.this) {
                    @Override
                    protected String doInBackground(String... params) {
                        return NetUtil.executeHttpGet(generateUrl());
                    }

                    @Override
                    protected void onSuccess(String jsonResult) {

                        if (listView == null) {
                            listView = mSimplePagerListDelegate.getmPullRefreshListView();
                        }

                        if (listView != null) {
                            listView.onRefreshComplete();
                        }

                        ResultData<String> result = Utils.json2ResultDataToast(String.class, FeedBackListActivity.this, jsonResult, "请求异常", true);

                        if (result == null) {
                            return;
                        }

                        String data = result.getSingleData();

                        List<List<TableColumn>> adapterdataListtemp = parseResult(data);

                        if (adapterdataListtemp == null) {
                            return;
                        }

                        if (adapterdataListtemp.size() == 0) {
                            if (!getIsLoadMoreMode()) { // Refresh
                                Toast.makeText(FeedBackListActivity.this, "没有记录", Toast.LENGTH_SHORT).show();
                                dataList.clear();
                                adapterdataList.clear();
                                adapter.notifyDataSetChanged();
                            } else {               // LoadMore
                                Toast.makeText(FeedBackListActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                            }
                        } else {

                            if (getIsLoadMoreMode()) {
                                int curIdx = getCurrentPageIndex();
                                setCurrentPageIndex(curIdx + 1);
                            } else {
                                Toast.makeText(FeedBackListActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                                dataList.clear();
                                adapterdataList.clear();
                            }

                            dataList.add(data);

                            adapterdataList.addAll(adapterdataListtemp);

                            adapter.notifyDataSetChanged();
                        }

                    }
                };
                mmtBaseTask.setCancellable(false);
                mmtBaseTask.mmtExecute();
            }
        };

        getBizTable();
    }

    PullToRefreshListView listView;

    @Override
    protected void afterViewCreated() {
        super.afterViewCreated();

        listView = mSimplePagerListDelegate.getmPullRefreshListView();
        if (listView != null) {
            listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        }

        addBottomBtn();
        // 每项的点击事件
        mSimplePagerListDelegate.getmPullRefreshListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                List<TableColumn> tcs = adapterdataList2.get(arg2 - 1);

                String ID = "";
                for (TableColumn tc : tcs) {
                    if (tc.FieldName.equalsIgnoreCase("ID")) {
                        ID = tc.FieldValue;
                        break;
                    }
                }

                if (TextUtils.isEmpty(ID)) {
                    showErrorMsg("ID不存在无法查看");
                    return;
                }

                if (TextUtils.isEmpty(bizTableName)) {
                    showErrorMsg("未获取到反馈表名，请稍候再试或检查配置");
                    return;
                }

                Intent intent = new Intent(FeedBackListActivity.this, TableOneRecordActivity.class);
                intent.putExtra("tableName", bizTableName);
                intent.putExtra("ID", Integer.valueOf(ID));
                intent.putExtra("viewMode", TabltViewMode.DELETE.getTableViewMode());

                if (maintenanceFeedBacks != null && maintenanceFeedBacks.size() > 0) {
                    intent.putExtra("maintenanceFeedBacks", new Gson().toJson(maintenanceFeedBacks));
                }

                FeedBackListActivity.this.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(FeedBackListActivity.this);
            }
        });


    }

    private void getBizTable() {

        new MmtBaseTask<Void, Void, String>(this) {
            @Override
            protected String doInBackground(Void... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetMaintenanceFBConfigList?BizName=" + bizName;

                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);

                ResultData<MaintenanceFeedBack> filterResult = Utils.json2ResultDataActivity(MaintenanceFeedBack.class,
                        FeedBackListActivity.this, s, "获取过滤条件失败", false);

                if (filterResult == null) {
                    return;
                }

                if (filterResult.DataList.size() == 0) {
                    return;
                }
                maintenanceFeedBacks = filterResult.DataList;

                bizTableName = filterResult.getSingleData().deviceFBTbl;
            }
        }.mmtExecute();

    }

    /**
     * @param str
     * @return 排除了ID的
     */
    private List<List<TableColumn>> parseResult(String str) {

        List<List<TableColumn>> adapterdataList = new ArrayList<>();

        try {

            if (TextUtils.isEmpty(str)) {
                throw new Exception("");
            }

            str = str.replace("T", " ");

            JSONArray ja = new JSONArray(str);

            for (int i = 0; i < ja.length(); i++) {
                List<TableColumn> tcs2 = new ArrayList<>();
                List<TableColumn> tcs = new ArrayList<>();
                JSONObject jo = (JSONObject) ja.get(i);

                if (jo == null || TextUtils.isEmpty(jo.toString())) {
                    continue;
                }

                Iterator it = jo.keys();
                while (it.hasNext()) {

                    TableColumn tc = new TableColumn();

                    String key = it.next().toString();
                    String val = jo.get(key).toString();


                    tc.FieldName = key;
                    tc.FieldValue = val;

                    if (key.equalsIgnoreCase("ID")) {
                        tcs2.add(tc);
                        continue;
                    }

                    tcs.add(tc);
                }

                adapterdataList.add(tcs);

                adapterdataList2.add(tcs2);

            }

            return adapterdataList;

        } catch (Exception ex) {

            return adapterdataList;

        }

    }

    private void addBottomBtn() {
        View rootView = findViewById(android.R.id.content);

        BottomUnitView backUnitView = new BottomUnitView(this);

        backUnitView.setContent("反馈");
        backUnitView.setImageResource(R.drawable.handoverform_report);
        backUnitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(bizTableName)) {
                    showErrorMsg("未获取到反馈表名，请稍候再试或检查配置");
                    return;
                }

                Intent intent = new Intent(FeedBackListActivity.this, FeebReportActivity.class);
                intent.putExtra("caseno", caseNo);
                intent.putExtra("tableName", bizTableName);
                intent.putExtra("viewMode", TabltViewMode.REPORT.getTableViewMode());
                if (maintenanceFeedBacks != null && maintenanceFeedBacks.size() > 0) {
                    intent.putExtra("maintenanceFeedBacks", new Gson().toJson(maintenanceFeedBacks));
                }

                FeedBackListActivity.this.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(FeedBackListActivity.this);
            }
        });
        RelativeLayout bottomBtn = (RelativeLayout) rootView.findViewById(R.id.bottombtn);
        bottomBtn.setVisibility(View.VISIBLE);
        bottomBtn.addView(backUnitView);

    }
}
