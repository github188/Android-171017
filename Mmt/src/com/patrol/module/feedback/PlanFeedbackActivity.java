package com.patrol.module.feedback;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.patrol.entity.KeyPoint;

public class PlanFeedbackActivity extends BaseActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private KeyPoint keyPoint;

    @Override
    protected void setDefaultContentView() {
        setContentView(R.layout.activity_tab_viewpager);

        setSwipeBackEnable(false);
        addBackBtnListener(getBaseLeftImageView());

        this.keyPoint = getIntent().getParcelableExtra("kp");
        getBaseTextView().setText("巡检反馈: " + keyPoint.LayerName);

        initView();
    }

    private void initView() {
        this.mTabLayout = (TabLayout) findViewById(com.mapgis.mmt.R.id.tab_layout);
        this.mViewPager = (ViewPager) findViewById(com.mapgis.mmt.R.id.viewpager);

        FragmentPagerAdapter adapter = new PlanFeedbackActivity.StateFragmentPagerAdapter(
                getSupportFragmentManager(), new String[]{"反馈", "属性"});
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private class StateFragmentPagerAdapter extends FragmentPagerAdapter {

        private final String[] tabNames;

        StateFragmentPagerAdapter(FragmentManager fm, String[] tabNames) {
            super(fm);
            this.tabNames = tabNames;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabNames[position];
        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;
            switch (tabNames[position]) {
                case "反馈":
                    Intent outerIntent = getIntent();
                    String flowName = outerIntent.getStringExtra("flowName");
                    String nodeName = outerIntent.getStringExtra("nodeName");
                    String defaultParam = outerIntent.getStringExtra("defaultParam");

                    fragment = PlanFeedbackFragment.newInstance(keyPoint, flowName, nodeName, defaultParam);
                    break;
                case "属性":
                    fragment = DevicePropertyFragment.createNewInstance(keyPoint);
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return tabNames.length;
        }
    }

}
