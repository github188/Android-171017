package com.repair.zhoushan.module.tablecommonhand;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.GDGroup;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.BaseDialogActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by lyunfan on 16/10/25.
 */
public class TableOneRecordDialog extends BaseDialogActivity {
    protected TableMode tableMode = new TableMode();

    protected List<FlowNodeMeta> mFlowNodeMetas = null;
    private String getViewDataUrl;
    private boolean isRead = true;

    @Override
    protected void initView() {

        handViewMode();
        createView();
    }

    @Override
    protected void handleOkEvent(String tag, List<FeedItem> feedItemList) {
        switch (tableMode.viewMode) {
            case 1: {
                break;
            }
            case 2: {
                editData(feedItemList);
                break;
            }
            case 3: {
                deleteData();
                break;

            }
            case 4: {
                reportData(feedItemList);
                break;
            }
        }
    }

    protected ReportInBackEntity getReportBackEntity(String url, List<FeedItem> feedbackItems) {

        if (feedbackItems == null) {
            return null;
        }

        if (mFlowNodeMetas == null) {
            return null;
        }

        if (mFlowNodeMetas.size() == 0) {
            return null;
        }

        ArrayList<FlowNodeMeta.TableValue> values = mFlowNodeMetas.get(0).Values;
        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedbackItems) {
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                    break;
                }
            }
        }

        // 将对信息转换为JSON字符串
        String reportData = new Gson().toJson(mFlowNodeMetas.get(0));

        // 将所有信息封装成后台上传的数据模型
        return new ReportInBackEntity(
                reportData,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                url,
                UUID.randomUUID().toString(),
                tableMode.tableName,
                mFlowBeanFragment.getAbsolutePaths(),
                mFlowBeanFragment.getRelativePaths());

    }


    protected void reportData(List<FeedItem> feedItemList) {

        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/");
        sb.append("SaveTableDataInfo?tableName=" + tableMode.tableName);

        ReportInBackEntity entity = getReportBackEntity(sb.toString(), feedItemList);

        if (entity == null) {
            return;
        }

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {

                        Toast.makeText(TableOneRecordDialog.this, "添加成功!", Toast.LENGTH_SHORT).show();

                        onSuccessV2();

                    } else {
                        Toast.makeText(TableOneRecordDialog.this, "添加失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);

    }

    protected void editData(List<FeedItem> feedItemList) {
//        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
//        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/");
//        sb.append("EditTableData?tableName=" + tableMode.tableName + "&id=" + tableMode.ID);
//
//        ReportInBackEntity entity = getReportBackEntity(sb.toString(),feedItemList);
//
//        if (entity == null) {
//            return;
//        }
//
//        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
//            @Override
//            public void doAfter(ResultData<Integer> result) {
//                try {
//                    if (result.ResultCode == 200) {
//
//                        Toast.makeText(TableOneRecordDialog.this, "编辑成功!", Toast.LENGTH_SHORT).show();
//
//                        isReportSuccess = true;
//                        mFlowBeanFragment.deleteCacheData(MyApplication.getInstance().getUserId(), cacheKey);
//
//                        onSuccessV2();
//
//                    } else {
//                        Toast.makeText(TableOneRecordDialog.this, "编辑失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }).mmtExecute(entity);
    }

    protected void deleteData() {

//        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("确定删除");
//        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
//            @Override
//            public void onRightButtonClick(View view) {
//                final StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
//                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/");
//                sb.append("DeleteTableData?tableName=" + tableMode.tableName + "&id=" + tableMode.ID);
//
//                new MmtBaseTask<Void, Void, String>(TableOneRecordDialog.this) {
//                    @Override
//                    protected String doInBackground(Void... params) {
//
//                        return NetUtil.executeHttpGet(sb.toString());
//
//                    }
//
//                    @Override
//                    protected void onSuccess(String s) {
//                        super.onSuccess(s);
//                        ResultData<Integer> tableResult = Utils.json2ResultDataActivity(Integer.class,
//                                TableOneRecordDialog.this, s, "删除失败", false);
//                        if (tableResult == null) return;
//                        MyApplication.getInstance().showMessageWithHandle("删除成功");
//
//                        backByReorder(true);
//                    }
//                }.mmtExecute();
//            }
//        });
//
//        okCancelDialogFragment.show(this.getSupportFragmentManager(), "");

    }

    protected void handViewData() {

        if (mFlowNodeMetas == null) {
            return;
        }

        if (mFlowNodeMetas.size() == 0) {
            return;
        }

        List<GDFormBean> gdFormBeans = new ArrayList<>();

        for (FlowNodeMeta flowNodeMeta : mFlowNodeMetas) {
            gdFormBeans.add(flowNodeMeta.mapToGDFormBean(isRead));
        }

        if (gdFormBeans.size() == 0) {
            return;
        }

        List<GDGroup> groups = new ArrayList<>();

        for (int i = 0; i < gdFormBeans.size(); i++) {

            GDFormBean gdFormBean1 = gdFormBeans.get(i);
            groups.addAll(Arrays.asList(gdFormBean1.Groups));

        }

        mGDFormBean = new GDFormBean();
        mGDFormBean.Groups = groups.toArray(new GDGroup[groups.size()]);

        super.initView();

    }

    protected void handViewMode() {

        Intent intent = getIntent();

        if (TextUtils.isEmpty(tableMode.tableName)) {
            tableMode.tableName = intent.getStringExtra("tableName");
        }
        if (TextUtils.isEmpty(tableMode.tableName)) {
            MyApplication.getInstance().showMessageWithHandle("表名不能为空");
            return;
        }

        if (tableMode.viewMode < 0 || tableMode.viewMode > 4) {
            tableMode.viewMode = intent.getIntExtra("viewMode", -1);
        }
        if (tableMode.viewMode < 0 || tableMode.viewMode > 4) {
            MyApplication.getInstance().showMessageWithHandle("表模式错误");
            return;
        }

        if (TextUtils.isEmpty(tableMode.uiGroup)) {
            tableMode.uiGroup = intent.getStringExtra("uiGroup");
        }
        if (TextUtils.isEmpty(tableMode.uiGroup)) {
            tableMode.uiGroup = "";
        }

        if (tableMode.viewMode == TabltViewMode.READ.getTableViewMode() || tableMode.viewMode == TabltViewMode.EDIT.getTableViewMode() || tableMode.viewMode == TabltViewMode.DELETE.getTableViewMode()) {
            if (tableMode.ID <= 0) {
                tableMode.ID = intent.getIntExtra("ID", 0);
            }
            if (tableMode.ID <= 0) {
                String ID = intent.getStringExtra("ID");
                if (!TextUtils.isEmpty(ID)) {
                    tableMode.ID = Integer.valueOf(ID);
                }
            }

            if (tableMode.ID <= 0) {
                if (TextUtils.isEmpty(tableMode.key)) {
                    tableMode.key = intent.getStringExtra("key");
                }
                if (TextUtils.isEmpty(tableMode.key)) {
                    MyApplication.getInstance().showMessageWithHandle("非上报模式下必须明确操作的是那条纪录");
                    return;
                }
                if (TextUtils.isEmpty(tableMode.value)) {
                    tableMode.value = intent.getStringExtra("value");
                }

                if (TextUtils.isEmpty(tableMode.value)) {
                    MyApplication.getInstance().showMessageWithHandle("非上报模式下必须明确操作的是那条纪录");
                    return;
                }

            } else {
                tableMode.key = "id";
                tableMode.value = String.valueOf(tableMode.ID);
            }

            if (TextUtils.isEmpty(tableMode.value)) {
                MyApplication.getInstance().showMessageWithHandle("非上报模式下必须明确操作的是那条纪录");
                return;
            }
            if (BaseClassUtil.isNum(tableMode.value)) {
                int value = Integer.parseInt(tableMode.value);
                if (value <= 0) {
                    MyApplication.getInstance().showMessageWithHandle("非上报模式下必须明确操作的是那条纪录");
                    return;
                }
            }
        }

        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/");

        switch (tableMode.viewMode) {
            case 1: {
                getViewDataUrl = sb.toString() + "GetTableDataInfo?tableName=" + tableMode.tableName + "&key=" + tableMode.key + "&value=" + tableMode.value;
                isRead = true;
                break;
            }
            case 2: {
                getViewDataUrl = sb.toString() + "GetTableDataInfo?tableName=" + tableMode.tableName + "&key=" + tableMode.key + "&value=" + tableMode.value;
                isRead = false;
                break;
            }
            case 3: {
                getViewDataUrl = sb.toString() + "GetTableDataInfo?tableName=" + tableMode.tableName + "&key=" + tableMode.key + "&value=" + tableMode.value;
                isRead = true;
                break;

            }
            case 4: {
                getViewDataUrl = sb.toString() + "GetTableGroupMeta?tableName=" + tableMode.tableName + "&uiGroup=" + tableMode.uiGroup;
                isRead = false;
                break;
            }
        }
    }

    protected void createView() {
        if (TextUtils.isEmpty(getViewDataUrl)) {
            return;
        }
        new MmtBaseTask<Void, Void, String>(this) {
            @Override
            protected String doInBackground(Void... params) {
                return NetUtil.executeHttpGet(getViewDataUrl);
            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);

                ResultData<FlowNodeMeta> resultData = Utils.json2ResultDataToast(FlowNodeMeta.class, context, s, "网络不通", false);
                if (resultData == null) {
                    return;
                }
                mFlowNodeMetas = resultData.DataList;

                handViewData();
            }
        }.mmtExecute();
    }
}
