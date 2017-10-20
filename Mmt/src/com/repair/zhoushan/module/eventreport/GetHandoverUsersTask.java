package com.repair.zhoushan.module.eventreport;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.NodeEntity;
import com.mapgis.mmt.global.MmtBaseTask;

public class GetHandoverUsersTask extends MmtBaseTask<String, Integer, Node> {

    public GetHandoverUsersTask(Context context, boolean showLoading, OnWxyhTaskListener<Node> listener) {
        super(context, showLoading, listener);
        setCancellable(false);
    }

    @Override
    protected Node doInBackground(String... params) {

        Node node = null;
        try {

            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/WorkFlow/WFGetSecondNodeMenTreeByStation";

            String json = NetUtil.executeHttpGet(url, "flowName", params[0], "userID", params[1], "station", params[2]);

            if (TextUtils.isEmpty(json))
                return null;

            NodeEntity nodeEntity = new Gson().fromJson(json, new TypeToken<NodeEntity>() {
            }.getType());

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
