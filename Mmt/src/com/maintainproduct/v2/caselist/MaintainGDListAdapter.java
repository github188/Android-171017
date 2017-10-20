package com.maintainproduct.v2.caselist;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maintainproduct.module.maintenance.list.MaintenanceListUtil;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.R;

import java.util.ArrayList;

public class MaintainGDListAdapter extends BaseAdapter {

	private final ArrayList<GDItem> dataList;
	private final LayoutInflater inflater;

	private int index = 0;

	public MaintainGDListAdapter(ArrayList<GDItem> dataList, LayoutInflater inflater) {
		super();
		this.dataList = dataList;
		this.inflater = inflater;
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.gd_list_item2, null);
			viewHolder.serialNumTV = (TextView) convertView.findViewById(R.id.serialNumTV);
			viewHolder.gdNumTV = (TextView) convertView.findViewById(R.id.gd_num);
			viewHolder.reportTypeTV = (TextView) convertView.findViewById(R.id.gd_report_type);
			viewHolder.levelTV = (TextView) convertView.findViewById(R.id.gd_level);
			viewHolder.timeTV = (TextView) convertView.findViewById(R.id.gd_time);
			viewHolder.stateTV = (TextView) convertView.findViewById(R.id.gd_state);
			viewHolder.addressTV = (TextView) convertView.findViewById(R.id.gd_address);
			viewHolder.opinintTypeTV = (TextView) convertView.findViewById(R.id.opinintTypeTV);
			viewHolder.opinionTV = (TextView) convertView.findViewById(R.id.opinionTV);
			viewHolder.tuidanIconImgv = (ImageView) convertView.findViewById(R.id.tuidanIconImgv);
			viewHolder.hasReadImgv = (ImageView) convertView.findViewById(R.id.hasReadImgv);
			viewHolder.betTime = (TextView) convertView.findViewById(R.id.betTime);
			viewHolder.betDistance = (TextView) convertView.findViewById(R.id.betDistance);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.serialNumTV.setText("" + (position + 1));
		viewHolder.gdNumTV.setText(dataList.get(position).CaseCode);
		viewHolder.reportTypeTV.setText(dataList.get(position).ReportType + "/" + dataList.get(position).ReportContent);
		viewHolder.levelTV.setText(dataList.get(position).Level);
		viewHolder.timeTV.setText(dataList.get(position).Receivetime);
		viewHolder.stateTV.setText(dataList.get(position).State);
		viewHolder.addressTV.setText(dataList.get(position).Address);
		viewHolder.opinionTV.setText(dataList.get(position).Opinion);

		if (dataList.get(position).ReadGDTime == null || dataList.get(position).ReadGDTime.length() == 0) {
			viewHolder.hasReadImgv.setImageResource(R.drawable.msg_new);
		} else {
			viewHolder.hasReadImgv.setImageResource(R.drawable.msg_read);
		}

		if (dataList.get(position).State.equals("未接受")) {
			viewHolder.stateTV.setTextColor(Color.RED);
		} else if (dataList.get(position).State.equals("已阅读")) {
			viewHolder.stateTV.setTextColor(Color.DKGRAY);
		} else if (dataList.get(position).State.equals("待处理")) {
			viewHolder.stateTV.setTextColor(Color.MAGENTA);
		} else if (dataList.get(position).State.equals("处理中")) {
			viewHolder.stateTV.setTextColor(Color.BLUE);
		} else {
			viewHolder.stateTV.setTextColor(Color.BLACK);
		}

		if (dataList.get(position).Direction.toString().equals("-1")) {
			viewHolder.opinintTypeTV.setText("退单原因：");
			viewHolder.opinintTypeTV.setTextColor(Color.RED);
			viewHolder.opinionTV.setTextColor(Color.RED);
			viewHolder.tuidanIconImgv.setVisibility(View.VISIBLE);
		} else {
			viewHolder.opinintTypeTV.setText("派单意见：");
			viewHolder.opinintTypeTV.setTextColor(Color.parseColor("#00AAAA"));
			viewHolder.opinionTV.setTextColor(Color.parseColor("#00AAAA"));
			viewHolder.tuidanIconImgv.setVisibility(View.INVISIBLE);
		}

		if (index == 0) {
			viewHolder.betTime.setAlpha(0.24f);
			viewHolder.betDistance.setAlpha(0.87f);
		} else {
			viewHolder.betTime.setAlpha(0.87f);
			viewHolder.betDistance.setAlpha(0.24f);
		}

		String distanceStr = "";
		if (!BaseClassUtil.isNullOrEmptyString(dataList.get(position).Distance) && BaseClassUtil.isNum(dataList.get(position).Distance)) {
			distanceStr = MaintenanceListUtil.getDistance(Double.valueOf(dataList.get(position).Distance));
		} else {
			distanceStr = dataList.get(position).Distance;
		}
		viewHolder.betDistance.setText(distanceStr);

		String betTimeStr = "";
		if (!BaseClassUtil.isNullOrEmptyString(dataList.get(position).BetTime) && BaseClassUtil.isNum(dataList.get(position).BetTime)) {
			betTimeStr = MaintenanceListUtil.getBetTime(Long.valueOf(dataList.get(position).BetTime));
		} else {
			betTimeStr = dataList.get(position).BetTime;
		}
		viewHolder.betTime.setText(betTimeStr);

		return convertView;
	}

	/** 刷新列表信息 */
	public void refreash(int index) {
		this.index = index;
		notifyDataSetChanged();
	}

	private class ViewHolder {

		public TextView serialNumTV;
		public TextView gdNumTV;
		public TextView reportTypeTV;
		public TextView levelTV;
		public TextView timeTV;
		public TextView stateTV;
		public TextView addressTV;
		public TextView opinintTypeTV;
		public TextView opinionTV;
		public ImageView tuidanIconImgv;
		public ImageView hasReadImgv;

		public TextView betTime;
		public TextView betDistance;

	}
}
