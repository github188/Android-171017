package com.repair.zhoushan.module.flownodecommonhand;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.CaseItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/8/26.
 */
public class PojListFragment extends Fragment {

    String flowName;
    String nodeName;

    String bizName;
    String tableName;
    BaseActivity mContext;
    List<CaseItem> eventItemList = new ArrayList<>();


    private PullToRefreshListView mPullRefreshListView;
    PojListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Bundle bundle=getArguments();
        flowName=bundle.getString("flowName");
        nodeName=bundle.getString("nodeName");

        bizName=bundle.getString("bizName");
        tableName=bundle.getString("tableName");

        View view = inflater.inflate(R.layout.list_view, container,
                false);

        mContext = (BaseActivity) getActivity();


        mPullRefreshListView = (PullToRefreshListView) view
                .findViewById(R.id.listView);

        adapter = new PojListAdapter(mContext, eventItemList, bizName,tableName);
        mPullRefreshListView.setAdapter(adapter);

        // 下拉刷新 文本提示
        mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel(
                "下拉刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false)
                .setRefreshingLabel("正在刷新...");
        mPullRefreshListView.getLoadingLayoutProxy(true, false)
                .setReleaseLabel("放开以刷新");
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mPullRefreshListView==null){
            return;
        }

        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetFlowNodePojBox")
                .append("?flowName=").append(flowName)
                .append("&nodeName=").append(nodeName);
        final String url = sb.toString();

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {

                reFetchData(url);
            }
        });
        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.onItemClick(position-1);
            }
        });
        mPullRefreshListView.setRefreshing();
    }

    private void reFetchData(final String url) {
        new MmtBaseTask<Void, Void, String>(mContext, true) {
            @Override
            protected String doInBackground(Void... params) {
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                mPullRefreshListView.onRefreshComplete();

                ResultData<CaseItem> resultData = Utils.json2ResultDataToast(CaseItem.class, mContext, s, "请求服务错误", true);

                if (resultData == null) {
                    return;
                }
                if (resultData.DataList.size() == 0) {
                    ((BaseActivity) getActivity()).showErrorMsg(flowName + "中不存在" + nodeName);
                    return;
                }
                eventItemList.clear();
                eventItemList.addAll(resultData.DataList);

                adapter.notifyDataSetChanged();
            }
        }.mmtExecute();
    }
}
