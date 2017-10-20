package com.repair.zhoushan.module.casemanage.casedetail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.widget.slidingtab.SlidingTabLayout;
import com.mapgis.mmt.R;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FeedbackInfo;

import java.util.ArrayList;
import java.util.List;

public class CaseDetailFragmentV2 extends Fragment {

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;

    public List<Fragment> fragments = new ArrayList<>();
    private ArrayList<String> groupTitles = new ArrayList<>();
    private ArrayList<GDFormBean> gdFormBeans;
    private CaseItem caseItemEntity;

    private List<FeedbackInfo> feedbackInfos;

    BaseActivity mContext;

    private CaseDetailFragmentV2(String[] groupTitles, ArrayList<GDFormBean> gdFormBeans, CaseItem caseItem) {

        this.gdFormBeans = gdFormBeans;
        this.caseItemEntity = caseItem;

        for (String title : groupTitles) {
            this.groupTitles.add(title);

        }

    }

    public CaseDetailFragmentV2(ArrayList<GDFormBean> gdFormBeans, CaseItem caseItem) {
        this(new String[]{"基本信息", "办理过程"}, gdFormBeans, caseItem);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = (BaseActivity) getActivity();

        View view = inflater.inflate(R.layout.morefragment_content_view, container, false);

        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);

            //基本信息
            Fragment fragment0 = new CaseDetailFlowFragment(groupTitles, gdFormBeans);

            fragments.add(fragment0);


            //办理过程
            Fragment fragment1 = new CaseHandleProcedureFragment();

            Bundle augBundle = new Bundle();

            augBundle.putParcelable("ListItemEntity", caseItemEntity);

            fragment1.setArguments(augBundle);

            fragments.add(fragment1);


            Bundle bundle = getArguments();

            if (bundle != null) {
                String feedbackInfoStr = bundle.getString("feedbackInfos");
                if (!TextUtils.isEmpty(feedbackInfoStr)) {
                    feedbackInfos = new Gson().fromJson(feedbackInfoStr, new TypeToken<List<FeedbackInfo>>() {
                    }.getType());
                }
                if (feedbackInfos != null) {
                    for (FeedbackInfo fbi : feedbackInfos) {
                        String fbBizName = fbi.FBBiz;
                        String tablename = fbi.FBTable;
                        if (TextUtils.isEmpty(fbBizName) || TextUtils.isEmpty(tablename)) {
                            continue;
                        }
                        this.groupTitles.add(fbi.FBBiz + "列表");


                        Fragment feedBackFragment = new FeedBackListFragment();

                        Bundle augBundle2 = new Bundle();

                        augBundle2.putBoolean("isRead", fbi.isRead);
                        augBundle2.putString("feedbackInfo",new Gson().toJson(fbi));
                        augBundle2.putString("bizName", fbBizName);
                        augBundle2.putString("caseNo", caseItemEntity.CaseNo);
                        augBundle2.putString("tableName", tablename);

                        feedBackFragment.setArguments(augBundle2);

                        fragments.add(feedBackFragment);
                    }
                }
            }

            mViewPager.setOffscreenPageLimit(15);
            mViewPager.setAdapter(new ViewPagerAdapter(mContext.getSupportFragmentManager(), groupTitles.toArray(new String[groupTitles.size()]), fragments));
            mSlidingTabLayout.setViewPager(mViewPager);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private final String[] pageTitles;

        private List<Fragment> fragments;

        public ViewPagerAdapter(FragmentManager fm, String[] pageTitles, List<Fragment> fragments) {
            super(fm);
            this.pageTitles = pageTitles;
            this.fragments = fragments;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }

        @Override
        public Fragment getItem(int position) {

            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return pageTitles.length;
        }
    }
}