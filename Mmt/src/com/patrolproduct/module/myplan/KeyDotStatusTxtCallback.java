package com.patrolproduct.module.myplan;

import android.os.Message;
import android.support.v4.view.ViewPager;

import com.mapgis.mmt.entity.BaseMapCallback;
import com.patrolproduct.module.myplan.entity.PatrolTask;
import com.patrolproduct.module.myplan.map.PlanPageFragment;

public class KeyDotStatusTxtCallback extends BaseMapCallback {
	PatrolTask data;

	public KeyDotStatusTxtCallback(PatrolTask map) {
		this.data = map;
	}

	@Override
	public boolean handleMessage(Message msg) {

		if (MyPlanUtil.getTaskStateString(Integer.valueOf(data.TaskID)).trim().length() != 0) {
			ViewPager viewPager = mapGISFrame.getFragment().getViewPager();

			if (viewPager.getAdapter() == null) {
				return false;
			}

			int index = viewPager.getCurrentItem();

			PlanPageFragment fragment = (PlanPageFragment) viewPager.getAdapter().instantiateItem(viewPager, index);

			fragment.setTaskState(Integer.valueOf(data.TaskID));

		}

		return false;
	}

}
