package com.repair.zhoushan.module.devicecare.platfromadd;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.module.BaseDialogActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/7/7.
 */
public class AddTaskActivity extends BaseDialogActivity {

    private int ID = -1;
    private String bizType = "";
    private String gisCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            Intent intent = getIntent();

            bizType = intent.getStringExtra("bizType");

            if (TextUtils.isEmpty(bizType)) {
                MyApplication.getInstance().showMessageWithHandle("业务类型异常");
                return;
            }

            ID = intent.getIntExtra("ID", -1);

            if (ID == -1) {
                MyApplication.getInstance().showMessageWithHandle("ID异常");
                return;
            }

            gisCode = intent.getStringExtra("GisCode");

            GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                    new String[]{"DisplayName", "GIS编号", "Name", "gisCode", "Type", "短文本", "Value", gisCode, "ConfigInfo", "", "Validate", "0", "IsRead", "true"},
                    new String[]{"DisplayName", "养护类型", "Name", "keepType", "Type", "值选择器", "Validate", "0"},
                    new String[]{"DisplayName", "执行人", "Name", "userName", "Type", "短文本", "Value", MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName, "Validate", "1", "IsRead", "true"},
                    new String[]{"DisplayName", "计划开始时间", "Name", "dateFrom", "Type", "日期框", "Validate", "1"},
                    new String[]{"DisplayName", "计划结束时间", "Name", "dateEnd", "Type", "日期框", "Validate", "1"},
                    new String[]{"DisplayName", "备注", "Name", "remark", "Type", "长文本", "Validate", "0"});
            getIntent().putExtra("GDFormBean", gdFormBean);
            getIntent().putExtra("Title", "临时任务");
        } catch (Exception ex) {
            MyApplication.getInstance().showMessageWithHandle(ex.getMessage());
        } finally {
            super.onCreate(savedInstanceState);
        }
    }

    @Override
    protected void handleOkEvent(String tag, final List<FeedItem> feedItemList) {

        if (feedItemList == null) {
            return;
        }

        new MmtBaseTask<Void, Void, String>(this) {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String url = ServerConnectConfig.getInstance().getBaseServerPath() +
                            "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/" + MyApplication.getInstance().getUserId() + "/TentativeInfoSave";
                    return NetUtil.executeHttpPost(url, getReportData(feedItemList));
                } catch (Exception ex) {
                    return null;
                }
            }

            @Override
            protected void onSuccess(String s) {
                boolean result = Utils.json2ResultToast(context, s, "网络异常");
                if (result) {
                    MyApplication.getInstance().showMessageWithHandle("提交成功");
                    AppManager.finishActivity();
                }
            }

        }.mmtExecute();

    }

    @Override
    protected void onViewCreated() {

        if (TextUtils.isEmpty(gisCode)) {
            View gisCodeView = getFlowBeanFragment().findViewByName("gisCode");
            if (gisCodeView != null) {
                gisCodeView.setVisibility(View.GONE);
            }
        }

        new MmtBaseTask<Void, Void, String>(this) {
            @Override
            protected String doInBackground(Void... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath() +
                        "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetMaintenancePlanCureType?bizName=" + bizType;
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String s) {

                final ResultData<String> result = Utils.json2ResultDataToast(String.class, AddTaskActivity.this, s, "网络异常", true);

                if (result == null) {
                    return;
                }

                View keepTypeView = getFlowBeanFragment().findViewByName("keepType");

                if (keepTypeView instanceof ImageButtonView) {

                    if (result.DataList.size() == 0) {
                        keepTypeView.setVisibility(View.GONE);
                        return;
                    }

                    final ImageButtonView imageButtonView = (ImageButtonView) keepTypeView;
                    imageButtonView.setValue(result.getSingleData());

                    imageButtonView.getButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ListDialogFragment fragment = new ListDialogFragment("养护类型", result.DataList);
                            fragment.show(AddTaskActivity.this.getSupportFragmentManager(), "");
                            fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                                @Override
                                public void onListItemClick(int arg2, String value) {
                                    imageButtonView.setValue(value);
                                }
                            });
                        }
                    });
                }
            }
        }.mmtExecute();
    }

    private String getReportData(List<FeedItem> feedItemList) {
        List<TentativeInfoSave> datas = new ArrayList<>();
        TentativeInfoSave data = new TentativeInfoSave();
        data.userID = String.valueOf(MyApplication.getInstance().getUserId());
        data.bizType = bizType;
        data.gisCode = gisCode;
        data.ID = String.valueOf(ID);
        data.userName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

        for (FeedItem fi : feedItemList) {
            if (fi.Name.equals("keepType")) {
                data.keepType = fi.Value;
                continue;
            }
            if (fi.Name.equals("dateFrom")) {
                data.dateFrom = fi.Value;
                continue;
            }
            if (fi.Name.equals("dateEnd")) {
                data.dateEnd = fi.Value;
                continue;
            }
            if (fi.Name.equals("remark")) {
                data.remark = fi.Value;
                continue;
            }
        }
        datas.add(data);
        return new Gson().toJson(datas);

    }
}