package com.patrolproduct.module.projectquery;

import android.support.v4.app.FragmentTransaction;

import com.mapgis.mmt.BaseActivity;

/**
 * 工程查询模块
 *      能够根据“工程名称”字段查询调压箱、调压柜、流量计等设备
 *      查询到结果后能快速定位到设备所在位置
 */
public class QueryProjectActivity extends BaseActivity {

    protected QueryProjectFragment fragemnt;

    @Override
    protected void setDefaultContentView() {
        setSwipeBackEnable(false);

        fragemnt = QueryProjectFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(android.R.id.content, fragemnt, QueryProjectActivity.class.getName());
        transaction.show(fragemnt);
        transaction.commitAllowingStateLoss();
    }

    public QueryProjectFragment getFragemnt(){
        return fragemnt;
    }
}
