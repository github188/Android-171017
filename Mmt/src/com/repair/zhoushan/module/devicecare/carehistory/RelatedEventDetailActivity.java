package com.repair.zhoushan.module.devicecare.carehistory;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.repair.zhoushan.entity.EventItem;

public class RelatedEventDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseTextView().setText("事件详情");

        EventItem eventItem = getIntent().getParcelableExtra("ListItemEntity");

        if (savedInstanceState == null) {

            RelatedEventDetailFragment fragment = new RelatedEventDetailFragment();
            Bundle args = new Bundle();
            args.putParcelable("ListItemEntity", eventItem);
            args.putBoolean("IsCheckVisible", false);
            fragment.setArguments(args);

            addFragment(fragment);
        }
    }

}
