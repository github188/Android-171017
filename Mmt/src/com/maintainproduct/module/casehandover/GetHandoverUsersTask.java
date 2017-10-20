package com.maintainproduct.module.casehandover;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.HandoverEntity;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.mapgis.mmt.CacheUtils;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.NodeEntity;

// ////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * 获取下移承办人员列表
 */
// ////////////////////////////////////////////////////////////////////////////////////////////////
public abstract class GetHandoverUsersTask extends AsyncTask<MaintainSimpleInfo, Integer, String> {
    private final FragmentActivity activity;
    private MaintainSimpleInfo caseItemEntity;

    public GetHandoverUsersTask(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(MaintainSimpleInfo... params) {
        caseItemEntity = params[0];

        // 操作标识+用户ID+ID0
        String json = CacheUtils.getInstance(activity).get(
                "HandoverUsers" + MyApplication.getInstance().getUserId() + caseItemEntity.ID0);

        if (!BaseClassUtil.isNullOrEmptyString(json)) {
            return json;
        }

        try {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/MapgisCity_WorkFlow/REST/WorkFlowREST.svc/WorkFlow/GetHandoverTree";

            HandoverEntity para = new HandoverEntity(caseItemEntity);
            para.userID = MyApplication.getInstance().getUserId() + "";
            para.stepID = caseItemEntity.ID0;

            String paraStr = new Gson().toJson(para, HandoverEntity.class);

            json = NetUtil.executeHttpPost(url, paraStr, "Content-Type", "application/json; charset=utf-8");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null || result.length() == 0) {
            Toast.makeText(activity, "获取人员机构列表失败", Toast.LENGTH_SHORT).show();
            onTaskDone(null);
        } else {
            CacheUtils.getInstance(activity).put("HandoverUsers" + MyApplication.getInstance().getUserId() + caseItemEntity.ID0,
                    result);

            NodeEntity entity = new Gson().fromJson(result, NodeEntity.class);
            Node node = entity.toNode(null);

            onTaskDone(node);
        }

    }

    protected abstract void onTaskDone(Node node);
}