package com.repair.changzhou.pianguan;

import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;

public class PianGuanCaseDetailActivity extends CaseDetailActivity {

    @Override
    protected void createBottomView() {
//        addBottomUnitView("关单", true, new OnNoDoubleClickListener() {
//            @Override
//            public void onNoDoubleClick(View v) {
//                caseFinish();
//            }
//        });
    }

    private void caseFinish() {

        CaseInfo caseInfo = caseItemEntity.mapToCaseInfo();
        caseInfo.Opinion = "关闭工单";

        new MmtBaseTask<String, Void, String>(this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
            @Override
            public void doAfter(String result) {

                if (BaseClassUtil.isNullOrEmptyString(result) || result.equals("\"\"")) {
                    Toast.makeText(PianGuanCaseDetailActivity.this, "关单成功!", Toast.LENGTH_SHORT).show();
                    setResult(PianGuanCaseListFragment.RC_NEED_REFRESH_DATA);
                    finish();
                } else {
                    Toast.makeText(PianGuanCaseDetailActivity.this, "关闭工单失败！", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected String doInBackground(String... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/WorkFlow/CaseFinish";
                try {
                    return NetUtil.executeHttpPost(url, params[0], "Content-Type", "application/json; charset=utf-8");
                } catch (Exception e) {
                    e.printStackTrace();
                    return "关闭工单失败: " + e.getMessage();
                }
            }
        }.mmtExecute(new Gson().toJson(caseInfo));
    }
}
