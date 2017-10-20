package com.repair.jinanwater.inapparentleak;

import android.widget.Toast;

import com.mapgis.mmt.config.ServerConnectConfig;
import com.repair.zhoushan.entity.CaseInfo;
import com.mapgis.mmt.global.OnResultListener;
import com.repair.zhoushan.module.casemanage.casehandover.CaseHandoverActivity;

public class ReceiveCaseHandoverActivity extends CaseHandoverActivity {

    @Override
    protected String getHandoverUsersUrl(CaseInfo ci) {
        return ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_JN/REST/CaseManageREST.svc/WorkFlow/GetHandoverTreeForWeb";
    }

    @Override
    protected void performHandoverAction() {

        // 先保存，保存成功之后再移交
        setOnSavedResultListener(new OnResultListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ReceiveCaseHandoverActivity.super.performHandoverAction();
            }

            @Override
            public void onFailed(String errMsg) {
                Toast.makeText(ReceiveCaseHandoverActivity.this, errMsg, Toast.LENGTH_SHORT).show();
            }
        });
        performSaveAction();
    }
}
