package com.repair.shaoxin.water.repairtask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.zondy.mapgis.android.graphic.Graphic;

import java.util.ArrayList;
import java.util.List;

public class RepairTaskDetailActivity extends BaseActivity {

    private int currentNodeType;
    private int taskID;

    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent outerIntent = getIntent();
        this.currentNodeType = outerIntent.getIntExtra("CurrentNodeType", -1);
        this.taskID = outerIntent.getIntExtra("TaskID", 0);

        // 不太严谨，未查看状态的工单只有在查询到详情时才置为已查看
        if (outerIntent.hasExtra("IsRead")) {
            setResult(Activity.RESULT_OK);
        }

        createView();
        createBottomView();
    }

    private void createView() {

        getBaseTextView().setText("工单详情");

        fragment = getSupportFragmentManager().findFragmentById(R.id.baseFragment);

        if (fragment == null) {

            Intent outerIntent = getIntent();
            String eventSource = outerIntent.getStringExtra("Type");
            int orderID = outerIntent.getIntExtra("OrderID", 0);

            fragment = RepairTaskDetailFragment.newInstance(eventSource, taskID, orderID);
            addFragment(fragment);
        }
    }

    private void createBottomView() {

        // 到达节点已经反馈的就不能再"到达"
        if (currentNodeType == -1 || currentNodeType == 0) {

            addBottomUnitView("到达", false, new OnNoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {

                    GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                            new String[]{"DisplayName", "照片", "Name", "照片", "Type", "拍照"},
                            new String[]{"DisplayName", "描述", "Name", "描述", "Type", "短文本", "DisplayColSpan", "100"});

                    Intent intent = new Intent(RepairTaskDetailActivity.this, RepairTaskReportDialogActivity.class);
                    intent.putExtra("Title", "到达上报");
                    intent.putExtra("Tag", "到达");
                    intent.putExtra("TaskID", taskID);
                    intent.putExtra("NodeType", 1);
                    intent.putExtra("GDFormBean", gdFormBean);

                    startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
                }
            });
        }


        addBottomUnitView("处理", false, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                isValidated(validateCallBack);
            }
        });

        addBottomUnitView("销单", false, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                        new String[]{"DisplayName", "照片", "Name", "照片", "Type", "拍照"},
                        new String[]{"DisplayName", "描述", "Name", "描述", "Type", "短文本", "DisplayColSpan", "100"});

                Intent intent = new Intent(RepairTaskDetailActivity.this, RepairTaskReportDialogActivity.class);
                intent.putExtra("Title", "销单申请");
                intent.putExtra("Tag", "销单");
                intent.putExtra("TaskID", taskID);
                intent.putExtra("NodeType", 3);
                intent.putExtra("GDFormBean", gdFormBean);

                startActivityForResult(intent, Constants.SPECIAL_REQUEST_CODE);
            }
        });

        addBottomUnitView("退单", false, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                        new String[]{"DisplayName", "描述", "Name", "描述", "Type", "短文本", "DisplayColSpan", "100"});

                Intent intent = new Intent(RepairTaskDetailActivity.this, RepairTaskReportDialogActivity.class);
                intent.putExtra("Title", "退单申请");
                intent.putExtra("Tag", "退单");
                intent.putExtra("TaskID", taskID);
                intent.putExtra("NodeType", 4);
                intent.putExtra("GDFormBean", gdFormBean);

                startActivityForResult(intent, Constants.SPECIAL_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK); // 操作成功返回需要刷新列表

            if (requestCode == Constants.SPECIAL_REQUEST_CODE) { // "销单","退单"操作成功后返回需要关闭详情界面
                AppManager.finishActivity(this);
            }
        }
    }

    private void isValidated(final ValidateCallBack callBack) {
        if (!(fragment instanceof RepairTaskDetailFragment)) {
            callBack.createView(false);
        }
        RepairTaskDetailFragment repairTaskDetailFragment = (RepairTaskDetailFragment) fragment;
        final RepairTaskEntity task = repairTaskDetailFragment.getTaskEntity();
        if (task == null) {
            callBack.createView(false);
            return;
        }

        if (task.leakPointDiameter1 > 300 || task.leakPointDiameter > 300) {
            callBack.createView(true);
            return;
        }

        new MmtBaseTask<Void, Void, Boolean>(this) {
            @Override
            protected Boolean doInBackground(Void... params) {

                StringBuilder sb = new StringBuilder();
                sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                        .append("/Services/CitySvr_Biz_SXGS/REST/BizSXGSRest.svc/GetRepairGis")
                        .append("?eventid=" + task.no);

                String strRet = NetUtil.executeHttpGet(sb.toString());
                if (TextUtils.isEmpty(strRet)) {
                    return false;
                }

                ResultData<RepaireGis> ret = new Gson().fromJson(strRet, new TypeToken<ResultData<RepaireGis>>() {
                }.getType());
                if (ret == null) {
                    return false;
                }
                RepaireGis repaireGis = ret.getSingleData();
                if (repaireGis == null) {
                    return false;
                }
                if (TextUtils.isEmpty(repaireGis.gisNO)) {
                    return false;
                }
                if (TextUtils.isEmpty(repaireGis.layerName)) {
                    return false;
                }
                final String strWhere = " 编号 = '" + repaireGis.gisNO + "' AND 管径>300";
                final String layerName = repaireGis.layerName;
                List<Graphic> graphics = GisQueryUtil.conditionQuery(layerName, strWhere);
                return !(graphics == null || graphics.size() == 0);

            }

            @Override
            protected void onSuccess(Boolean isValidated) {
                super.onSuccess(isValidated);
                callBack.createView(isValidated);
            }
        }.mmtExecute();
    }

    public interface ValidateCallBack {
        void createView(boolean isValidated);
    }

    ValidateCallBack validateCallBack = new ValidateCallBack() {
        @Override
        public void createView(boolean isValidated) {

            List<String[]> controls = new ArrayList<>();
            controls.add(new String[]{"DisplayName", "照片", "Name", "照片", "Type", "拍照"});

            if (isValidated) {
                controls.add(new String[]{"DisplayName", "埋深", "Name", "埋深", "Type", "短文本", "Validate", "1"});
                controls.add(new String[]{"DisplayName", "管径", "Name", "管径", "Type", "短文本", "Validate", "1"});
                controls.add(new String[]{"DisplayName", "材质", "Name", "材质", "Type", "短文本", "Validate", "1"});
                controls.add(new String[]{"DisplayName", "所处位置", "Name", "所处位置", "Type", "值选择器", "ConfigInfo", "水泥路,柏油路,绿化带", "Validate", "1"});
            } else {
                controls.add(new String[]{"DisplayName", "埋深", "Name", "埋深", "Type", "短文本"});
                controls.add(new String[]{"DisplayName", "管径", "Name", "管径", "Type", "短文本"});
                controls.add(new String[]{"DisplayName", "材质", "Name", "材质", "Type", "短文本"});
                controls.add(new String[]{"DisplayName", "所处位置", "Name", "所处位置", "Type", "值选择器", "ConfigInfo", "水泥路,柏油路,绿化带"});
            }

            controls.add(new String[]{"DisplayName", "描述", "Name", "描述", "Type", "短文本", "DisplayColSpan", "100"});

            GDFormBean gdFormBean = GDFormBean.generateSimpleForm(controls.toArray(new String[][]{}));

            Intent intent = new Intent(RepairTaskDetailActivity.this, RepairTaskReportDialogActivity.class);
            intent.putExtra("Title", "处理上报");
            intent.putExtra("Tag", "处理");
            intent.putExtra("TaskID", taskID);
            intent.putExtra("NodeType", 2);
            intent.putExtra("GDFormBean", gdFormBean);

            startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
        }
    };
}
