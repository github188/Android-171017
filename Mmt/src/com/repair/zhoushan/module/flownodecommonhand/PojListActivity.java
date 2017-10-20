package com.repair.zhoushan.module.flownodecommonhand;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.mapgis.mmt.BaseActivity;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class PojListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent=getIntent();
        setTitleAndClear(intent.getStringExtra("Title"));

        String params=intent.getStringExtra("params");

        if(TextUtils.isEmpty(params)){
            showErrorMsg("模块参数未配置");
            return;
        }
        String[] flowBizs=params.split("\\|");
        if(flowBizs.length!=2){
            showErrorMsg("模块参数配置错误");
            return;
        }

        String[] flowNodes=flowBizs[0].split(",");
        if(flowNodes.length!=2){
            showErrorMsg("模块参数配置错误");
            return;
        }
        String flowName=flowNodes[0];
        String nodeName=flowNodes[1];

        String[] bizs=flowBizs[1].split(",");
        if(bizs.length!=2){
            showErrorMsg("模块参数配置错误");
            return;
        }
        String bizName=bizs[0];
        String tableName=bizs[1];


        PojListFragment fragment = new PojListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("flowName", flowName);
        bundle.putString("nodeName", nodeName);
        bundle.putString("bizName", bizName);
        bundle.putString("tableName", tableName);

        fragment.setArguments(bundle);

        replaceFragment(fragment);
    }
}
