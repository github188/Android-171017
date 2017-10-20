package com.maintainproduct.module.basereport.history;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.maintainproduct.entity.BaseReportEntity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton.OnScrollListener;
import com.mapgis.mmt.config.Product;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.MenuItem;
import com.mapgis.mmt.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReportHistoryFragment extends Fragment {
	private MultiSwitchButton switchButton;
	private FrameLayout midFrameLayout;

	private final List<ReportHistoryListFragment> fragments = new ArrayList<ReportHistoryListFragment>();

	private int showIndex = 0;// 当前显示的Fragmen在fragments内的角标

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.basereport_history_view, null);
		switchButton = (MultiSwitchButton) view.findViewById(R.id.baseReportHistoryTop);
		midFrameLayout = (FrameLayout) view.findViewById(R.id.baseReportHistoryMid);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		init();

		switchButton.setOnScrollListener(new OnScrollListener() {
			@Override
			public void OnScrollComplete(int index) {
				showFragment(index);
			}
		});
	}

	/**
	 * 根据条件查询本地数据库的信息
	 * 
	 * @param flag
	 *            周期
	 * @param startTime
	 *            开始日期，只有在选择自定义时间才有效
	 * @param endTime
	 *            结束日期，只有在选择自定义时间才有效
	 */
	public void refreash(int flag, String startTime, String endTime) {
		for (ReportHistoryListFragment fragment : fragments) {
			fragment.refreash(flag, startTime, endTime);
		}
	}

	/** 初始化界面 */
	private void init() {
		deleteData();

		List<String> titles = new ArrayList<String>();

		for (MenuItem menuItem : Product.getInstance().MainMenus) {
			if (menuItem.Name.equals("自定义上报")) {
				titles.add(menuItem.Alias);
			}
		}

		switchButton.setContent(titles);

		initMid(titles);
	}

	// 删除15天以前的数据，防止数据冗余
	private void deleteData() {
		List<BaseReportEntity> entities = DatabaseHelper.getInstance().query(BaseReportEntity.class,
				"Time<datetime('now','-15 day')");

		for (BaseReportEntity entity : entities) {

			List<String> paths = BaseClassUtil.StringToList(entity.FilePath, ",");

			for (String path : paths) {

				File file = new File(path);

				if (file.exists()) {
					file.delete();
				}
			}
		}

		DatabaseHelper.getInstance().delete(BaseReportEntity.class, "Time<datetime('now','-15 day')");
	}

	private void initMid(List<String> titles) {
		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

		for (String title : titles) {

			ReportHistoryListFragment listFragment = new ReportHistoryListFragment(getActivity(), title);

			FrameLayout frameLayout = new FrameLayout(getActivity());
			frameLayout.setLayoutParams(new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT));
			frameLayout.setId(frameLayout.hashCode());

			midFrameLayout.addView(frameLayout);

			ft.replace(frameLayout.getId(), listFragment);
			ft.hide(listFragment);

			fragments.add(listFragment);
		}

		ft.commitAllowingStateLoss();

		showFragment(0);
	}

	// 显示指定的Fragment
	private void showFragment(int index) {
		if (fragments.size() == 0) {
			return;
		}

		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.hide(fragments.get(showIndex));
		showIndex = index;
		ft.show(fragments.get(showIndex));
		ft.commitAllowingStateLoss();
	}
}
