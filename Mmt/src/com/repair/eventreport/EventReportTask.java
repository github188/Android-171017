package com.repair.eventreport;

import android.content.Context;

import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;


public class EventReportTask extends MmtBaseTask<ReportInBackEntity, String, ResultData<Integer>> {
    public EventReportTask(Context context, boolean showLoading, OnWxyhTaskListener<ResultData<Integer>> listener) {
        super(context, showLoading, listener);
        setCancellable(false);
    }

    @Override
    protected ResultData<Integer> doInBackground(ReportInBackEntity... params) {
        return params[0].report(this);
    }
}
