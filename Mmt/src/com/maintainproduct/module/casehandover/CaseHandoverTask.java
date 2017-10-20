package com.maintainproduct.module.casehandover;

import com.google.gson.Gson;
import com.maintainproduct.entity.HandoverEntity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.module.taskcontrol.TaskControlDBHelper;

public class CaseHandoverTask {

    public void createCaseHandoverData(HandoverEntity params) {
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/MapgisCity_WorkFlow/REST/WorkFlowREST.svc/WorkFlow/MobileCaseHandOver";

        String paraStr = new Gson().toJson(params, HandoverEntity.class);

        ReportInBackEntity entity = new ReportInBackEntity(paraStr, MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING, url, params.caseNo, "移交案件", null, null);

        entity.insert();

        int taskId = entity.getIdInSQLite();

        if (taskId != -1) {
            TaskControlDBHelper.getIntance().createControlData(taskId + "");
        }
    }
}