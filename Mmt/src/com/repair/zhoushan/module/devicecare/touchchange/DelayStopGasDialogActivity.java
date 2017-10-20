package com.repair.zhoushan.module.devicecare.touchchange;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.module.BaseDialogActivity;

import java.util.List;

/**
 * Created by liuyunfan on 2016/3/15.
 */
public class DelayStopGasDialogActivity extends BaseDialogActivity {

    @Override
    protected void handleOkEvent(String title, List<FeedItem> feedItemList) {
        Intent intent = getIntent();
        String caseno = intent.getStringExtra("caseno");
        String applyEndTime = "";
        String reason = "";
        for (FeedItem feedItem : feedItemList) {
            switch (feedItem.Name) {
                case "延长时间":
                    applyEndTime = feedItem.Value;
                    break;
                case "延长原因":
                    reason = feedItem.Value;
                    break;
            }
        }
        if (TextUtils.isEmpty(applyEndTime)) {
            Toast.makeText(DelayStopGasDialogActivity.this, "延长时间不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (title) {

            case "申请延长停气":
                new MmtBaseTask<String, Void, String>(DelayStopGasDialogActivity.this) {
                    @Override
                    protected String doInBackground(String... params) {
                        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CrashReplace/DelayStopGas?caseNO=" + params[0] + "&applyEndTime=" + params[1] + "&reason=" + params[2];
                        return NetUtil.executeHttpGet(uri.replaceAll(" ", "%20"));
                    }

                    @Override
                    protected void onSuccess(String s) {
                        super.onSuccess(s);
                        Utils.json2ResultToast(DelayStopGasDialogActivity.this, s, "延期失败");
                        if (Utils.json2ResultToast(DelayStopGasDialogActivity.this, s, "延期失败")) {
                            Toast.makeText(context, "延期成功", Toast.LENGTH_SHORT).show();
                            DelayStopGasDialogActivity.super.onSuccess();
                        }
                    }
                }.mmtExecute(caseno, applyEndTime, reason);

                break;
        }
    }
}
