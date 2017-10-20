package com.repair.common;

import android.content.Context;
import android.widget.Toast;

import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;

public class ReportPostTask extends MmtBaseTask<ReportInBackEntity, String, ResultData<Integer>> {
    public ReportPostTask(Context context) {
        super(context);
    }

    @Override
    protected ResultData<Integer> doInBackground(ReportInBackEntity... params) {
        return params[0].report(this);
    }

    @Override
    protected void onSuccess(ResultData<Integer> data) {
        super.onSuccess(data);

        Toast.makeText(context, data.ResultMessage, Toast.LENGTH_SHORT).show();
    }
}
