package com.mapgis.mmt.module.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class NNavigationPagerAdapter extends FragmentPagerAdapter {
	
	private NavigationActivity activity;
	private final ArrayList<ArrayList<NavigationItem>> pagerDataSource;

	public NNavigationPagerAdapter(FragmentManager fm, NavigationActivity activity, ArrayList<ArrayList<NavigationItem>> pagerDataSource) {
		super(fm);
		this.pagerDataSource = pagerDataSource;
		this.activity = activity;
	}

	@Override
	public Fragment getItem(int arg0) {
		return new NNavigationFragment(activity, pagerDataSource.get(arg0));
	}
	
	@Override
	public int getCount() {
		return pagerDataSource.size();
	}

}
