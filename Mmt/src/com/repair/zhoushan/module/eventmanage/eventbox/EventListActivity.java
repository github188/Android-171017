package com.repair.zhoushan.module.eventmanage.eventbox;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;

import com.mapgis.mmt.BaseActivity;
import com.repair.zhoushan.common.Constants;

public class EventListActivity extends BaseActivity {

    @Override
    protected void setDefaultContentView() {
        setSwipeBackEnable(false);

        EventListFragment eventListFragment = (EventListFragment)
                getSupportFragmentManager().findFragmentByTag(EventListFragment.TAG);

        if (eventListFragment == null) {
            Intent intent = getIntent();
            eventListFragment = EventListFragment.newInstance(
                    intent.getIntExtra("MODE", Mode.DISPATCH), // 设置工作模式，默认"调度箱模式"
                    intent.getStringExtra("Title"),
                    intent.getStringExtra("DefaultEventNames"));

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(android.R.id.content, eventListFragment, EventListFragment.TAG);
            transaction.show(eventListFragment);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasCategory(Constants.CATEGORY_BACK_TO_LIST)) {
            intent.removeCategory(Constants.CATEGORY_BACK_TO_LIST);

            ((EventListFragment) getSupportFragmentManager()
                    .findFragmentByTag(EventListFragment.TAG)).refreshData();
        }
    }

    public static class Mode {

        /**
         * 调度箱模式
         */
        public static final int DISPATCH = 0x0;

        /**
         * 领单箱模式
         */
        public static final int RECEIVE = 0x1;
    }

}
