package com.repair.zhoushan.module.casemanage.casedetail;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.NodeEntity;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;

public class GetHandoverUsersTask extends MmtBaseTask<String, Integer, Node> {

    public static final String DEFAULT_URL;

    static {
        DEFAULT_URL = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/WorkFlow/GetHandoverTree";
    }

    public GetHandoverUsersTask(Context context, boolean showLoading, OnWxyhTaskListener<Node> listener) {
        super(context, showLoading, listener);
    }

    @Override
    protected Node doInBackground(String... params) {

        Node node = null;

        try {

            String url;
            if (params.length > 1 && !TextUtils.isEmpty(params[1])) {
                url = params[1];
            } else {
                url = DEFAULT_URL;
            }

            String json = NetUtil.executeHttpPost(url, params[0], "Content-Type", "application/json; charset=utf-8");

            if (BaseClassUtil.isNullOrEmptyString(json)) {
                throw new Exception("返回结果为空");
            }

            NodeEntity nodeEntity = null;
            if (json.contains("currentPageIndex")) {
                Results<NodeEntity> results = new Gson().fromJson(json, new TypeToken<Results<NodeEntity>>(){}.getType());
                if (results != null && results.say != null && TextUtils.isEmpty(results.say.errMsg)) {
                    nodeEntity = results.getMe.get(0);
                }
            } else {
                nodeEntity = new Gson().fromJson(json, new TypeToken<NodeEntity>() {
                }.getType());
            }

            if (nodeEntity != null) {
                node = nodeEntity.toNode(null);
            }

            return node;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
