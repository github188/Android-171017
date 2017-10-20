package com.patrolproduct.module.myplan.list;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;

public class PlanDetailActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PlanDetailFragment fragment = new PlanDetailFragment();
        Bundle args = new Bundle();
        args.putAll(getIntent().getExtras());
        fragment.setArguments(args);

        addFragment(fragment);

        getBaseTextView().setText("计划详情");

        /**
         getBaseRightImageView().setVisibility(View.VISIBLE);
         getBaseRightImageView().setOnClickListener(new OnClickListener() {

        @Override public void onClick(View v) {
        Intent intent = new Intent(PlanDetailActivity.this, MyPlanFeedback.class);
        // intent.putExtra("id2", ((HashMap<String, String>)
        // getIntent().getSerializableExtra("planDetail")).get("id2"));
        intent.putExtra("planName", patrolTask.PlanInfo.PlanName);
        intent.putExtra("PlanTypeID", patrolTask.PlanInfo.PlanTypeID);
        intent.putExtra("flowId", patrolTask.PlanFlowID);
        intent.putExtra("taskId", patrolTask.TaskID);
        startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(PlanDetailActivity.this);
        }
        });

         // 默认不可上报,只有当设置了需要反馈，才可以反馈
         // 设备巡检针对设备反馈，不需要针对计划反馈
         //        if (Integer.valueOf(patrolTask.PlanInfo.PType) == MyPlanUtil.PLAN_DEVICE_PID || patrolTask.PlanInfo.IsFeedBack == null
         //                || Integer.valueOf(patrolTask.PlanInfo.IsFeedBack) == 0) {
         getBaseRightImageView().setVisibility(View.GONE);
         //        }
         **/
    }
}
