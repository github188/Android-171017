package com.maintainproduct.module.basereport.history;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.maintainproduct.entity.BaseReportEntity;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.DateSelectDialogFragment;
import com.mapgis.mmt.common.widget.DateSelectDialogFragment.OnDateSelectPositiveClick;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.common.widget.fragment.MultiSwitchFragment;
import com.mapgis.mmt.common.widget.fragment.MultiSwitchFragment.OnPageSelectedListener;
import com.mapgis.mmt.config.Product;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.MenuItem;
import com.mapgis.mmt.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReportHistoryActivity extends BaseActivity {
	// private ReportHistoryFragment fragment;

	public static final int Today = 0;
	public static final int Week = 1;
	public static final int Month = 2;
	public static final int Year = 3;
	public static final int Any = 4;
	public static final int All = 5;

	private ReportHistoryListFragment[] fragments;
	private String[] titleNames;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		deleteData();

		getBaseTextView().setText("历史事件");

		titleNames = initTitles();
		if (titleNames == null || titleNames.length == 0){
			return;
		}
		fragments = initFragments(titleNames);

		MultiSwitchFragment fragment = new MultiSwitchFragment();
		fragment.setDate(titleNames, fragments);
		fragment.setOnPageSelectedListener(new OnPageSelectedListener() {
			@Override
			public void onPageSelected(int arg0) {

			}
		});

		addFragment(fragment);

		getBaseRightImageView().setVisibility(View.VISIBLE);
		getBaseRightImageView().setImageResource(R.drawable.icon_more);
		getBaseRightImageView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showScope();
			}
		});
	}

	/** 显示选择范围对话框 */
	private void showScope() {
		final ListDialogFragment dialogFragment = new ListDialogFragment("选择时间段", new String[] { "本日", "本周", "自定义", "全部" });
		dialogFragment.show(getSupportFragmentManager(), "1");
		dialogFragment.setListItemClickListener(new OnListItemClickListener() {
			@Override
			public void onListItemClick(int arg2, String value) {
				switch (arg2) {
				case 0:
					refreash(Today, null, null);
					break;
				case 1:
					refreash(Week, null, null);
					break;
				case 2:
					showDateSelect();
					break;
				case 3:
					refreash(All, null, null);
					break;
				}
			}
		});

	}

	/** 显示日期选择框 */
	private void showDateSelect() {
		DateSelectDialogFragment dateSelectDialogFragment = new DateSelectDialogFragment();
		dateSelectDialogFragment.show(getSupportFragmentManager(), "2");
		dateSelectDialogFragment.setOnDateSelectPositiveClick(new OnDateSelectPositiveClick() {
			@Override
			public void setOnDateSelectPositiveClickListener(View view, String startDate, String endDate, long startTimeLong,
					long endTimeLong) {
				refreash(Any, startDate, endDate);
			}
		});
	}

	/** 初始标题来信息 */
	private String[] initTitles() {
		List<String> titles = new ArrayList<String>();

		for (MenuItem menuItem : Product.getInstance().MainMenus) {
			if (menuItem.Name.equals("自定义上报")) {
				titles.add(menuItem.Alias);
			}
		}

		String[] items = MyApplication.getInstance().getConfigValue("自定义上报", String[].class);

		if (items == null){
			return new String[0];
		}
		for (String item : items) {
			if (!titles.contains(item)) {
				titles.add(item);
			}
		}

		return titles.toArray(new String[titles.size()]);
	}

	/** 初始化内容信息 */
	private ReportHistoryListFragment[] initFragments(String[] titles) {
		ReportHistoryListFragment[] fragments = new ReportHistoryListFragment[titles.length];

		for (int i = 0; i < titles.length; i++) {
			fragments[i] = new ReportHistoryListFragment(this, titles[i]);
		}

		return fragments;
	}

	// 删除15天以前的数据，防止数据冗余
	private void deleteData() {
		List<BaseReportEntity> entities = DatabaseHelper.getInstance().query(BaseReportEntity.class, "Time<datetime('now','-15 day')");

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
	private void refreash(int flag, String startTime, String endTime) {
		for (ReportHistoryListFragment fragment : fragments) {
			fragment.refreash(flag, startTime, endTime);
		}
	}

}
