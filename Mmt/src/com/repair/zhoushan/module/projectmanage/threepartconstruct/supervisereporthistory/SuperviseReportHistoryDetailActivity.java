package com.repair.zhoushan.module.projectmanage.threepartconstruct.supervisereporthistory;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;

public class SuperviseReportHistoryDetailActivity extends BaseActivity {

    protected FlowBeanFragment formBeanFragment;
    private ConstructSupervision formData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        formData = getIntent().getParcelableExtra("ConstructSupervision");

        initView();
    }

    private void initView() {

        new MmtBaseTask<String, Void, String>(this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
            @Override
            public void doAfter(String result) {
                if (BaseClassUtil.isNullOrEmptyString(result)) {
                    SuperviseReportHistoryDetailActivity.this.showErrorMsg("未正确获取信息");
                    return;
                }

                Results<FlowNodeMeta> rawData = new Gson().fromJson(result, new TypeToken<Results<FlowNodeMeta>>() {
                }.getType());

                if (rawData == null || rawData.getMe == null || rawData.getMe.size() <= 0) {
                    SuperviseReportHistoryDetailActivity.this.showErrorMsg(rawData.say.errMsg);
                    return;
                }
                FlowNodeMeta flowNodeMeta = rawData.getMe.get(0);

                // TODO: 2015/12/12 数据映射到结构上去
                for (FlowNodeMeta.TableValue tableValue : flowNodeMeta.Values) {
                    switch (tableValue.FieldName) {
                        case "施工进度" :
                            tableValue.FieldValue = formData.Progress;
                            break;
                        case "施工是否正常" :
                            tableValue.FieldValue = formData.IsNormal;
                            break;
                        case "施工动态" :
                            tableValue.FieldValue = formData.DynamicState;
                            break;
                        case "现场照片" :
                            tableValue.FieldValue = formData.Picture;
                            break;
                        case "施工点与管道距离" :
                            tableValue.FieldValue = formData.Distance;
                            break;
                    }
                }

                createView(flowNodeMeta.mapToGDFormBean());
            }
        }) {
            @Override
            protected String doInBackground(String... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableGroupMeta";

                String jsonStr = NetUtil.executeHttpGet(url, "tableName", params[0], "uiGroup", params[1]);
                return jsonStr;
            }
        }.mmtExecute("第三方施工监管表", "日常监管");
    }

    protected void createView(final GDFormBean formBean) {

        // 将Fragment显示在界面上
        formBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", formBean);
        formBeanFragment.setArguments(args);

        formBeanFragment.setCls(SuperviseReportHistoryDetailActivity.class);
        formBeanFragment.setAddEnable(false);
        formBeanFragment.setFormOnlyShow();
        formBeanFragment.setBeanFragmentOnCreate(new FlowBeanFragment.BeanFragmentOnCreate() {
            @Override
            public void onCreated() {
//                if (formBean.hasGroupName("办理过程")) {
//                    showFetchCaseProcedure("办理过程");
//                }
            }
        });

        addFragment(formBeanFragment);
    }

}
