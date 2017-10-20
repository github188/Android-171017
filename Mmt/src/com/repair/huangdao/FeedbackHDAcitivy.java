package com.repair.huangdao;

import android.os.Bundle;

import com.google.gson.Gson;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.HandoverEntity;
import com.maintainproduct.module.BeanFragment;
import com.maintainproduct.module.casehandover.CaseHandoverUserFragment;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.maintainproduct.module.maintenance.feedback.MaintenanceFormActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;
import com.repair.common.ReportPostTask;

import java.util.List;

public class FeedbackHDAcitivy extends MaintenanceFormActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //审核节点不用保存就可以移交
        if(itemEntity.ActiveName.contains("审核")){
            isReported=true;
        }
    }

    @Override
    public boolean saveGroupValue(BeanFragment formBeanFragment, int status) {
        if (formBeanFragment == null) {
            return false;
        }

        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/MapgisCity_WXYH_Huangdao/REST/ServiceManageREST.svc/Feedback";

        List<FeedItem> items = formBeanFragment.getFeedbackItems(status);

        if (items == null || items.size() == 0) {
            return false;
        }

        UserBean userBean = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);

        CaseItemV21 fbItem = new CaseItemV21();

        fbItem.fbRepairMan = userBean.TrueName;
        fbItem.fbRepairManID = userBean.UserID;
        fbItem.CaseID = String.valueOf(itemEntity.ID);

        for (FeedItem item : items) {
            switch (item.Name) {
                case "开始时间":
                    fbItem.fbStartTime = item.Value;
                    break;
                case "维修管径":
                    fbItem.fbDiameter = item.Value;
                    break;
                case "埋深":
                    fbItem.fbDepth = item.Value;
                    break;
                case "停水影响范围":
                    fbItem.fbStopRange = item.Value;
                    break;
                case "用户告知情况":
                    fbItem.fbUserNotice = item.Value;
                    break;
                case "临时水供应情况":
                    fbItem.fbTempWater = item.Value;
                    break;
                case "工作面恢复情况":
                    fbItem.fbWorkRetore = item.Value;
                    break;
                case "工建队伍":
                    fbItem.fbTroops = item.Value;
                    break;
                case "机械设备":
                    fbItem.fbDevice = item.Value;
                    break;
                case "用料情况":
                    fbItem.fbMaterial = item.Value;
                    break;
                case "维修前照片":
                    fbItem.fbPhotoBefore = item.Value;
                    break;
                case "维修中照片":
                    fbItem.fbPhotoMid = item.Value;
                    break;
                case "维修后照片":
                    fbItem.fbPhotoAfter = item.Value;
                    break;
                case "事故原因":
                    fbItem.fbReason = item.Value;
                    break;
                case "维修情况":
                    fbItem.fbSituation = item.Value;
                    break;
                case "结束时间":
                    fbItem.fbEndTime = item.Value;
                    break;
            }
        }

        // 将对信息转换为JSON字符串
        String json = new Gson().toJson(fbItem, CaseItemV21.class);

        // 将所有信息封装成后台上传的数据模型
        ReportInBackEntity entity = new ReportInBackEntity(json, userBean.UserID, status,
                uri, fbItem.CaseID, "黄岛工单反馈", formBeanFragment.getAbsolutePaths(),
                formBeanFragment.getRelativePaths());

        new ReportPostTask(this).mmtExecute(entity);
        isReported = true;
        return false;
    }

    @Override
    protected void handover() {
        CaseHandoverUserFragment fragment = new CaseHandoverUserFragment(itemEntity, handler, MaintenanceConstant.SERVER_ONLY_HANDOVER);

        fragment.show(getSupportFragmentManager(), "");
    }

    @Override
    protected void startHandoverTask(HandoverEntity params) {
        CaseInfo info = new CaseInfo();

        info.CaseID = String.valueOf(itemEntity.ID);
        info.CaseNO = params.caseNo;
        info.IsOver = 0;
        info.activeID = params.activeID;
        info.activeName = params.activeName;
        info.direction = params.direction;
        info.nextActiveID = "";
        info.opinion = params.option;
        info.stepID = params.stepID;
        info.undertakeman = params.undertakeman;
        info.userID = params.userID;
        info.userTrueName = params.userTrueName;

        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/MapgisCity_WXYH_Huangdao/REST/ServiceManageREST.svc/CaseHandover";

        String json = new Gson().toJson(info, CaseInfo.class);

        ReportInBackEntity entity = new ReportInBackEntity(json, MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING, url, info.CaseID, "移交案件", null, null);

        new ReportPostTask(this) {
            @Override
            protected void onSuccess(ResultData<Integer> data) {
                super.onSuccess(data);

                if (data.ResultCode > 0) {
                    setResult(data.ResultCode);
                    onBackPressed();
                }
            }
        }.mmtExecute(entity);
    }
}
