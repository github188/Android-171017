package com.mapgis.mmt.module.gis.map.pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import com.mapgis.mmt.module.gis.MapGISFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 想要在地图上采用ViewPager滑动显示业务数据的，都必需自定义一个Fragment继承此类。
 * 此类的作用是作为地图界面中备用Fragment，用来和MapFragment交互显示。
 * 
 * <p>
 * 此类定义结束后，作为MmtMapBottomPager的MmtMapBottomFragment的属性。
 * 
 * <p>
 * 最终通过MapFragment的setBottomPager(MmtMapBottomPager bottomPager) 实现功能。
 * 
 * <p>
 * Notice: 继承此类完成后，注意在获取数据结束后调用addViewPager() 方法。
 * 
 */
public abstract class MmtMapBottomFragment extends Fragment {
	private final BottomPagerAdapter adapter;

	public MmtMapBottomFragment(MapGISFrame mapGISFrame) {
		adapter = new BottomPagerAdapter(mapGISFrame);

		mapGISFrame.getFragment().getViewPager().setVisibility(View.VISIBLE);
		mapGISFrame.getFragment().getViewPager().setAdapter(adapter);
	}

	/** 数据加载完毕后，调用此方法。会先根据复写的createPageFragment()创建滑动的Fragments，然后添加到地图界面上 */
	protected final void addViewPager() {
		setAdapter(createPageFragment());
	}

	/** 创建自己的滑动显示的Fragment，将数据显示到自己的Fragment 上 */
	public abstract Fragment[] createPageFragment();

	/** 当数据加载完毕后，将适配器加载到滑动页面上 */
	private void setAdapter(Fragment[] fragments) {
		if (fragments != null && fragments.length > 0) {
			adapter.setFragments(Arrays.asList(fragments));
			adapter.notifyDataSetChanged();
		}
	}

	class BottomPagerAdapter extends FragmentStatePagerAdapter {
		private final List<Fragment> fragments = new ArrayList<Fragment>();

		public BottomPagerAdapter(MapGISFrame mapGISFrame) {
			super(mapGISFrame.getFragment().getChildFragmentManager());
		}

		public void setFragments(List<Fragment> fragments) {
			this.fragments.clear();
			this.fragments.addAll(fragments);
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragments.get(arg0);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}
	}

}
