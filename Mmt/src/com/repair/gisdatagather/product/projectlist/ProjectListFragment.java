package com.repair.gisdatagather.product.projectlist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.gisdatagather.common.entity.GISDataProject;
import com.repair.gisdatagather.common.entity.GisProjectWithGisData;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FlowCenterData;
import com.zondy.mapgis.android.mapview.MapView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by liuyunfan on 2016/5/3.
 */
public class ProjectListFragment extends Fragment {
    // 数据显示列表
    private PullToRefreshListView dataListView;
    private int index = 1;
    private final int pageSize = 10;
    ProjectListAdapter adapter;
    BaseActivity context;
    final ArrayList<CaseItem> data = new ArrayList<CaseItem>();
    boolean isdoingBox = false;
    private FlowCenterData flowCenterData;

    //这两个在用配置工具配置的时候需要固定
    public static final String flowName = "管网采集";
    public static final String secendNodeName = "管网采集";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_view, container, false);
        context = (BaseActivity) getActivity();
        Bundle bundle = getArguments();
        isdoingBox = bundle.getBoolean("isdoingBox", false);
        dataListView = (PullToRefreshListView) view.findViewById(R.id.listView);
        dataListView.setMode(PullToRefreshBase.Mode.BOTH);
        String flowCenterDataStr = "";
        if (!TextUtils.isEmpty(flowCenterDataStr = bundle.getString("flowCenterData"))) {
            flowCenterData = new Gson().fromJson(flowCenterDataStr, FlowCenterData.class);
        }
        if (flowCenterData == null) {
            flowCenterData = GisDataGatherUtils.getFlowCenterDataForProduct();
        }
        if (!flowCenterData.FlowName.equals(flowName)) {
            requiredConfig("请将流程名称配置为管网采集！");
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new ProjectListAdapter(data, (BaseActivity) context, isdoingBox);
        dataListView.setAdapter(adapter);

        dataListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        dataListView.getLoadingLayoutProxy(true, false).setRefreshingLabel(
                "正在刷新");
        dataListView.getLoadingLayoutProxy(true, false)
                .setReleaseLabel("放开以刷新");
        // 上拉加载更多时的提示文本设置
        dataListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载");
        dataListView.getLoadingLayoutProxy(false, true).setRefreshingLabel(
                "正在加载...");
        dataListView.getLoadingLayoutProxy(false, true)
                .setReleaseLabel("放开以加载");
        dataListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                String label = DateUtils.formatDateTime(getActivity()
                                .getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);
                dataListView.getLoadingLayoutProxy(true, false)
                        .setLastUpdatedLabel(label);
                dataListView.getLoadingLayoutProxy(false, true)
                        .setLastUpdatedLabel(label);

                index = 1;
                loadData();
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                String label = DateUtils.formatDateTime(getActivity()
                                .getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);
                dataListView.getLoadingLayoutProxy(false, true)
                        .setLastUpdatedLabel(label);
                index++;
                loadData();
            }

        });

        dataListView.setRefreshing(false);

        dataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                try {
                    final CaseItem caseItem = (CaseItem) arg0.getItemAtPosition(arg2);
                    if (caseItem == null) {
                        Toast.makeText(getActivity(), "工程异常",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new MmtBaseTask<String, Void, String>(context) {
                        @Override
                        protected String doInBackground(String... params) {
                            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetGisProject";

                            return NetUtil.executeHttpGet(url, "caseNo", params[0]);
                        }

                        @Override
                        protected void onSuccess(String s) {
                            super.onSuccess(s);
                            ResultData<GisProjectWithGisData> resultData = Utils.json2ResultDataToast(GisProjectWithGisData.class, getActivity(), s, "获取工程已采集的点线错误", true);
                            if (resultData == null) {
                                return;
                            }
                            final GisProjectWithGisData gisProjectWithGisData = resultData.getSingleData();

                            MapGISFrame mapGISFrame=MyApplication.getInstance().mapGISFrame;
                            MapView mapView = mapGISFrame.getMapView();
                            GISDataProject gisDataProject = gisProjectWithGisData.convert2GisDataProject(mapView);
                            gisDataProject.setCaseItem(caseItem);
                            GisDataGatherUtils.openGisGather(mapGISFrame, gisDataProject, !isdoingBox, false);
                        }
                    }.mmtExecute(caseItem.CaseNo);

                    if (isdoingBox && TextUtils.isEmpty(caseItem.ReadCaseTime)) {
                        new MmtBaseTask<Void, Void, String>(context, false) {
                            @Override
                            protected String doInBackground(Void... params) {

                                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/ReadCase";

                                String result = "";
                                try {
                                    result = NetUtil.executeHttpPost(url, new Gson().toJson(caseItem.mapToCaseInfo()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return result;
                            }
                        }.mmtExecute();
                    }
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
        super.onViewCreated(view, savedInstanceState);
    }


    private void requiredConfig(String tip) {
        OkDialogFragment fragment = new OkDialogFragment(tip);
        fragment.setOnButtonClickListener(new OkDialogFragment.OnButtonClickListener() {
            @Override
            public void onButtonClick(View view) {
                AppManager.finishActivity(context);
            }
        });
        fragment.setCancelable(false);
        fragment.show(context.getSupportFragmentManager(), "");
    }

    private void loadData() {
        new MmtBaseTask<String, Void, String>(context, false) {
            @Override
            protected String doInBackground(String... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/" + MyApplication.getInstance().getUserId()
                        + "/StardEventDoingBox?_mid=" + UUID.randomUUID().toString()
                        + "&pageIndex=" + index
                        + "&pageSize=" + pageSize
                        + "&flowName=" + flowName
                        + "&flowNodeName=" + secendNodeName
                        + "&sortFields=ID0&direction=desc";

                if (!isdoingBox) {
                    url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/" + MyApplication.getInstance().getUserId()
                            + "/StardEventDoneBox?_mid=" + UUID.randomUUID().toString()
                            + "&pageIndex=" + index
                            + "&pageSize=" + pageSize
                            + "&flowName=" + flowName
                            + "&sortFields=ID0&direction=desc";
                }
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String jsonResult) {
                ResultData<CaseItem> newData = Utils.json2ResultDataToast(CaseItem.class, context, jsonResult, "获取数据失败", true);
                dataListView.onRefreshComplete();
                if (newData == null) {
                    requiredConfig("服务请求错误，请检查流程名称和第二个节点名称是否为管网采集！");
                    return;
                }
                if (newData.DataList.size() == 0) {
                    if (index == 1) {
                        Toast.makeText(getActivity(), "没有数据",
                                Toast.LENGTH_SHORT).show();
                        data.clear();
                    } else {
                        Toast.makeText(getActivity(), "没有更多数据",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    if (index == 1) {
                        data.clear();
                    }
                    data.addAll(newData.DataList);
                }

                adapter.notifyDataSetChanged();
            }
        }.mmtExecute();
    }
}
