package com.repair.gisdatagather.product.addproject;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultStatus;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.gisdatagather.common.entity.GISDataProject;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.repair.gisdatagather.product.projectlist.ProjectListFragment;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.module.BaseDialogActivity;

import java.util.List;

/**
 * Created by liuyunfan on 2016/3/29.
 */
public class AddProjectDialogActivity extends BaseDialogActivity {
    String projectName = "";
    GISDataProject gisDataProject;

    //不点击取消，面板就不消失
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    protected void handleOkEvent(String title, List<FeedItem> feedItemList) {

        for (FeedItem feedItem : feedItemList) {
            switch (feedItem.Name) {
                case "工程名称":
                    projectName = feedItem.Value;
                    break;
            }
        }
        if (TextUtils.isEmpty(projectName)) {
            Toast.makeText(AddProjectDialogActivity.this, "工程名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (title) {

            case "新建工程": {
                // 将所有信息封装成后台上传的数据模型
                gisDataProject = new GISDataProject(projectName);
                new MmtBaseTask<Void, Void, CaseItem>(AddProjectDialogActivity.this, true) {
                    @Override
                    protected CaseItem doInBackground(Void... params) {
                        try {

                            int projectID = createPoj();
                            if (projectID <= 0) {
                                return null;
                            }
                            gisDataProject.setID(projectID);

                            return getPojCaseItem(projectID);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onSuccess(CaseItem caseItem) {
                        if (caseItem == null) {
                            return;
                        }
                        AddProjectDialogActivity.super.onSuccess();

                        gisDataProject.setCaseItem(caseItem);
                        GisDataGatherUtils.openGisGather(MyApplication.getInstance().mapGISFrame, gisDataProject, false, true);
                    }
                }.executeOnExecutor(MyApplication.executorService);
            }
            break;
        }
    }

    private int createPoj() {
        try {
            ReportInBackEntity entity = gisDataProject.getProjectReportInBackEntity(AddProjectDialogActivity.this, GisDataGatherUtils.getFlowCenterDataForProduct());
            String ret = NetUtil.executeHttpPost(entity.getUri(), entity.getData());

            if (TextUtils.isEmpty(ret)) {
                MyApplication.getInstance().showMessageWithHandle("工程创建失败");
                return -1;
            }
            ResultStatus rawData = new Gson().fromJson(ret, new TypeToken<ResultStatus>() {
            }.getType());
            if (rawData == null) {
                MyApplication.getInstance().showMessageWithHandle("工程解析失败");
                return -1;
            }
            if (!TextUtils.isEmpty(rawData.errMsg)) {
                MyApplication.getInstance().showMessageWithHandle(rawData.errMsg);
                return -1;
            }

            ResultWithoutData newData = rawData.toResultWithoutData();
            return newData.ResultCode;

        } catch (Exception ex) {
            return -1;
        }
    }

    private CaseItem getPojCaseItem(int pojID) {
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/" + MyApplication.getInstance().getUserId() + "/GetStardEventDoingBoxByEventID"
                + "?flowName=" + ProjectListFragment.flowName
                + "&flowNode=" + ProjectListFragment.secendNodeName
                + "&eventID=" + pojID;

        String ret = NetUtil.executeHttpGet(uri);
        if (TextUtils.isEmpty(ret)) {
            MyApplication.getInstance().showMessageWithHandle("获取工程流程信息失败");
            return null;
        }
        Results<CaseItem> caseItemResults = new Gson().fromJson(ret, new TypeToken<Results<CaseItem>>() {
        }.getType());
        if (caseItemResults == null) {
            MyApplication.getInstance().showMessageWithHandle("解析工程流程信息失败");
            return null;
        }
        ResultData<CaseItem> resultData = caseItemResults.toResultData();
        if (!TextUtils.isEmpty(resultData.ResultMessage)) {
            MyApplication.getInstance().showMessageWithHandle(resultData.ResultMessage);
            return null;
        }
        if (resultData.DataList.size() == 0) {
            MyApplication.getInstance().showMessageWithHandle("获取工程流程信息失败");
            return null;
        }
        return resultData.getSingleData();
    }
}
