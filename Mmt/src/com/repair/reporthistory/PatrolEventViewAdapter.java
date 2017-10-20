package com.repair.reporthistory;

import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.R;

class PatrolEventViewAdapter extends BaseAdapter {
	private final PatrolEventListViewFragment eventViewFragment;
	private final LayoutInflater inflater;
	PullToRefreshListView listView;

	public PatrolEventViewAdapter(
			PatrolEventListViewFragment eventViewFragment,
			View mPullRefreshListView) {
		this.eventViewFragment = eventViewFragment;
		this.inflater = LayoutInflater.from(this.eventViewFragment
				.getActivity());
	}

	@Override
	public int getCount() {
		return PatrolEventListViewFragment.parametersList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View contentView, ViewGroup arg2) {
		ViewHolder holder;
		if (contentView == null) {
			contentView = inflater.inflate(
					R.layout.patrol_reporter_event_view_item, null);

			holder = new ViewHolder();
			holder.position = position;
			holder.indexText = (TextView) contentView
					.findViewById(R.id.patrolReportListItemIndex);
			holder.eventCodeText = (TextView) contentView
					.findViewById(R.id.txtEventCode);
			holder.addressText = (TextView) contentView
					.findViewById(R.id.addressText);
			holder.stateText = (TextView) contentView
					.findViewById(R.id.stateText);
			holder.bigClassText = (TextView) contentView
					.findViewById(R.id.bigClassText);
			holder.smallClassText = (TextView) contentView
					.findViewById(R.id.smallClassText);
			holder.reportTimeText = (TextView) contentView
					.findViewById(R.id.reportTimeText);
			holder.patrolReportListItemDetail = (TextView) contentView
					.findViewById(R.id.patrolReportListItemDetail);
			holder.patrolReportListItemLocation = (TextView) contentView
					.findViewById(R.id.patrolReportListItemLocation);

			contentView.setTag(holder);
		} else {
			holder = (ViewHolder) contentView.getTag();
		}
		final PatrolEventEntityTrue pe = PatrolEventListViewFragment.parametersList
				.get(position);
		if (pe != null) {
			holder.indexText.setText("" + (position + 1));
			if (BaseClassUtil.isNullOrEmptyString(pe.getEventCode())) {
				holder.eventCodeText.setVisibility(View.GONE);
			} else {
				holder.eventCodeText.setVisibility(View.VISIBLE);
				holder.eventCodeText.setText("编号：" + pe.getEventCode());
			}
			holder.addressText.setText("地址：" + pe.Address);
			String state = pe.getEventState();
			if(BaseClassUtil.isNullOrEmptyString(state)){
				state = "(未处理)";
			}
			if (state.equals("已处理")) {
				holder.stateText
						.setTextColor(PatrolEventViewAdapter.this.eventViewFragment
								.getActivity().getResources()
								.getColor(R.color.default_line_bg));
			} else if(state.equals("未上报")) {
				holder.stateText
						.setTextColor(PatrolEventViewAdapter.this.eventViewFragment
								.getActivity().getResources()
								.getColor(R.color.red));
			}else{
                holder.stateText
                        .setTextColor(PatrolEventViewAdapter.this.eventViewFragment
                                .getActivity().getResources()
                                .getColor(R.color.default_accent_color));
            }
			holder.stateText.setText(state);
			holder.bigClassText.setText("类型：" + pe.EventType);
			holder.smallClassText.setText("内容：" + pe.EventClass);
			holder.reportTimeText.setText("时间：" + pe.getReportTime().replace('T', ' '));
			holder.patrolReportListItemDetail
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							PatrolEventDetailFragment fragment = new PatrolEventDetailFragment(
									pe);
							FragmentTransaction ft = eventViewFragment
									.getActivity().getSupportFragmentManager()
									.beginTransaction();
							ft.replace(R.id.baseFragment, fragment);
							ft.addToBackStack(null);
							ft.commit();
						}
					});
	    	final int index=position;
			holder.patrolReportListItemLocation
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
//							if (pe.Position == null
//									|| pe.Position.split(",").length != 2) {
//								Toast.makeText(eventViewFragment.getActivity(),
//										"无效坐标", 0).show();
//							}
//							Dot dot = new Dot(Double.valueOf(pe.Position
//									.split(",")[0]), Double.valueOf(pe.Position
//									.split(",")[1]));
//
//							MyApplication.getInstance().sendToBaseMapHandle(
//									new TaskLocationOnMapCallback(dot, "巡线定位",
//											""));
//							Intent intent = new Intent(eventViewFragment
//									.getActivity(), MapGISFrame.class);
//							intent.putExtra("isShowBaseLeftImage", true);
//							intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//							eventViewFragment.getActivity().startActivity(
//									intent);
							
							 BaseMapCallback callback = new ShowMapPointCallback(eventViewFragment.getActivity(), pe.Position ,
		                                pe.getEventCode(), pe.Address, index);

		                        MyApplication.getInstance().sendToBaseMapHandle(callback);
						}
					});

		}
		return contentView;
	}

	class ViewHolder {
		public int position;
		public TextView indexText;
		public TextView eventCodeText;
		public TextView addressText;
		public TextView stateText;
		public TextView bigClassText;
		public TextView smallClassText;
		public TextView reportTimeText;
		public TextView patrolReportListItemLocation;
		public TextView patrolReportListItemDetail;
	}
}