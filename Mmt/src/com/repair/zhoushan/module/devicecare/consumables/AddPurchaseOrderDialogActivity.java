package com.repair.zhoushan.module.devicecare.consumables;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.BaseDialogActivity;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.entity.CaiGouOrderBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.GongChangBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.GongSiBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.QiYeBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.SAPBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class AddPurchaseOrderDialogActivity extends BaseDialogActivity {

    private List<CostCenter> costCenterList = new LinkedList<CostCenter>();
    private List<String> costCenterShowList = new LinkedList<String>();

    // key:公司; value:供应商列表
    private static HashMap<String, List<SAPBean>> companySupplierList = new HashMap<String, List<SAPBean>>();

    private ImageButtonView costCenterView;
    private ImageButtonView supplierView;

    private CostCenter curCostCenter;
    private List<SAPBean> curSupplierList;
    private List<String> curSupplierShowList = new LinkedList<String>();
    private SAPBean curSupplier;

    private ScheduleTask mScheduleTask;

    // 是否包含成本中心: 1. 包含：成本中心-供应商; 2. 不包含：公司-供应商.
    private boolean isExistCostCenter;

    @Override
    protected void onViewCreated() {
        super.onViewCreated();

        final Intent outerIntent = getIntent();
        this.isExistCostCenter = outerIntent.getBooleanExtra("IsExistCostCenter", false);
        this.mScheduleTask = outerIntent.getParcelableExtra("ListItemEntity");

        String additionalData = outerIntent.getStringExtra("AdditionalData");
        if (!TextUtils.isEmpty(additionalData)) {
            // 企业
            QiYeBean enterprise = new Gson().fromJson(additionalData, new TypeToken<QiYeBean>() {
            }.getType());

            CostCenter costCenter;
            if (isExistCostCenter) {

                for (GongSiBean gongSi : enterprise.GongSiList) {
                    for (GongChangBean gongChang : gongSi.GongChangList) {
                        for (SAPBean sapBean : gongChang.ChenBenCenterList) {
                            costCenter = new CostCenter(sapBean);
                            costCenter.company = gongSi;
                            costCenter.factory = gongChang;
                            costCenterList.add(costCenter);
                            costCenterShowList.add(costCenter.getName());
                        }
                    }
                }
            } else {

                for (GongSiBean gongSi : enterprise.GongSiList) {
                    for (GongChangBean gongChang : gongSi.GongChangList) {
                        // 复用'CostCenter'结构用以暂存工厂信息
                        costCenter = new CostCenter();
                        costCenter.company = gongSi;
                        costCenter.factory = gongChang;
                        costCenterList.add(costCenter);
                        costCenterShowList.add(costCenter.factory.getName());
                    }
                }
            }
        }

        FlowBeanFragment flowBeanFragment = getFlowBeanFragment();
        costCenterView = (ImageButtonView) flowBeanFragment.findViewByName("costCenter");
        supplierView = (ImageButtonView) flowBeanFragment.findViewByName("supplier");

        supplierView.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (curCostCenter == null) {
                    Toast.makeText(AddPurchaseOrderDialogActivity.this,
                            (isExistCostCenter ? "请先选择成本中心" : "请先选择工厂"), Toast.LENGTH_SHORT).show();
                    return;
                }

                curSupplierList = companySupplierList.get(curCostCenter.company.getCode());

                if (curSupplierList != null && curSupplierList.size() > 0) {

                    curSupplierShowList.clear();
                    for (SAPBean sapBean : curSupplierList) {
                        curSupplierShowList.add(sapBean.getName());
                    }

                    if (true) {

                        Intent intent = new Intent(AddPurchaseOrderDialogActivity.this, AddPurchaseOrderChooseSupplierActivity.class);
                        ArrayList<String> paramData = new ArrayList<String>();
                        paramData.addAll(curSupplierShowList);
                        intent.putStringArrayListExtra("CostCenterData", paramData);
                        startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
                    } else {

                        ListDialogFragment fragment = new ListDialogFragment("供应商", curSupplierShowList);
                        fragment.show(getSupportFragmentManager(), "");
                        fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {

                            @Override
                            public void onListItemClick(int arg2, String value) {

                                supplierView.setValue(value);
                                curSupplier = curSupplierList.get(arg2);
                            }
                        });
                    }

                } else {

                    Toast.makeText(AddPurchaseOrderDialogActivity.this, "未获取到对应的供应商数据", Toast.LENGTH_SHORT).show();
                }
            }
        });

        costCenterView.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ListDialogFragment fragment;

                fragment = new ListDialogFragment(isExistCostCenter ? "成本中心" : "工厂", costCenterShowList);
                fragment.show(getSupportFragmentManager(), "");
                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {

                    private String curValue;

                    @Override
                    public void onListItemClick(int arg2, String value) {
                        curValue = costCenterView.getValue();
                        if (value.equals(curValue))
                            return;

                        costCenterView.setValue(value);
                        curValue = value;

                        curCostCenter = costCenterList.get(arg2);
                        getSupplierList(curCostCenter.company.getCode());
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.DEFAULT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String result = data.getStringExtra("RESULT");
            int index = data.getIntExtra("INDEX", 0);

            supplierView.setValue(result);
            curSupplier = curSupplierList.get(index);
        }
    }

    private void getSupplierList(final String companyCode) {

        if (!companySupplierList.containsKey(companyCode)) {

            MmtBaseTask<String, Void, ResultData<SAPBean>> mmtBaseTask = new MmtBaseTask<String, Void, ResultData<SAPBean>>(AddPurchaseOrderDialogActivity.this) {
                @Override
                protected ResultData<SAPBean> doInBackground(String... params) {

                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/FetchGongYinShangByGSCode"
                            + "?gsCode=" + params[0];

                    String result = NetUtil.executeHttpGet(url);

                    if (TextUtils.isEmpty(result)) {
                        return null;
                    }

                    return new Gson().fromJson(result, new TypeToken<ResultData<SAPBean>>() {
                    }.getType());
                }

                @Override
                protected void onSuccess(ResultData<SAPBean> resultData) {

                    String defErrMsg = "获取供应商信息失败";

                    if (resultData == null) {
                        Toast.makeText(AddPurchaseOrderDialogActivity.this, defErrMsg, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (resultData.ResultCode != 200) {
                        Toast.makeText(AddPurchaseOrderDialogActivity.this,
                                TextUtils.isEmpty(resultData.ResultMessage) ? defErrMsg : resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (resultData.DataList.size() == 0) {
                        Toast.makeText(AddPurchaseOrderDialogActivity.this, "未查询到供应商信息", Toast.LENGTH_SHORT).show();
                    }

                    companySupplierList.put(companyCode, resultData.DataList);
                }
            };
            mmtBaseTask.setCancellable(false);
            mmtBaseTask.mmtExecute(companyCode);
        }
    }

    @Override
    protected void handleOkEvent(String title, final List<FeedItem> feedItemList) {

        MmtBaseTask<Void, Void, ResultWithoutData> mmtBaseTask = new MmtBaseTask<Void, Void, ResultWithoutData>(AddPurchaseOrderDialogActivity.this) {

            @Override
            protected ResultWithoutData doInBackground(Void... params) {
                ResultWithoutData data;

                CaiGouOrderBean purchaseOrder = new CaiGouOrderBean();

                purchaseOrder.CaseNo = mScheduleTask.TaskCode;
                purchaseOrder.FlowCode = mScheduleTask.PreCodeFormat;
                purchaseOrder.OperType = "生成采购订单";
                purchaseOrder.OperTime = BaseClassUtil.getSystemTime();
                purchaseOrder.OperMan = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

                purchaseOrder.GongSi = curCostCenter.company;
                purchaseOrder.GongChang = curCostCenter.factory;
                purchaseOrder.GongYinShang = curSupplier;
                if (isExistCostCenter) {
                    purchaseOrder.ChenBenCenter = new SAPBean(curCostCenter);
                } else {
                    purchaseOrder.ChenBenCenter = new SAPBean();
                }

                for (FeedItem feedItem : feedItemList) {
                    switch (feedItem.Name) {
                        case "amount":
                            purchaseOrder.Num = Double.parseDouble(feedItem.Value);
                            break;
                        case "taxCode":
                            purchaseOrder.ShuiMa = feedItem.Value;
                            break;
                    }
                }

                String dataStr = new Gson().toJson(purchaseOrder, CaiGouOrderBean.class);

                // 执行网络操作
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/AddCaiGouOrder";

                try {

                    String result = NetUtil.executeHttpPost(url, dataStr,
                            "Content-Type", "application/json; charset=utf-8");

                    if (TextUtils.isEmpty(result)) {
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
                    Toast.makeText(AddPurchaseOrderDialogActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                } else if (resultWithoutData.ResultCode != 200) {
                    Toast.makeText(AddPurchaseOrderDialogActivity.this,
                            TextUtils.isEmpty(resultWithoutData.ResultMessage) ? "添加失败" : resultWithoutData.ResultMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddPurchaseOrderDialogActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);

                    AddPurchaseOrderDialogActivity.this.onSuccess();
                }

            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private final class CostCenter extends SAPBean {

        public CostCenter() {
        }

        public CostCenter(SAPBean sapBean) {
            super(sapBean.getCode(), sapBean.getName());
        }

        public SAPBean company;
        public SAPBean factory;
    }

}
