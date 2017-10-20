package com.repair.zhoushan.module.devicecare.careoverview.detail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.widget.slidingtab.SlidingTabLayout;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.devicecare.ScheduleTask;

import java.util.ArrayList;

public class CareOverviewDetailActivity extends BaseActivity {

    private ScheduleTask mScheduleTask;

    @Override
    protected void setDefaultContentView() {
        setSwipeBackEnable(false);

        mScheduleTask = getIntent().getParcelableExtra("ScheduleTask");

        setContentView(R.layout.activity_care_overview_detail);
        initView();
    }

    private void initView() {

        getBaseTextView().setText("详情");

        addBackBtnListener(getBaseLeftImageView());

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(10);

        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), getTabStrs(mScheduleTask.BizName)));
        slidingTabLayout.setViewPager(viewPager);
    }

    private String[] getTabStrs(final String biz) {

        final String[] careBizNames = {
                "场站设备",
                "场站设备检定",
                "车用设备",
                "车用设备检定",
                "调压器养护",
                "阀门养护",

                "防腐层检测",
                "工商户安检",
                "工商户表具",
                "工商户表具检定",
                "工商户抄表",
                "工商户台账",
                "阴极保护桩检测"
        };

        final int[] tabCodes = {   // 对应养护类型详情页面的Tab编码,编码倒序
                0B111101, // 101111
                0B101, // 101
                0B111101, // 101111
                0B101, // 101
                0B111110, // 011111
                0B111110, // 011111

                0B100111, // 011111
                0B100111, // 011111
                0B100111, // 011111
                0B100111, // 011111
                0B100111, // 011111
                0B100111, // 011111
                0B100111, // 01111
        };

        final String[] detailTabs = {
                "设备详情",
                "台账信息",
                "任务详情",
                "物料详情",
                "采购订单详情",
                "耗材详情"
        };

        int tabCode = 0;
        for (int i = 0; i < careBizNames.length; i++) {
            if (careBizNames[i].equals(biz)) {
                tabCode = tabCodes[i];
                break;
            }
        }

        int index = 0;
        ArrayList<String> tabList = new ArrayList<>();

        while (tabCode > 0) {
            if ((tabCode & 1) == 1) {
                tabList.add(detailTabs[index]);
            }
            tabCode >>= 1;
            index++;
        }

        return tabList.toArray(new String[tabList.size()]);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private final String[] pageTitles;

        ViewPagerAdapter(FragmentManager fm, String[] pageTitles) {
            super(fm);
            this.pageTitles = pageTitles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }

        @Override
        public Fragment getItem(int position) {

            Bundle args;
            switch (pageTitles[position]) {
                case "台账信息":
                    args = new Bundle();
                    args.putString("BizName", mScheduleTask.BizName);
                    args.putString("TaskCode", mScheduleTask.TaskCode);
                    return Fragment.instantiate(CareOverviewDetailActivity.this, TaskTableInfoFragment.class.getName(), args);

                case "设备详情":
                    args = new Bundle();
                    args.putString("BizName", mScheduleTask.BizName);
                    args.putString("TaskCode", mScheduleTask.TaskCode);
                    return Fragment.instantiate(CareOverviewDetailActivity.this, StationTaskTableInfoFragment.class.getName(), args);

                case "任务详情":
                    args = new Bundle();
                    args.putString("BizName", mScheduleTask.BizName);
                    args.putString("TaskCode", mScheduleTask.TaskCode);
                    args.putString("BizTaskTable", mScheduleTask.BizTaskTable);
                    args.putString("BizFeedBackTable", mScheduleTask.BizFeedBackTable);
                    return Fragment.instantiate(CareOverviewDetailActivity.this, FeedbackInfoFragment.class.getName(), args);

                case "物料详情":
                    args = new Bundle();
                    args.putString("CaseNo", mScheduleTask.TaskCode);
                    return Fragment.instantiate(CareOverviewDetailActivity.this, MaterialFragment.class.getName(), args);

                case "采购订单详情":
                    args = new Bundle();
                    args.putString("CaseNo", mScheduleTask.TaskCode);
                    return Fragment.instantiate(CareOverviewDetailActivity.this, PurchaseOrderFragment.class.getName(), args);

                case "耗材详情":
                    args = new Bundle();
                    args.putString("BizName", mScheduleTask.BizName);
                    args.putString("CaseNo", mScheduleTask.TaskCode);
                    return Fragment.instantiate(CareOverviewDetailActivity.this, ConsumableFragment.class.getName(), args);

                default:
                    args = new Bundle();
                    args.putString("BizName", pageTitles[position]);
                    // return Fragment.instantiate(CareOverviewDetailActivity.this, CareListFragment.class.getName(), args);
            }

            // TODO: 7/18/16
            return null;
        }

        @Override
        public int getCount() {
            return pageTitles.length;
        }
    }
}
