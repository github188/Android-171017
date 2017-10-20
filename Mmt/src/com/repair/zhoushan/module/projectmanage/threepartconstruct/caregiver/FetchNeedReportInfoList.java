package com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.List;

/**
 * Created by liuyunfan on 2015/12/8.
 */
public class FetchNeedReportInfoList extends MmtBaseTask<Void, Void, ResultData<String>> {
    private Context context;
    private List<String> list = null;
    private String sql;


    public FetchNeedReportInfoList(Context context, List<String> list,String sql) {
        super(context, false);
        this.list = list;
        this.sql=sql;
    }

    @Override
    protected ResultData<String> doInBackground(Void... params) {
        try {
            String resultStr = NetUtil
                    .executeHttpGet(
                            ServerConnectConfig.getInstance()
                                    .getBaseServerPath()
                                    + "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/GetDefaultValuesBySQL",
                            "sql", sql);

            if (BaseClassUtil.isNullOrEmptyString(resultStr))
                return null;

            return new Gson().fromJson(resultStr,
                    new TypeToken<ResultData<String>>() {
                    }.getType());

        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(ResultData<String> result) {
        if (result == null) {
            return;
        }

        if (result.ResultCode < 0) {
            return;
        }
        list.addAll(result.DataList);
    }
}
