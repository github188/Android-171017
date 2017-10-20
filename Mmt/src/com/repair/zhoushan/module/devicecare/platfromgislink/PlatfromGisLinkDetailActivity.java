package com.repair.zhoushan.module.devicecare.platfromgislink;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.devicecare.TableColumn;

import java.util.List;

/**
 * Created by liuyunfan on 2016/3/22.
 */
public class PlatfromGisLinkDetailActivity extends BaseActivity {
    // 界面结构数据
    private FlowNodeMeta mFlowNodeMeta;
    //界面值
    private List<FlowNodeMeta.TableValue> tableValues;
    FlowBeanFragment mFlowBeanFragment;
    String table;
    String filter;
    DeviceModel deviceModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        deviceModel = new Gson().fromJson(intent.getStringExtra("DeviceModel"), DeviceModel.class);
        setTitleAndClear("设备详情");
        if (deviceModel != null) {
            table = deviceModel.AccountTable;

            filter ="ID="+deviceModel.ID;

            createView();
        } else {
            Toast.makeText(this, "deviceModel不存在", Toast.LENGTH_SHORT).show();
        }
    }

    void createView() {
        new MmtBaseTask<String, Void, String[]>(PlatfromGisLinkDetailActivity.this, true) {

            @Override
            protected String[] doInBackground(String... params) {
                String[] results = new String[2];
                // 界面结构
                String url1 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableGroupMeta";

                results[0] = NetUtil.executeHttpGet(url1, "tableName", table, "uiGroup", "");

                // 界面值
                String url2 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableValuesByFilter";

                results[1] = NetUtil.executeHttpGet(url2, "tableName", table, "filter", filter);


                return results;
            }

            @Override
            protected void onSuccess(String[] results) {

                ResultData<FlowNodeMeta> tableResult = Utils.json2ResultDataToast(FlowNodeMeta.class,
                        PlatfromGisLinkDetailActivity.this, results[0], "获取界面结构失败", false);
                if (tableResult == null) return;
                mFlowNodeMeta = tableResult.getSingleData();

                ResultData<FlowNodeMeta.TableValue> tableValueResultData = Utils.resultDataJson2ResultDataToast(FlowNodeMeta.TableValue.class,
                        PlatfromGisLinkDetailActivity.this, results[1], "获取界面值失败", false);
                if (tableValueResultData == null) return;
                tableValues = tableValueResultData.DataList;
                setVal2mFlowNodeMeta();
                startCreateView();
            }

        }.mmtExecute();
    }

    void setVal2mFlowNodeMeta() {
        mFlowNodeMeta.Values.clear();
        mFlowNodeMeta.Values.addAll(tableValues);
    }

    void startCreateView() {
        mFlowBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", mFlowNodeMeta.mapToGDFormBean());
        mFlowBeanFragment.setArguments(args);
        addFragment(mFlowBeanFragment);
        mFlowBeanFragment.setFormOnlyShow();
        createBottomView();
    }

    private void createBottomView() {
        BottomUnitView recordUnitView = new BottomUnitView(PlatfromGisLinkDetailActivity.this);
        recordUnitView.setContent("挂接");
        recordUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(recordUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = "";
                String taskNo = "";
                for (TableColumn tableColumn : deviceModel.MobileRow) {
                    if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(taskNo)) {
                        break;
                    }
                    if (tableColumn.FieldName.equals("地址") || tableColumn.FieldName.equals("地点")) {
                        key = tableColumn.FieldValue;
                        continue;
                    }
                    if (tableColumn.FieldName.equals("编号")) {
                        taskNo = tableColumn.FieldValue;
                        continue;
                    }
                }
                String layerName = deviceModel.GisLayers;
                BaseMapCallback callback = new PlatfromGisLinkCallback(PlatfromGisLinkDetailActivity.this, key, layerName, deviceModel.AccountTable, deviceModel.ID);
                MyApplication.getInstance().sendToBaseMapHandle(callback);
            }
        });
    }
}
