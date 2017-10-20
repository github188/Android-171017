package com.repair.zhoushan.module.devicecare.consumables;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.entity.QiYeBean;

public class PurchaseOrderListActivity extends BaseActivity {

    private PurchaseOrderListFragment mPurchaseOrderListFragment;

    private ScheduleTask mScheduleTask;

    // 企业
    private QiYeBean enterprise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");

        getBaseTextView().setText("采购订单");

        createView();
        createBottomView();

    }

    private void createView() {
        this.mPurchaseOrderListFragment = new PurchaseOrderListFragment();

        Bundle args = new Bundle();
        args.putParcelable("ListItemEntity", mScheduleTask);
        mPurchaseOrderListFragment.setArguments(args);
        addFragment(mPurchaseOrderListFragment);
    }

    private void createBottomView() {

        BottomUnitView addUnitView = new BottomUnitView(PurchaseOrderListActivity.this);
        addUnitView.setContent("添加");
        addUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(addUnitView, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getFormData();
            }
        });
    }

    private void getFormData() {

        if (enterprise == null) {
            MmtBaseTask<String, Void, ResultData<QiYeBean>> fetchInitDataTask = new MmtBaseTask<String, Void, ResultData<QiYeBean>>(PurchaseOrderListActivity.this) {
                @Override
                protected ResultData<QiYeBean> doInBackground(String... params) {
                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/FetchWuLiaoChenBenList";

                    String result = NetUtil.executeHttpGet(url, "userID", params[0], "gcCode", params[1]);

                    if (TextUtils.isEmpty(result)) {
                        return null;
                    }
                    ResultData<QiYeBean> resultData = new Gson().fromJson(result, new TypeToken<ResultData<QiYeBean>>() {
                    }.getType());

                    return resultData;
                }

                @Override
                protected void onSuccess(ResultData<QiYeBean> resultData) {
                    String defErrMsg = "获取成本中心信息失败";

                    if (resultData == null) {
                        Toast.makeText(PurchaseOrderListActivity.this, defErrMsg, Toast.LENGTH_SHORT).show();

                    } else if (resultData.ResultCode != 200) {
                        Toast.makeText(PurchaseOrderListActivity.this,
                                TextUtils.isEmpty(resultData.ResultMessage) ? defErrMsg : resultData.ResultMessage, Toast.LENGTH_LONG).show();
                    } else {
                        enterprise = resultData.getSingleData();

                        generateForm();
                    }
                }
            };
            fetchInitDataTask.setCancellable(false);
            fetchInitDataTask.mmtExecute(String.valueOf(MyApplication.getInstance().getUserId()), "");

        } else {
            generateForm();
        }

    }

    private void generateForm() {

        boolean isExistCostCenter = mPurchaseOrderListFragment.isExistCostCenter();

        GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                new String[]{"DisplayName", isExistCostCenter ? "成本中心" : "工厂", "Name", "costCenter", "Type", "值选择器", "ConfigInfo", "", "Validate", "1"},
                new String[]{"DisplayName", "供应商", "Name", "supplier", "Type", "值选择器", "ConfigInfo", "", "Validate", "1"},
                new String[]{"DisplayName", "金额", "Name", "amount", "Type", "短文本", "ConfigInfo", "请填入金额", "ValidateRule", "number", "Validate", "1", "Unit", "元"},
                new String[]{"DisplayName", "税码", "Name", "taxCode", "Type", "选择器", "ConfigInfo", "税码", "Validate", "1"});

        Intent intent = new Intent(PurchaseOrderListActivity.this, AddPurchaseOrderDialogActivity.class);
        intent.putExtra("Title", "采购订单添加");
        intent.putExtra("GDFormBean", gdFormBean);
        intent.putExtra("AdditionalData", new Gson().toJson(enterprise));
        intent.putExtra("IsExistCostCenter", isExistCostCenter);
        intent.putExtra("ListItemEntity" ,mScheduleTask);

        startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.DEFAULT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mPurchaseOrderListFragment.getPurchaseListData();
        }
    }
}