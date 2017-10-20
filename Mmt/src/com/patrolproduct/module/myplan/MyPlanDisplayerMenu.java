package com.patrolproduct.module.myplan;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.R;
import com.patrolproduct.module.myplan.entity.PatrolTask;

public class MyPlanDisplayerMenu extends BaseMapMenu {
	private PatrolTask data;
	private OnDetailButtonClickListener onDetailButtonClickListener;

	public MyPlanDisplayerMenu(MapGISFrame mapGISFrame) {
		super(mapGISFrame);
	}

	public void createCustomView(PatrolTask data) {
		this.data = data;
		initCustomView();
	}

	public void setOnDetailButtonClickListener(OnDetailButtonClickListener onDetailButtonClickListener) {
		this.onDetailButtonClickListener = onDetailButtonClickListener;
	}

	@Override
	public View initTitleView() {
		View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.header_bar_plan_name, null);

		(view.findViewById(R.id.ivPlanDetail)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected();
			}
		});

		(view.findViewById(R.id.tvPlanBack)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDetailButtonClickListener.onBackClick();
			}
		});

		return view;
	}

	private void initCustomView() {
		View view = mapGISFrame.getCustomView();

		TextView tvPlanName = (TextView) view.findViewById(R.id.tvPlanName);
		tvPlanName.setText(data.PlanInfo.PlanName.toString());

		TextView tvTaskState = (TextView) view.findViewById(R.id.tvTaskState);

		if (MyPlanUtil.getTaskStateString(Integer.valueOf(data.TaskID)).trim().length() != 0) {
			tvTaskState.setTag(data);
			tvTaskState.setTextColor(Color.WHITE);
			tvTaskState.setText(MyPlanUtil.getTaskStateString(Integer.valueOf(data.TaskID)));
		} else {
			tvPlanName.setTextAppearance(mapGISFrame, R.style.default_text_large);
			tvPlanName.setTextColor(Color.WHITE);
			tvTaskState.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onOptionsItemSelected() {
		onDetailButtonClickListener.onDetailClick();
		return false;
	}

	public interface OnDetailButtonClickListener {
		void onDetailClick();

		void onBackClick();
	}

}
