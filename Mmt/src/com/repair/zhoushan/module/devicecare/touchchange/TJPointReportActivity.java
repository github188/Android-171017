package com.repair.zhoushan.module.devicecare.touchchange;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageDotView;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.tablecommonhand.TableMode;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordActivity;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

/**
 * Created by liuyunfan on 2016/7/15.
 */
public class TJPointReportActivity extends TableOneRecordActivity {
    private String caseno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        caseno = getIntent().getStringExtra("caseno");

        if (TextUtils.isEmpty(caseno)) {
            showErrorMsg("caseno未找到");
            return;
        }

        cacheKey = caseno;

        tableMode = new TableMode("碰接点记录表", -1, TabltViewMode.REPORT.getTableViewMode());

        if (flowBeanFragment != null) {

            flowBeanFragment.setBeanFragmentOnCreate(new FlowBeanFragment.BeanFragmentOnCreate() {
                @Override
                public void onCreated() {
                    View view = flowBeanFragment.findViewByName("工单编号");

                    if (view instanceof ImageEditView) {
                        ImageEditView iv = (ImageEditView) view;

                        iv.setValue(caseno);
                        return;
                    }

                    if (view instanceof ImageTextView) {
                        ImageTextView iv = (ImageTextView) view;

                        iv.setValue(caseno);
                        return;
                    }

                    showErrorMsg("工单编号控件未找到");


                }
            });
        }

        super.onCreate(savedInstanceState);

    }

    private void updateEventPos() {

        String pos="";
        if(flowBeanFragment!=null){
            ImageDotView view = (ImageDotView) flowBeanFragment.findViewByType("坐标");

            if(view !=null){
                pos=view.getValue();
            }
            if(TextUtils.isEmpty(pos)){
                ImageButtonView iv= (ImageButtonView)flowBeanFragment.findViewByType("坐标");
                pos=iv.getValue();
            }
        }

        if(TextUtils.isEmpty(pos)){
            GpsXYZ gpsXYZdot = GpsReceiver.getInstance().getLastLocalLocation();

            if (!gpsXYZdot.isUsefull()) {
                return;
            }
            pos=gpsXYZdot.toXY();
        }


        final StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CrashReplace/UpdateCrashPointEventPosition")
                .append("?caseNO=").append(caseno)
                .append("&pos=").append(pos);

        new MmtBaseTask<Void, Void, String>(this) {
            @Override
            protected String doInBackground(Void... params) {

                return NetUtil.executeHttpGet(sb.toString());
            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                ResultWithoutData rwd = Utils.resultWithoutDataJson2ResultDataToast(TJPointReportActivity.this, s, "更新事件坐标失败", "更新事件坐标成功");
                if (rwd == null) {
                    return;
                }
            }
        }.mmtExecute();
    }

    @Override
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

                        Toast.makeText(TJPointReportActivity.this, "添加成功!", Toast.LENGTH_SHORT).show();

                        backByReorder(true);

                        updateEventPos();

                    } else {
                        Toast.makeText(TJPointReportActivity.this, "添加失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }
}
