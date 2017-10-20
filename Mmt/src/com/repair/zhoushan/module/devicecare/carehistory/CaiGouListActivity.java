package com.repair.zhoushan.module.devicecare.carehistory;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.PurchaseOrderListFragment;

public class CaiGouListActivity extends BaseActivity {

    private ScheduleTask mScheduleTask;

    private PurchaseOrderListFragment mPurchaseOrderListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");

        getBaseTextView().setText("采购订单");

        createView();

    }

    private void createView() {

        this.mPurchaseOrderListFragment = new PurchaseOrderListFragment();

        Bundle args = new Bundle();
        args.putParcelable("ListItemEntity", mScheduleTask);
        args.putBoolean("AllowDelete", false);
        mPurchaseOrderListFragment.setArguments(args);

        addFragment(mPurchaseOrderListFragment);
    }

}
