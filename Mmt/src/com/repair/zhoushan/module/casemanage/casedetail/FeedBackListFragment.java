package com.repair.zhoushan.module.casemanage.casedetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.ConfigFieldsAdapter;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FeedbackInfo;
import com.repair.zhoushan.module.devicecare.MaintenanceFeedBack;
import com.repair.zhoushan.module.devicecare.TableColumn;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by liuyunfan on 2016/7/22.
 */
public class FeedBackListFragment extends Fragment {
    protected String bizName = "";
    protected String caseNo = "";
    protected String type = "手持列表字段";

    protected boolean isRead = false;

    protected String tableName = "";

    protected ArrayList<MaintenanceFeedBack> maintenanceFeedBacks = null;
    protected FeedbackInfo feedbackInfo;

    //完整的
    protected List<List<TableColumn>> adapterdataList2 = new ArrayList<>();

    //排除了ID
    protected List<List<TableColumn>> adapterdataList = new ArrayList<>();


    protected List<String> dataList = new ArrayList<>();

    protected PullToRefreshListView pullToRefreshListView;
    protected ConfigFieldsAdapter adapter;
    protected BaseActivity baseActivity;
    protected View fullview;

    protected boolean addReportbtn = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        baseActivity = (BaseActivity) getActivity();

        init();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addBottomBtn();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        fullview = inflater.inflate(R.layout.fbeventreport_history_list, container, false);
        pullToRefreshListView = (PullToRefreshListView) fullview.findViewById(R.id.mainFormList);


        pullToRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        pullToRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        pullToRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");

        adapter = new ConfigFieldsAdapter(baseActivity, adapterdataList);
        pullToRefreshListView.setAdapter(adapter);
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);


        pullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<TableColumn> tcs = adapterdataList2.get(position - 1);

                String ID = "";
                for (TableColumn tc : tcs) {
                    if (tc.FieldName.equalsIgnoreCase("ID")) {
                        ID = tc.FieldValue;
                        break;
                    }
                }

                if (TextUtils.isEmpty(ID)) {
                    baseActivity.showErrorMsg("ID不存在无法查看");
                    return;
                }

                if (TextUtils.isEmpty(tableName)) {
                    baseActivity.showErrorMsg("未获取到反馈表名，请稍候再试或检查配置");
                    return;
                }

                Intent intent = new Intent(baseActivity, FeebReportActivity.class);
                intent.putExtra("bizName", bizName);
                intent.putExtra("tableName", tableName);
                intent.putExtra("ID", Integer.valueOf(ID));
                if (!isRead) {
                    intent.putExtra("viewMode", TabltViewMode.EDIT_DELETE.getTableViewMode());
                } else {
                    intent.putExtra("viewMode", TabltViewMode.READ.getTableViewMode());
                }

                if (maintenanceFeedBacks != null && maintenanceFeedBacks.size() > 0) {
                    intent.putExtra("maintenanceFeedBacks", new Gson().toJson(maintenanceFeedBacks));
                }

                baseActivity.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(baseActivity);
            }
        });

        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

                // 更新下拉面板
                String label = DateUtils.formatDateTime(baseActivity, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(label);
                refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                loadData();

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            }
        });


        pullToRefreshListView.setRefreshing(false);

        return fullview;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    public void refeshFragment(FBRefreshFragment fbRefreshFragment) {

        pullToRefreshListView.setRefreshing(false);
    }

    protected void addBottomBtn() {

        if (isRead) {
            return;
        }

        if (!addReportbtn) {
            return;
        }

        BottomUnitView fbUnitView = new BottomUnitView(baseActivity);

        fbUnitView.setContent("反馈");
        fbUnitView.setImageResource(R.drawable.handoverform_report);
        fbUnitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(tableName)) {
                    baseActivity.showErrorMsg("未获取到反馈表名，请稍候再试或检查配置");
                    return;
                }

                Intent intent = new Intent(baseActivity, FeebReportActivity.class);

                intent.putExtra("caseno", caseNo);
                intent.putExtra("tableName", tableName);
                intent.putExtra("bizName", bizName);
                intent.putExtra("viewMode", TabltViewMode.REPORT.getTableViewMode());
                if (maintenanceFeedBacks != null && maintenanceFeedBacks.size() > 0) {
                    intent.putExtra("maintenanceFeedBacks", new Gson().toJson(maintenanceFeedBacks));
                }

                baseActivity.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(baseActivity);
            }
        });

//        LineLinearLayout.LayoutParams params = (LineLinearLayout.LayoutParams) fbUnitView.getLayoutParams();
//
//        int margin = DimenTool.dip2px(baseActivity, 10);
//        params.bottomMargin = margin;
//        params.leftMargin = margin;
//        params.rightMargin = margin;
//
//        params.width = LineLinearLayout.LayoutParams.MATCH_PARENT;
//        params.height = LineLinearLayout.LayoutParams.WRAP_CONTENT;
//        params.gravity= Gravity.BOTTOM;
//        fbUnitView.setLayoutParams(params);
//        fbUnitView.setBackgroundResource(R.drawable.mapview_bottombar_bg);
//        fbUnitView.setMinimumHeight(DimenTool.dip2px(baseActivity, 50));
//
//        ((ViewGroup)fullview).addView(fbUnitView);

        LinearLayout bottomContain = (LinearLayout) fullview.findViewById(R.id.fbbottombtn);
        bottomContain.setVisibility(View.VISIBLE);
        bottomContain.addView(fbUnitView);

//        RelativeLayout.LayoutParams paramsContain = (RelativeLayout.LayoutParams) bottomContain.getLayoutParams();
//        params.bottomMargin = DimenTool.dip2px(baseActivity, 10);
//        bottomContain.setLayoutParams(paramsContain);
    }

    protected void loadData() {
        // 执行更新任务,结束后刷新界面
        MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(baseActivity) {
            @Override
            protected String doInBackground(String... params) {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc")
                        .append("/GetFeedbackTableList")
                        .append("?bizName=").append(bizName)
                        .append("&caseNo=").append(caseNo)
                        .append("&type=").append(type);
                return NetUtil.executeHttpGet(sb.toString());
            }

            @Override
            protected void onSuccess(String jsonResult) {

                pullToRefreshListView.onRefreshComplete();

                ResultData<String> result = Utils.json2ResultDataToast(String.class, baseActivity, jsonResult, "请求异常", true);

                if (result == null) {
                    return;
                }

                String data = result.getSingleData();

                List<List<List<TableColumn>>> adapterdataListtemps = parseResult(data);

                List<List<TableColumn>> adapterdataListtemp = adapterdataListtemps.get(0);

                if (adapterdataListtemp.size() == 0) {
                    Toast.makeText(baseActivity, "没有记录", Toast.LENGTH_SHORT).show();
                    dataList.clear();
                    adapterdataList.clear();
                    adapterdataList2.clear();
                    adapter.notifyDataSetChanged();
                } else {

                    Toast.makeText(baseActivity, "刷新成功", Toast.LENGTH_SHORT).show();
                    dataList.clear();
                    adapterdataList.clear();
                    adapterdataList2.clear();
                    dataList.add(data);

                    adapterdataList.addAll(adapterdataListtemp);
                    adapterdataList2.addAll(adapterdataListtemps.get(1));

                    adapter.notifyDataSetChanged();
                }

            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    /**
     * @param str json串
     * @return 第0个排除了ID，第1个未排除ID
     */
    private List<List<List<TableColumn>>> parseResult(String str) {

        List<List<List<TableColumn>>> ret = new ArrayList<>();

        List<List<TableColumn>> adapterdataList = new ArrayList<>();
        List<List<TableColumn>> adapterdataList2 = new ArrayList<>();

        ret.add(adapterdataList);
        ret.add(adapterdataList2);
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

            return ret;

        } catch (Exception ex) {

            return ret;

        }

    }

    protected void getBizTable() {

        new MmtBaseTask<Void, Void, String>(baseActivity) {
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
                        baseActivity, s, "获取过滤条件失败", false);

                if (filterResult == null) {
                    return;
                }

                if (filterResult.DataList.size() == 0) {
                    return;
                }
                maintenanceFeedBacks = filterResult.DataList;

            }
        }.mmtExecute();

    }


    protected void init() {

        Bundle bundle = getArguments();
        addReportbtn = bundle.getBoolean("addReportbtn", false);

        bizName = bundle.getString("bizName");

        if (TextUtils.isEmpty(bizName)) {
            baseActivity.showErrorMsg("业务名称异常");
            return;
        }

        caseNo = bundle.getString("caseNo");

        if (TextUtils.isEmpty(caseNo)) {
            baseActivity.showErrorMsg("caseNo异常");
            return;
        }

        tableName = bundle.getString("tableName");

        if (TextUtils.isEmpty(tableName)) {
            baseActivity.showErrorMsg("反馈表名为空");
            return;
        }

        isRead = bundle.getBoolean("isRead", false);
        String feedbackInfoStr = bundle.getString("feedbackInfo");

        if (!TextUtils.isEmpty(feedbackInfoStr)) {
            feedbackInfo = new Gson().fromJson(feedbackInfoStr, FeedbackInfo.class);
            maintenanceFeedBacks = new ArrayList<>();
            maintenanceFeedBacks.add(feedbackInfo.feedbackInfo2MaintenanceFeedBack());
        }

        if (maintenanceFeedBacks == null) {
            getBizTable();
        }

    }

    public static class FBRefreshFragment {
    }
}
