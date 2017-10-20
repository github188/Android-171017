package com.mapgis.mmt.module.gps.gpsstate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

public class GpsStateActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            getBaseTextView().setText("GPS状态");

            Fragment fragment = GpsStatesFragment.getInstance();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            ft.add(R.id.baseFragment, fragment);
            ft.show(fragment);

            ft.commitAllowingStateLoss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    private TabLayout mTabLayout;
//    private ViewPager mViewPager;
//
//    @Override
//    protected void setDefaultContentView() {
//        setContentView(R.layout.activity_tab_viewpager);
//
//        setSwipeBackEnable(false);
//        addBackBtnListener(getBaseLeftImageView());
//        getBaseTextView().setText("GPS状态");
//
//        initView();
//    }
//
//    private void initView() {
//
//        this.mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
//        this.mViewPager = (ViewPager) findViewById(R.id.viewpager);
//
//        FragmentPagerAdapter adapter = new StateFragmentPagerAdapter(getSupportFragmentManager());
//        mViewPager.setAdapter(adapter);
//        mTabLayout.setupWithViewPager(mViewPager);
//    }
//
//    private class StateFragmentPagerAdapter extends FragmentPagerAdapter {
//
//        private String[] tabStrs = new String[]{"GPS状态", "后台任务"};
//
//        StateFragmentPagerAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return tabStrs[position];
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//
//            Fragment fragment = null;
//            switch (position) {
//                case 0:
//                    fragment = GpsStatesFragment.getInstance();
//                    break;
//                case 1:
//                    fragment = BackgroundTaskStateFragment.getInstance();
//                    break;
//            }
//
//            return fragment;
//        }
//
//        @Override
//        public int getCount() {
//            return tabStrs.length;
//        }
//    }

}
