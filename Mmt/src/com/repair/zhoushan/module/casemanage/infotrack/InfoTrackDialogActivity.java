package com.repair.zhoushan.module.casemanage.infotrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultStatus;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.BaseDialogActivity;

import java.util.List;

public class InfoTrackDialogActivity extends BaseDialogActivity {

    private HotlineModel mHotlineModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.mHotlineModel = getIntent().getParcelableExtra("ListItemEntity");
    }

    @Override
    protected void handleOkEvent(String tag, List<FeedItem> feedItemList) {

        InfoFollowModel infoFollowModel = new InfoFollowModel();

        if (getmTag().equals("信息跟踪")) {

            for (FeedItem feedItem : feedItemList) {

                switch (feedItem.Name) {

                    case "跟踪登录员":
                        infoFollowModel.Follower = feedItem.Value;
                        break;
                    case "跟踪时间":
                        infoFollowModel.FollowTime = feedItem.Value;
                        break;
                    case "跟踪结果":
                        infoFollowModel.Result = feedItem.Value;
                        break;
                    case "跟踪备注":
                        infoFollowModel.Remark = feedItem.Value;
                        break;
                }
            }

            infoFollowModel.EventCode = mHotlineModel.EventCode;
            infoFollowModel.DealStation = mHotlineModel.DealStation;
            infoFollowModel.ReportTime = mHotlineModel.ReportTime;

            infoTrack(infoFollowModel);
        }
    }

    private void infoTrack(final InfoFollowModel infoFollowModel) {

        MmtBaseTask<String, Void, ResultWithoutData> mmtBaseTask = new MmtBaseTask<String, Void, ResultWithoutData>(InfoTrackDialogActivity.this) {
            @Override
            protected ResultWithoutData doInBackground(String... params) {

                ResultWithoutData data;

                String dataStr = new Gson().toJson(infoFollowModel, InfoFollowModel.class);

                // 执行网络操作
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_ProjectManage_ZS/REST/ProjectManageREST.svc/ReportFollowInfo";

                try {

                    String result = NetUtil.executeHttpPost(url, dataStr,
                            "Content-Type", "application/json; charset=utf-8");

                    if (BaseClassUtil.isNullOrEmptyString(result)) {
                        throw new Exception("返回结果为空");
                    }

                    ResultStatus resultStatus = new Gson().fromJson(result, new TypeToken<ResultStatus>() {
                    }.getType());

                    data = resultStatus.toResultWithoutData();

                } catch (Exception e) {
                    e.printStackTrace();
                    data = new ResultWithoutData();
                    data.ResultCode = -200;
                    data.ResultMessage = e.getMessage();
                }

                return data;
            }

            @Override
            protected void onSuccess(ResultWithoutData resultWithoutData) {
                if (resultWithoutData.ResultCode == 200) {
                    Toast.makeText(InfoTrackDialogActivity.this, "上报成功", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(InfoTrackDialogActivity.this, OnlineTrackListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.addCategory(Constants.CATEGORY_BACK_TO_LIST);
                    startActivity(intent);

                } else {
                    Toast.makeText(InfoTrackDialogActivity.this, resultWithoutData.ResultMessage, Toast.LENGTH_SHORT).show();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }
}
