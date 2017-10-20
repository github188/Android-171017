package com.patrolproduct.module.projectquery;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by KANG on 2016/9/2.
 * ViewPager的页面数据适配器
 */
public class ListFragmentPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<ListPagerFragment> listFragments;

    public ListFragmentPagerAdapter(FragmentManager fm,ArrayList<ListPagerFragment> fragments) {
        super(fm);
        this.listFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return listFragments.get(position);
    }

    @Override
    public int getCount() {
        return listFragments == null ? 0 : listFragments.size();
    }
}
