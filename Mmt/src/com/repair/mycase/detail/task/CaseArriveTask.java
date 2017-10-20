package com.repair.mycase.detail.task;

import android.content.Context;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * 到场操作
 * <p>
 * <p>
 * 参数0：CaseID<br>
 * 参数1：json数据<br>
 * 参数2：所有文件的绝对路径<br>
 * 参数3：所有文件的相对路径
 */
public class CaseArriveTask extends MmtBaseTask<String, String, ResultWithoutData> {

    public CaseArriveTask(Context context, boolean showLoading) {
        super(context, showLoading);
    }

    @Override
    protected ResultWithoutData doInBackground(String... params) {
        String url = this.repairService + "/CaseArrive?CaseID=" + params[0] + "&UserID=" + this.userID;

        ReportInBackEntity entity = new ReportInBackEntity(params[1], MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING, url, params[0], "到场信息", params[2], params[3]);
        ResultData<Integer> resultData = entity.report(this);

        ResultWithoutData resultWithoutData = new ResultWithoutData();
        resultWithoutData.ResultCode = resultData.ResultCode;
        resultWithoutData.ResultMessage = resultData.ResultMessage;

        return resultWithoutData;
    }
}
