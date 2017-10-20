package com.repair.zhoushan.module.devicecare;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.entity.FlowTableInfo;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 编辑台账信息
 */
public class EditAccountTableActivity extends BaseActivity {

    private FlowBeanFragment mFragment;

    private ScheduleTask mScheduleTask;
    private FlowTableInfo mFlowTableInfo; // 界面数据

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");
        this.userId = MyApplication.getInstance().getUserId();

        getBaseTextView().setText("台账信息编辑");

        createView();
    }

    private void createView() {

        MmtBaseTask<Void, Void, String> mmtBaseTask = new MmtBaseTask<Void, Void, String>(EditAccountTableActivity.this) {
            @Override
            protected String doInBackground(Void... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/"
                        + userID + "/AccountTableInfo?bizName="
                        + mScheduleTask.BizName + "&id=" + mScheduleTask.ID;

                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String jsonResult) {

                // resolve data.
                ResultData<FlowTableInfo> newData = Utils.json2ResultDataActivity(FlowTableInfo.class,
                        EditAccountTableActivity.this, jsonResult, "获取台账信息失败", false);
                if (newData == null) return;

                mFlowTableInfo = newData.getSingleData();

                // create fragment.
                mFragment = new FlowBeanFragment();
                Bundle args = new Bundle();
                args.putParcelable("GDFormBean", mFlowTableInfo.TableMetaDatas.get(0).FlowNodeMeta.mapToGDFormBean());
                mFragment.setArguments(args);

                mFragment.setFragmentFileRelativePath(mScheduleTask.TaskCode);
                mFragment.setCls(EditAccountTableActivity.class);
                mFragment.setAddEnable(true);

                addFragment(mFragment);

                createBottomView();
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();

    }

    private void createBottomView() {
        BottomUnitView saveUnitView = new BottomUnitView(EditAccountTableActivity.this);
        saveUnitView.setContent("保存");
        saveUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(saveUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                save();
            }
        });
    }

    private void save() {

        if (mFragment == null) {
            return;
        }

        List<FeedItem> feedbackItems = mFragment.getFeedbackItems(ReportInBackEntity.REPORTING);
        if (feedbackItems == null) {
            return;
        }

        ScheduleTaskSaveInfo mScheduleTaskSaveInfo = new ScheduleTaskSaveInfo();
        mScheduleTaskSaveInfo.flowNodeMeta = mFlowTableInfo.TableMetaDatas.get(0).FlowNodeMeta;
        // "工商户安检"业务类型特殊的逻辑，更新的是"工商户信息表"
        if ("工商户安检".equals(mScheduleTask.BizName)) {
            mScheduleTask.BizAccountTable = "工商户信息表";
        }
        mScheduleTaskSaveInfo.scheduleTask = mScheduleTask;
        mScheduleTaskSaveInfo.scheduleTask.UserID = String.valueOf(userId);

        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = mScheduleTaskSaveInfo.flowNodeMeta.Values;
        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedbackItems) {
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                    break;
                }
            }
        }
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/"
                + userId + "/AccountInfoSave";

        // 将信息转换为 JSON字符串
        String reportData = new Gson().toJson(mScheduleTaskSaveInfo, new TypeToken<ScheduleTaskSaveInfo>() {
        }.getType());

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                reportData,
                userId,
                ReportInBackEntity.REPORTING,
                url,
                UUID.randomUUID().toString(),
                mScheduleTask.BizName,
                mFragment.getAbsolutePaths(),
                mFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {

            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {
                        Toast.makeText(EditAccountTableActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditAccountTableActivity.this, "保存失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);

    }
}
