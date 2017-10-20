package com.repair.beihai.poj.hbpoj.module.construct;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.mapgis.mmt.BaseActivity;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class ConstructReportListAcitvity extends BaseActivity {

    private String fbBizName;
    private String caseNo;
    private String tablename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        fbBizName = intent.getStringExtra("fbBizName");
        caseNo = intent.getStringExtra("caseNo");
        tablename = intent.getStringExtra("tablename");

        if (TextUtils.isEmpty(fbBizName)) {
            showErrorMsg("业务名不能为空");
            return;
        }
        if (TextUtils.isEmpty(caseNo)) {
            showErrorMsg("工单编号不能为空");
            return;
        }
        if (TextUtils.isEmpty(tablename)) {
            showErrorMsg("表名不能为空");
            return;
        }

        Fragment feedBackFragment = new ConstructReportListFragment();

        Bundle augBundle2 = new Bundle();
        augBundle2.putBoolean("isDoingBox", true);
        augBundle2.putString("bizName", fbBizName);
        augBundle2.putString("caseNo", caseNo);
        augBundle2.putString("tableName", tablename);
        augBundle2.putBoolean("addReportbtn", true);
        feedBackFragment.setArguments(augBundle2);

        addFragment(feedBackFragment);

        setTitleAndClear(fbBizName + "上报列表");
    }
}
