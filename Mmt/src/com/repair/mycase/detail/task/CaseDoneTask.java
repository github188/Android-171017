package com.repair.mycase.detail.task;

import android.content.Context;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * 完工操作
 * <p>
 * <p>
 * 参数0：CaseID<br>
 * 参数1：ID0<br>
 * 参数2：json数据<br>
 * 参数3：所有文件的绝对路径<br>
 * 参数4：所有文件的相对路径
 */
public class CaseDoneTask extends MmtBaseTask<String, String, ResultWithoutData> {

    public CaseDoneTask(Context context, boolean showLoading) {
        super(context, showLoading);
    }

    @Override
    protected ResultWithoutData doInBackground(String... params) {
        String url = this.repairService + "/CaseDone?UserID=" + this.userID + "&CaseID=" + params[0] + "&StepID=" + params[1];

        ReportInBackEntity entity = new ReportInBackEntity(params[2], MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING, url, params[0], "完工信息", params[3], params[4]);
        return entity.report(this);
    }
}