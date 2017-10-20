package com.maintainproduct.module.maintenance.feedback;

import android.os.AsyncTask;
import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取存储在本地或者存储在网络数据库的数据，并将值显示在信息指定的位置。<br>
 * 该访问服务会先访问服务器数据库存储的数据，若数据库存储的有数据，则返回服务器的数据， 说明该工单是已经反馈过的工单；
 * 若服务器数据库没有数据，则试着从本地数据库读取数据，若有指定数据，则说明该工单时存储在本地还未上传的数据。
 */
public class MaintenanceFormValueTask extends AsyncTask<Object, String, String> {
    private final BaseActivity activity;
    private final Handler handler;

    private MaintainSimpleInfo itemEntity;
    private GDFormBean data;

    public MaintenanceFormValueTask(BaseActivity activity, Handler handler) {
        this.activity = activity;
        this.handler = handler;
    }

    @Override
    protected void onPreExecute() {
        activity.setBaseProgressBarVisibility(true);
    }

    @Override
    protected String doInBackground(Object... params) {
        itemEntity = (MaintainSimpleInfo) params[0];
        data = (GDFormBean) params[1];

        // 从服务器去除反馈的数据，满足单子被打回时候，在原有基础上进行修反馈
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/GetGDFormValueByCaseNo";

        String jsonStr = NetUtil.executeHttpGet(url, "CaseNo", itemEntity.CaseNo, "tableName", data.TableName);

        ResultData<List<FeedItem>> data = new Gson().fromJson(jsonStr, new TypeToken<ResultData<List<FeedItem>>>() {
        }.getType());

        if (data != null && data.ResultCode == 200 && data.DataList != null) {
            this.data.setValueByFeedItems(data.getSingleData());

            return "";
        }

        // 若本地缓存的有数据，则取出本地缓存数据
        ArrayList<ReportInBackEntity> list = DatabaseHelper.getInstance().query(ReportInBackEntity.class,
                new SQLiteQueryParameters("key='" + itemEntity.CaseNo + "_工单反馈'"));

        if (list != null && list.size() > 0) {
            ReportInBackEntity reportInBackEntity = list.get(0);

            List<FeedItem> items = new Gson().fromJson(reportInBackEntity.getData(), new TypeToken<List<FeedItem>>() {
            }.getType());

            this.data.setValueByFeedItems(items);
        }

        return "";
    }

    @Override
    protected void onPostExecute(String result) {

        activity.setBaseProgressBarVisibility(false);

        handler.sendEmptyMessage(MaintenanceConstant.SERVER_CREATE_FEEDBACK_VIEW);
    }

}