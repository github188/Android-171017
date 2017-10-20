package com.repair.zhoushan.module.devicecare.consumables;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.repair.zhoushan.module.devicecare.consumables.entity.Material;

import java.util.List;

public class AddHaoCaiActivity extends BaseActivity {

    private ScheduleTask mScheduleTask;

    private AddHaoCaiFragment addCaiLiaoFragment;

    private boolean showOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");
        this.showOnly = getIntent().getBooleanExtra("ShowOnly", false);

        createView();
        createBottomView();
    }

    private void createView() {

        this.addCaiLiaoFragment = new AddHaoCaiFragment();

        Bundle args = new Bundle();
        args.putString("CareType", getIntent().getStringExtra("CareType"));
        args.putParcelable("ListItemEntity", mScheduleTask);
        args.putBoolean("ShowOnly", showOnly);
        addCaiLiaoFragment.setArguments(args);

        addFragment(addCaiLiaoFragment);
    }

    private void createBottomView() {

        if (showOnly) {
            return;
        }

        LinearLayout bottomView = getBottomView();
        bottomView.setBackgroundResource(0);
        bottomView.setMinimumHeight(DimenTool.dip2px(AddHaoCaiActivity.this, 45));

        CusBottomUnitView feedbackUnitView = new CusBottomUnitView(AddHaoCaiActivity.this);
        feedbackUnitView.setContent("保 存");

        addBottomUnitView(feedbackUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Material> materialList = addCaiLiaoFragment.getMaterialList();
                addMaterial(materialList);
            }
        });
    }

    private void addMaterial(final List<Material> materialList) {

        if (materialList.size() == 0) {
            return;
        }

        MmtBaseTask<Void, Void, ResultWithoutData> mmtBaseTask = new MmtBaseTask<Void, Void, ResultWithoutData>(AddHaoCaiActivity.this) {
            @Override
            protected ResultWithoutData doInBackground(Void... params) {

                ResultWithoutData data;

                String dataStr = new Gson().toJson(materialList, new TypeToken<List<Material>>() {
                }.getType());

                // 执行网络操作
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostMaterials?caseNo=" + mScheduleTask.TaskCode
                        + "&man=" + MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName
                        + "&time=" + Uri.encode(BaseClassUtil.getSystemTime());

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
            protected void onSuccess(ResultWithoutData resultWithoutData) {

                if (resultWithoutData == null) {
                    Toast.makeText(AddHaoCaiActivity.this, "添加材料失败", Toast.LENGTH_SHORT).show();
                } else if (resultWithoutData.ResultCode != 200) {
                    Toast.makeText(AddHaoCaiActivity.this,
                            BaseClassUtil.isNullOrEmptyString(resultWithoutData.ResultMessage) ? "添加材料失败" : resultWithoutData.ResultMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddHaoCaiActivity.this, "添加材料成功", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);

                    // 领料成功后关闭界面
                    finish();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

}


