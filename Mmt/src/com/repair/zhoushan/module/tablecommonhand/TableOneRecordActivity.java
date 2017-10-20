package com.repair.zhoushan.module.tablecommonhand;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowNodeMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by liuyunfan on 2016/7/15.
 * <p/>
 * 表中的某条记录增删改查
 * 界面架构：FlowNodeMeta
 */
public class TableOneRecordActivity extends FlowNodeMetaActivity {

    protected TableMode tableMode = new TableMode();

    protected String optTblBtnText = "";

    protected String cacheKey = "确定";

    protected ValidateListener validateListener;

    protected void setValidateListener(ValidateListener validateListener) {
        this.validateListener = validateListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        handViewMode();

        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initView() {

        GDFormBean gdFormBean = getGdFormBean();

        if (gdFormBean == null) {
            return;
        }

        flowBeanFragment.setFilterCriteria(maintenanceFeedBacks);

        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", gdFormBean);

        if (tableMode.viewMode == TabltViewMode.READ.getTableViewMode()) {

            if (!TextUtils.isEmpty(cacheKey)) {
                args.putString("CacheSearchParam", ("userId=" + MyApplication.getInstance().getUserId() + " and key='" + cacheKey + "'"));

            }
        }

        flowBeanFragment.setArguments(args);

        addFragment(flowBeanFragment);

        String title = getIntent().getStringExtra("title");
        if (TextUtils.isEmpty(title)) {
            title = tableMode.tableName;
        }
        setTitleAndClear(title);
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

        if (tableMode.viewMode < 0 || tableMode.viewMode > 5) {
            tableMode.viewMode = intent.getIntExtra("viewMode", -1);
        }
        if (tableMode.viewMode <= 0 || tableMode.viewMode > 5) {
            MyApplication.getInstance().showMessageWithHandle("表模式错误");
            return;
        }

        if (TextUtils.isEmpty(tableMode.uiGroup)) {
            tableMode.uiGroup = intent.getStringExtra("uiGroup");
        }
        if (TextUtils.isEmpty(tableMode.uiGroup)) {
            tableMode.uiGroup = "";
        }

        if (tableMode.viewMode!=TabltViewMode.REPORT.getTableViewMode()) {
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
                optTblBtnText = "";
                break;
            }
            case 2: {
                getViewDataUrl = sb.toString() + "GetTableDataInfo?tableName=" + tableMode.tableName + "&key=" + tableMode.key + "&value=" + tableMode.value;
                isRead = false;
                optTblBtnText = "保存";
                break;
            }
            case 3: {
                getViewDataUrl = sb.toString() + "GetTableDataInfo?tableName=" + tableMode.tableName + "&key=" + tableMode.key + "&value=" + tableMode.value;
                isRead = true;
                optTblBtnText = "删除";
                break;

            }
            case 4: {
                getViewDataUrl = sb.toString() + "GetTableGroupMeta?tableName=" + tableMode.tableName + "&uiGroup=" + tableMode.uiGroup;
                isRead = false;
                optTblBtnText = "上报";
                break;
            }
            case 5: {
                getViewDataUrl = sb.toString() + "GetTableDataInfo?tableName=" + tableMode.tableName + "&key=" + tableMode.key + "&value=" + tableMode.value;
                isRead = false;
                optTblBtnText = "保存,删除";
                break;
            }
        }
    }

    @Override
    protected void setBottonBtn() {
        if (tableMode.viewMode == TabltViewMode.READ.getTableViewMode()) {
            return;
        }

        String[] btnArr = optTblBtnText.split(",");
        for (final String btnTxt : btnArr) {
            BottomUnitView manageUnitView = new BottomUnitView(TableOneRecordActivity.this);
            manageUnitView.setContent(btnTxt);
            manageUnitView.setImageResource(R.drawable.handoverform_report);
            addBottomUnitView(manageUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    submit(btnTxt);

                }
            });
        }

    }

    private void submit(String btnTxt) {
        switch (btnTxt) {
            case "": {
                break;
            }
            case "保存": {
                editData();
                break;
            }
            case "删除": {
                deleteData();
                break;

            }
            case "上报": {
                reportData();
                break;
            }
        }
    }

    protected ReportInBackEntity getReportBackEntity(String url) {
        List<FeedItem> feedbackItems = flowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

        if (feedbackItems == null) {
            showErrorMsg("界面值获取错误");
            return null;
        }

        if (mFlowNodeMetas == null) {
            showErrorMsg("界面值获取错误");
            return null;
        }

        if (mFlowNodeMetas.size() == 0) {
            showErrorMsg("界面值获取错误");
            return null;
        }

        if (validateListener != null) {
            boolean isValidated = validateListener.isValidated(feedbackItems);

            if (!isValidated) {
                return null;
            }
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
                flowBeanFragment.getAbsolutePaths(),
                flowBeanFragment.getRelativePaths());

    }

    protected void reportData() {

        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/");
        sb.append("SaveTableDataInfo?tableName=" + tableMode.tableName);

        ReportInBackEntity entity = getReportBackEntity(sb.toString());

        if (entity == null) {
            return;
        }

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {

                        Toast.makeText(TableOneRecordActivity.this, "添加成功!", Toast.LENGTH_SHORT).show();

                        backByReorder(true);

                    } else {
                        Toast.makeText(TableOneRecordActivity.this, "添加失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);

    }

    protected void editData() {
        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/");
        sb.append("EditTableData?tableName=" + tableMode.tableName + "&id=" + tableMode.ID);

        ReportInBackEntity entity = getReportBackEntity(sb.toString());

        if (entity == null) {
            return;
        }

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {

                        Toast.makeText(TableOneRecordActivity.this, "编辑成功!", Toast.LENGTH_SHORT).show();

                        isReportSuccess = true;
                        flowBeanFragment.deleteCacheData(MyApplication.getInstance().getUserId(), cacheKey);

                        backByReorder(true);

                    } else {
                        Toast.makeText(TableOneRecordActivity.this, "编辑失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

    protected void deleteData() {

        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("确定删除");
        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
            @Override
            public void onRightButtonClick(View view) {
                final StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/");
                sb.append("DeleteTableData?tableName=" + tableMode.tableName + "&id=" + tableMode.ID);

                new MmtBaseTask<Void, Void, String>(TableOneRecordActivity.this) {
                    @Override
                    protected String doInBackground(Void... params) {

                        return NetUtil.executeHttpGet(sb.toString());

                    }

                    @Override
                    protected void onSuccess(String s) {
                        super.onSuccess(s);
                        ResultData<Integer> tableResult = Utils.json2ResultDataActivity(Integer.class,
                                TableOneRecordActivity.this, s, "删除失败", false);
                        if (tableResult == null) return;
                        MyApplication.getInstance().showMessageWithHandle("删除成功");

                        backByReorder(true);
                    }
                }.mmtExecute();
            }
        });

        okCancelDialogFragment.show(this.getSupportFragmentManager(), "");

    }

    private boolean isReportSuccess = false;

    @Override
    protected void onPause() {
        super.onPause();

        if (isReportSuccess) {
            return;
        }

        if (TextUtils.isEmpty(cacheKey)) {
            return;
        }

        if (tableMode.viewMode != TabltViewMode.REPORT.getTableViewMode()) {
            return;
        }

        if (flowBeanFragment == null) {
            return;
        }

        flowBeanFragment.saveCacheData(MyApplication.getInstance().getUserId(), cacheKey);
    }


    public interface ValidateListener {
        boolean isValidated(List<FeedItem> feedbackItems);
    }
}
