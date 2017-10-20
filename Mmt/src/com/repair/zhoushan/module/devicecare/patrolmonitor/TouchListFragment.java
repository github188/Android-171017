package com.repair.zhoushan.module.devicecare.patrolmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.CrashPointRecord;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordActivity;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/7/6.
 */
public class TouchListFragment extends Fragment {
    private String eventCode;

    protected BaseActivity baseContext;
    protected TouchListAdapter adapter;
    protected PullToRefreshListView listView;
    protected List<CrashPointRecord> dataList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventCode = getArguments().getString("EventCode");
        baseContext = (BaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.supervise_report_list, container, false);
        listView = (PullToRefreshListView) view.findViewById(R.id.superviseReportListView);

        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        adapter = new TouchListAdapter(baseContext, dataList);
        listView.setAdapter(adapter);
        listView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        listView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        listView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");

        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                new MmtBaseTask<Void, Void, String>(baseContext) {
                    @Override
                    protected String doInBackground(Void... params) {
                        String url = ServerConnectConfig.getInstance().getBaseServerPath() + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CrashReplace/GetCrashPointByEventCode?eventCode=" + eventCode;
                        return NetUtil.executeHttpGet(url);
                    }

                    @Override
                    protected void onSuccess(String result) {
                        listView.onRefreshComplete();
                        if (TextUtils.isEmpty(result)) {
                            Toast.makeText(baseContext, "网络错误或服务错误", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Results<CrashPointRecord> crashPointRecordResults = new Gson().fromJson(result, new TypeToken<Results<CrashPointRecord>>() {
                        }.getType());
                        if (crashPointRecordResults == null) {
                            Toast.makeText(baseContext, "服务错误", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!TextUtils.isEmpty(crashPointRecordResults.say.errMsg)) {
                            Toast.makeText(baseContext, crashPointRecordResults.say.errMsg, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        dataList.clear();
                        dataList.addAll(crashPointRecordResults.getMe);
                        adapter.notifyDataSetChanged();
                    }
                }.mmtExecute();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(baseContext, TableOneRecordActivity.class);
                intent.putExtra("tableName", "碰接点记录表");
                intent.putExtra("ID", dataList.get(position - 1).ID);
                intent.putExtra("viewMode", TabltViewMode.READ.getTableViewMode());
                baseContext.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(baseContext);

            }
        });
        listView.setRefreshing(false);
        return view;
    }
}
