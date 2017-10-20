package com.mapgis.mmt.common.widget.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton.OnScrollListener;

public class MultiSwitchFragment extends Fragment {
	private MultiSwitchButton switchButton;
	private ViewPager viewPager;

	private OnPageSelectedListener onPageSelectedListener;

	private String[] titleNames;
	private Fragment[] fragments;

	private int currentIndex = -1;
	private boolean isScrolling = true;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mutil_switch_fragment_view, container, false);
        switchButton = (MultiSwitchButton) view.findViewById(R.id.mutilSwitchFragmentTitle);
		viewPager = (ViewPager) view.findViewById(R.id.mutilSwitchFragmentPager);
		viewPager.setOffscreenPageLimit(5);
		return view;
	}

    /**
     * 设置标题的可见性
     */
    public void setTitleVisibility(int visibility){
        switchButton.setVisibility(View.GONE);
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		if (titleNames == null || fragments == null) {
			return;
		}

		if (titleNames.length != fragments.length) {
			return;
		}

		switchButton.setContent(titleNames);

		viewPager.setAdapter(new Adapter(getActivity().getSupportFragmentManager(), fragments));
		viewPager.setCurrentItem(currentIndex == -1 ? 0 : currentIndex);
		switchButton.setCurrentItem(currentIndex == -1 ? 0 : currentIndex);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                onMyPageSelected(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                if (isScrolling) {
                    switchButton.moveRectLeft(arg0, arg1);
                }
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                if (arg0 == ViewPager.SCROLL_STATE_IDLE) {
                    isScrolling = true;
                }
            }
        });
		switchButton.setOnScrollListener(new OnScrollListener() {
            @Override
            public void OnScrollComplete(int index) {
                isScrolling = false;
                onMyPageSelected(index);
            }
        });
	}

	/** 获取Fragment */
	public Fragment getFragment(int pos) {
		if (pos >= fragments.length || pos < 0 || fragments.length == 0)
			return null;

		return fragments[pos];
	}

	/**
	 * 当前显示的第几页
	 * 
	 * @return 当前第几页，从0开始，没有返回-1。
	 */
	public int getCurrentItem() {
		return currentIndex;
	}

	/**
	 * 设置当前显示第几页
	 * 
	 * @param index
	 */
	public void setCurrentIndex(int index) {
		this.currentIndex = index;
	}

	public void setCurrentPage(int index){
		onMyPageSelected(index);
		switchButton.invalidate();
	}

	/**
	 * 填充数据
	 * 
	 * @param titleNames
	 *            标题栏目信息
	 * @param fragments
	 *            滑动显示的Fragment
	 */
	public void setDate(String[] titleNames, Fragment[] fragments) {
		this.titleNames = titleNames;
		this.fragments = fragments;
	}

	private void onMyPageSelected(int index) {
		viewPager.setCurrentItem(index);
		switchButton.setCurrentItem(index);

		currentIndex = index;

		if (onPageSelectedListener != null) {
			onPageSelectedListener.onPageSelected(index);
		}
	}

	public void setOnPageSelectedListener(OnPageSelectedListener onPageSelectedListener) {
		this.onPageSelectedListener = onPageSelectedListener;
	}

	/** 滑动页适配器 */
	class Adapter extends FragmentStatePagerAdapter {
		private final Fragment[] fragments;

		public Adapter(FragmentManager fm, Fragment[] fragments) {
			super(fm);
			this.fragments = fragments;
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragments[arg0];
		}

		@Override
		public int getCount() {
			return fragments.length;
		}
	}

	/** 滑动结束后的监听器 */
	public interface OnPageSelectedListener {
		/**
		 * 滑动结束后执行
		 * 
		 * @param arg0
		 *            当前显示的第几页
		 */
        void onPageSelected(int arg0);
	}
}
