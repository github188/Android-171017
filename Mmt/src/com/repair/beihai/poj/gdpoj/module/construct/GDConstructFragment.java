package com.repair.beihai.poj.gdpoj.module.construct;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.repair.beihai.poj.gdpoj.entity.GDConstructEvent;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/8/26.
 */
public class GDConstructFragment extends Fragment {

    String flowName = "";
    String bizType = "";
    BaseActivity mContext;
    List<GDConstructEvent> eventItemList = new ArrayList<>();


    private PullToRefreshListView mPullRefreshListView;
    GDConstructEventAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.list_view, container,
                false);

        flowName = getArguments().getString("flowName");

        if (TextUtils.isEmpty(flowName)) {
            return view;
        }

        //默认表名和业务名（管道监管）

        String tableName = "管道工程施工监管表";

        if ("管道工程招标流程".equals(flowName)) {
            bizType = "管道招标施工监管";
        }
        if ("管道工程自建流程".equals(flowName)) {
            bizType = "管道自建施工监管";
        }

        String params = getArguments().getString("params");
        if (!TextUtils.isEmpty(params)) {
            String[] paramArr = params.split("\\|");
            if (paramArr.length != 2) {
                MyApplication.getInstance().showMessageWithHandle("手持上模块参数配置错误");
                return view;
            }
            //管道自建施工过程,管道招标施工过程|管道工程施工过程表
            tableName = paramArr[1];
            String[] bizs = paramArr[0].split(",");

            if ("管道工程招标流程".equals(flowName)) {
                for (String biz : bizs) {
                    if (biz.contains("招标")) {
                        bizType = biz;
                        break;
                    }
                }
            }
            if ("管道工程自建流程".equals(flowName)) {
                for (String biz : bizs) {
                    if (biz.contains("自建")) {
                        bizType = biz;
                        break;
                    }
                }
            }
        }

        if (TextUtils.isEmpty(bizType)) {
            MyApplication.getInstance().showMessageWithHandle("手持上模块参数配置错误");
            return view;
        }
        if (TextUtils.isEmpty(tableName)) {
            MyApplication.getInstance().showMessageWithHandle("手持上模块参数配置错误");
            return view;
        }

        mContext = (BaseActivity) getActivity();


        mPullRefreshListView = (PullToRefreshListView) view
                .findViewById(R.id.listView);

        adapter = new GDConstructEventAdapter(mContext, eventItemList, bizType, tableName);
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

        if (mPullRefreshListView == null) {
            return;
        }

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {

                reFetchData("");
            }
        });
        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.onItemClick(position - 1);
            }
        });
        mPullRefreshListView.setRefreshing();
    }

    public void reFetchData(final String keyWord) {
        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/Services/MapgisCity_ProjectManage_BH/REST/ProjectManageREST.svc/FetchGDConstructList")
                .append("?flowName=").append(flowName);

        if (!TextUtils.isEmpty(keyWord)) {
            sb.append("&keyWord=" + keyWord);
        }
        final String url = sb.toString();
        new MmtBaseTask<Void, Void, String>(mContext, true) {
            @Override
            protected String doInBackground(Void... params) {
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                mPullRefreshListView.onRefreshComplete();

                ResultData<GDConstructEvent> resultData = Utils.json2ResultDataToast(GDConstructEvent.class, mContext, s, "请求服务错误", true);

                if (resultData == null) {
                    return;
                }

                eventItemList.clear();
                eventItemList.addAll(resultData.DataList);

                adapter.notifyDataSetChanged();
            }
        }.mmtExecute();
    }
}
