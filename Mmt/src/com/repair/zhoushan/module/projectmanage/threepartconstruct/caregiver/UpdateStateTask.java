package com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.CaseInfo;

/**
 * Created by liuyunfan on 2015/12/12.
 */
public class UpdateStateTask extends MmtBaseTask<String, String, ResultWithoutData> {

    CaseInfo caseinfo;
    public UpdateStateTask(Context context, boolean showLoading,CaseInfo caseinfo) {
        super(context, showLoading);
        this.caseinfo = caseinfo;
    }

    @Override
    protected ResultWithoutData doInBackground(String... params) {
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventFlowStatus";
        ReportInBackEntity entity = new ReportInBackEntity(new Gson().toJson(caseinfo, new TypeToken<CaseInfo>() {
        }.getType()), MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING, url, "", caseinfo.CaseNo, "", "");

        ResultData<Integer> resultData = entity.report(this);

        ResultWithoutData resultWithoutData = new ResultWithoutData();
        resultWithoutData.ResultCode = resultData.ResultCode;
        resultWithoutData.ResultMessage = resultData.ResultMessage;

        return resultWithoutData;
    }
}

