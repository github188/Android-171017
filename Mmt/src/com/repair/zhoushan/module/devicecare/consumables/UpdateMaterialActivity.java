package com.repair.zhoushan.module.devicecare.consumables;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.CusBottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoOrderBean;

import java.util.LinkedList;
import java.util.List;

/**
 * 退料
 */
public class UpdateMaterialActivity extends BaseActivity {

    private UpdateMaterialFragment mUpdateMaterialFragment;

    private ScheduleTask mScheduleTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");

        getBaseTextView().setText("退料");

        createView();
        createBottomView();
    }

    private void createView() {

        this.mUpdateMaterialFragment = new UpdateMaterialFragment();

        Bundle args = new Bundle();
        args.putString("MaterialListStr", getIntent().getStringExtra("MaterialListStr"));
        args.putBoolean("IsExistCostCenter", getIntent().getBooleanExtra("IsExistCostCenter", false));
        mUpdateMaterialFragment.setArguments(args);

        addFragment(mUpdateMaterialFragment);
    }

    private void createBottomView() {

        LinearLayout bottomView = getBottomView();
        bottomView.setBackgroundResource(0);
        bottomView.setMinimumHeight(DimenTool.dip2px(UpdateMaterialActivity.this, 45));

        CusBottomUnitView feedbackUnitView = new CusBottomUnitView(UpdateMaterialActivity.this);
        feedbackUnitView.setContent("退 料");

        addBottomUnitView(feedbackUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<WuLiaoBean> hackbackData = new LinkedList<WuLiaoBean>();

                List<WuLiaoBean> materialList = mUpdateMaterialFragment.getMaterialList();
                for (WuLiaoBean material : materialList) {
                    if (material.getTagValue() != 0) {
                        material.setNum(material.getTagValue());
                        hackbackData.add(material);
                    }
                }

                if (hackbackData.size() == 0) {
                    Toast.makeText(UpdateMaterialActivity.this, "请先选择需退物料", Toast.LENGTH_SHORT).show();
                    return;
                }

                handbackMaterial(hackbackData);
            }
        });
    }

    private void handbackMaterial(final List<WuLiaoBean> hackbackData) {

        MmtBaseTask<String, Void, ResultWithoutData> mmtBaseTask = new MmtBaseTask<String, Void, ResultWithoutData>(UpdateMaterialActivity.this) {
            @Override
            protected ResultWithoutData doInBackground(String... params) {

                ResultWithoutData data;

                WuLiaoOrderBean materialOrder = new WuLiaoOrderBean();

                // 填充数据
                materialOrder.WuLiaoList.addAll(hackbackData);
                materialOrder.CaseNo = mScheduleTask.TaskCode;
                materialOrder.FlowCode = mScheduleTask.PreCodeFormat;
                materialOrder.OperType = "退料";
                materialOrder.OperTime = BaseClassUtil.getSystemTime();
                materialOrder.OperMan = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

                // 请求网络
                String dataStr = new Gson().toJson(materialOrder, WuLiaoOrderBean.class);

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/PostWuLiaoOrder";

                try {

                    String result = NetUtil.executeHttpPost(url, dataStr,
                            "Content-Type", "application/json; charset=utf-8");

                    if (BaseClassUtil.isNullOrEmptyString(result)) {
                        throw new Exception("返回结果为空");
                    }

                    data = new Gson().fromJson(result, new TypeToken<ResultWithoutData>() {
                    }.getType());

                } catch (Exception e) {
                    e.printStackTrace();
                    data = new ResultWithoutData();
                    data.ResultCode = -200;
                    data.ResultMessage = e.getMessage();
                }

                return data;
            }

            @Override
            protected void onSuccess(ResultWithoutData data) {
                if (data == null) {
                    Toast.makeText(UpdateMaterialActivity.this, "退料失败", Toast.LENGTH_SHORT).show();
                } else if (data.ResultCode != 200) {
                    String msg = TextUtils.isEmpty(data.ResultMessage) ? "退料失败" : data.ResultMessage;

                    Toast.makeText(UpdateMaterialActivity.this, msg, Toast.LENGTH_LONG).show();
                } else {
                    String msg = TextUtils.isEmpty(data.ResultMessage) ? "退料成功" : data.ResultMessage;

                    Toast.makeText(UpdateMaterialActivity.this, msg, Toast.LENGTH_SHORT).show();

                    setResult(Activity.RESULT_OK);
                    AppManager.finishActivity();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

}
