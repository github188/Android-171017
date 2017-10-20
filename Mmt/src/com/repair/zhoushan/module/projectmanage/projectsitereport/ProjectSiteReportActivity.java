package com.repair.zhoushan.module.projectmanage.projectsitereport;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.CusBottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FeedbackData;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.List;
import java.util.UUID;

public class ProjectSiteReportActivity extends BaseActivity {

    private static final String TABLE_NAME = "工程管理现场信息表";

    // 界面结构数据
    private FlowNodeMeta mFlowNodeMeta;
    // 上报信息
    private final FeedbackData mFeedbackData = new FeedbackData();

    private FlowBeanFragment mFlowBeanFragment;

    private ProjectInfoModel mProjectInfoModel;

    private String currentProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBaseTextView().setText("工程现场上报");

        mProjectInfoModel = getIntent().getParcelableExtra("ListItemEntity");
        if (mProjectInfoModel == null) {
            showErrorMsg("获取参数失败");
            return;
        }

        initView();
    }

    private void initView() {

        MmtBaseTask<String, Void, String[]> mmtBaseTask = new MmtBaseTask<String, Void, String[]>(ProjectSiteReportActivity.this, true) {

            @Override
            protected String[] doInBackground(String... params) {

                String[] results = new String[2];

                // 界面结构
                String url0 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableGroupMeta";

                String url1 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_ProjectManage_ZS/REST/ProjectManageREST.svc/ProjectManage/GetProgressByCaseNO";

                results[0] = NetUtil.executeHttpGet(url0, "tableName", TABLE_NAME, "uiGroup", "");
                results[1] = NetUtil.executeHttpGet(url1, "caseno", mProjectInfoModel.CaseNo);

                return results;
            }

            @Override
            protected void onSuccess(String[] resultStrs) {

                ResultData<FlowNodeMeta> tableResult = Utils.json2ResultDataActivity(FlowNodeMeta.class,
                        ProjectSiteReportActivity.this, resultStrs[0], "获取界面结构失败", false);
                if (tableResult == null) return;
                mFlowNodeMeta = tableResult.getSingleData();

                ResultData<String> progressResult = Utils.json2ResultDataToast(String.class,
                        ProjectSiteReportActivity.this, resultStrs[1], "未查询到工程进度信息", false);
                if (progressResult != null) {
                    currentProgress = progressResult.getSingleData();
                }

                createView();
                createBottomView();
            }

        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void createView() {

        for (FlowNodeMeta.TableValue value : mFlowNodeMeta.Values) {
            if ("工单编号".equals(value.FieldName)) {
                value.FieldValue = mProjectInfoModel.CaseNo;
                break;
            }
        }
        if (!TextUtils.isEmpty(currentProgress)) {
            for (FlowNodeMeta.TableValue value : mFlowNodeMeta.Values) {
                if ("完成进度".equals(value.FieldName)) {
                    value.FieldValue = currentProgress;
                    break;
                }
            }
        }

        this.mFlowBeanFragment = new FlowBeanFragment();

        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", mFlowNodeMeta.mapToGDFormBean());
        mFlowBeanFragment.setArguments(args);

        addFragment(mFlowBeanFragment);
    }

    private void createBottomView() {

        LinearLayout bottomView = getBottomView();
        bottomView.setBackgroundResource(0);
        bottomView.setMinimumHeight(DimenTool.dip2px(ProjectSiteReportActivity.this, 45));

        CusBottomUnitView feedbackUnitView = new CusBottomUnitView(ProjectSiteReportActivity.this);
        feedbackUnitView.setContent("上 报");

        addBottomUnitView(feedbackUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                doReport();
            }
        });
    }

    private void doReport() {

        if (mFlowBeanFragment == null) {
            return;
        }

        try {
            List<FeedItem> feedItemList = mFlowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

            if (feedItemList == null) {
                return;
            }

            // 把Feedback的值映射到Value上
            for (FlowNodeMeta.TableValue value : mFlowNodeMeta.Values) {
                for (FeedItem item : feedItemList) {
                    if (value.FieldName.equals(item.Name)) {
                        value.FieldValue = item.Value;
                        break;
                    }
                }
            }

            mFeedbackData.DataParam.flowNodeMeta = mFlowNodeMeta;
            mFeedbackData.TableName = TABLE_NAME;

            mFeedbackData.DefaultParam = " : ";

            // 创建服务路径
            String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveFeedbackData";

            String data = new Gson().toJson(mFeedbackData, new TypeToken<FeedbackData>() {
            }.getType());

            // 将所有信息封装成后台上传的数据模型
            final ReportInBackEntity entity = new ReportInBackEntity(
                    data,
                    MyApplication.getInstance().getUserId(),
                    ReportInBackEntity.REPORTING,
                    uri,
                    UUID.randomUUID().toString(),
                    "",
                    mFlowBeanFragment.getAbsolutePaths(),
                    mFlowBeanFragment.getRelativePaths());

            new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
                @Override
                public void doAfter(ResultData<Integer> result) {
                    try {
                        if (result.ResultCode > 0) {

                            success();
                        } else {
                            Toast.makeText(ProjectSiteReportActivity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).mmtExecute(entity);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void success() {
        Toast.makeText(ProjectSiteReportActivity.this, "上报成功", Toast.LENGTH_SHORT).show();

        // 成功后跳转至列表页面
        Intent intent = new Intent(ProjectSiteReportActivity.this, ProjectSiteReportListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addCategory(Constants.CATEGORY_BACK_TO_LIST);
        startActivity(intent);

        this.finish();
    }

}


