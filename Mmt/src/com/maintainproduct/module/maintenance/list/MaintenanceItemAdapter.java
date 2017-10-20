package com.maintainproduct.module.maintenance.list;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.maintainproduct.entity.MaintainSimpleInfo;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.maintainproduct.module.maintenance.detail.MaintenanceDetailActivity;
import com.maintainproduct.module.maintenance.detail.TaskLocationOnMapCallback;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.R;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/** ListView的适配器，决定每一列的视图样式 */
public class MaintenanceItemAdapter extends BaseAdapter {

	protected final ArrayList<LinkedHashMap<String, String>> data;
	protected final LayoutInflater inflater;
	protected final BaseActivity activity;
    protected BaseActivity detailActivity=null;
	protected int index = 0;
    //当前点击的item的位置，方便从list中移除该item
    public int curClickPos = 0;
	public MaintenanceItemAdapter(ArrayList<LinkedHashMap<String, String>> data, BaseActivity activity) {
		this.data = data;
		this.activity = activity;
		this.inflater = LayoutInflater.from(activity);
	}
	public MaintenanceItemAdapter(ArrayList<LinkedHashMap<String, String>> data, BaseActivity activity,BaseActivity detailActivity) {
		this.data = data;
		this.activity = activity;
		this.detailActivity=detailActivity;
		this.inflater = LayoutInflater.from(activity);
	}
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.maintenance_list_item, null);
			viewHolder = new ViewHolder();

			viewHolder.maintenanceListItemIndex = (TextView) convertView.findViewById(R.id.maintenanceListItemIndex);

			viewHolder.maintenanceListItemLeftTop = (TextView) convertView.findViewById(R.id.maintenanceListItemLeftTop);
			viewHolder.maintenanceListItemRightTop = (TextView) convertView.findViewById(R.id.maintenanceListItemRightTop);
			viewHolder.maintenanceListItemMiddle = (TextView) convertView.findViewById(R.id.maintenanceListItemMiddle);
			viewHolder.maintenanceListItemLeftBottom = (TextView) convertView.findViewById(R.id.maintenanceListItemLeftBottom);
			viewHolder.maintenanceListItemDate = (TextView) convertView.findViewById(R.id.maintenanceListItemDate);
			viewHolder.maintenanceListItemDistance = (TextView) convertView.findViewById(R.id.maintenanceListItemDistance);

			viewHolder.maintenanceListItemLocation = (TextView) convertView.findViewById(R.id.maintenanceListItemLocation);
			viewHolder.maintenanceListItemDetail = (TextView) convertView.findViewById(R.id.maintenanceListItemDetail);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		LinkedHashMap<String, String> table = data.get(position);

		Iterator<String> ite = table.keySet().iterator();

		List<String> values = new ArrayList<String>();

		int i = 0;
		while (ite.hasNext() && i < 5) {
			values.add(table.get(ite.next()));
			i++;
		}

		viewHolder.maintenanceListItemLocation.setText("定位");
		viewHolder.maintenanceListItemLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (data.get(position).containsKey("坐标") && !BaseClassUtil.isNullOrEmptyString(data.get(position).get("坐标"))) {
					onLocation(position);
				} else {
					activity.showToast("无效的坐标信息");
				}
			}
		});

		viewHolder.maintenanceListItemDetail.setText("详情");
		viewHolder.maintenanceListItemDetail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDetailClick(position);
			}
		});

		viewHolder.maintenanceListItemIndex.setText((position + 1) + ".");

		viewHolder.maintenanceListItemLeftTop.setText(values.get(0));
		viewHolder.maintenanceListItemRightTop.setText(values.get(1));
		viewHolder.maintenanceListItemMiddle.setText(values.get(2));
		viewHolder.maintenanceListItemLeftBottom.setText(values.get(3));

		viewHolder.maintenanceListItemDate.setText(table.get("BetTimeStr"));
		viewHolder.maintenanceListItemDistance.setText(table.get("DistanceStr"));
		if (index == 0) {
			viewHolder.maintenanceListItemDate.setAlpha(0.48f);
			viewHolder.maintenanceListItemDistance.setAlpha(0.87f);
		} else {
			viewHolder.maintenanceListItemDate.setAlpha(0.87f);
			viewHolder.maintenanceListItemDistance.setAlpha(0.48f);
		}

		return convertView;
	}

	public MaintainSimpleInfo mapToEntity(LinkedHashMap<String, String> itemMap) {
		MaintainSimpleInfo itemEntity = new MaintainSimpleInfo();
		itemEntity.ActiveID = Integer.valueOf(itemMap.get("活动ID"));
		itemEntity.ActiveName = itemMap.get("当前环节");
		itemEntity.CaseName = "";
		itemEntity.CaseNo = itemMap.get("案件编号");
		itemEntity.FlowName = itemMap.get("流程名称");
		itemEntity.ID0 = Integer.valueOf(itemMap.get("ID0"));
		itemEntity.Opinion = itemMap.get("承办意见");
		itemEntity.PreStepUnderTakenManName = itemMap.get("前承办人");

		itemEntity.Position = itemMap.get("坐标");
		itemEntity.ID = Integer.valueOf(itemMap.get("ID"));

		return itemEntity;
	}

	public void onDetailClick(int position) {
        curClickPos=position;
		LinkedHashMap<String, String> itemMap = data.get(position);

		Intent intent = new Intent(activity, MaintenanceDetailActivity.class);
        if(this.detailActivity!=null){
          intent = new Intent(activity, this.detailActivity.getClass());
        }
		intent.putExtra("ListItemEntity", mapToEntity(itemMap));
		activity.startActivityForResult(intent, MaintenanceConstant.DEFAULT_REQUEST_CODE);
		MyApplication.getInstance().startActivityAnimation(activity);
	}

	public void onLocation(int position) {
		LinkedHashMap<String, String> itemMap = data.get(position);

		String positionStr = itemMap.get("坐标");
		Dot dot = new Dot(Double.valueOf(positionStr.split(",")[0]), Double.valueOf(positionStr.split(",")[1]));

		MyApplication.getInstance().sendToBaseMapHandle(
				new TaskLocationOnMapCallback(dot, itemMap.get("CaseName"), itemMap.get("CaseNo")));
		Intent intent = new Intent(activity, MapGISFrame.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		activity.startActivity(intent);
	}

	class ViewHolder {
		public TextView maintenanceListItemIndex;

		public TextView maintenanceListItemLeftTop;
		public TextView maintenanceListItemRightTop;
		public TextView maintenanceListItemMiddle;
		public TextView maintenanceListItemLeftBottom;
		public TextView maintenanceListItemDate;
		public TextView maintenanceListItemDistance;

		public TextView maintenanceListItemLocation;
		public TextView maintenanceListItemDetail;
	}

	/** 刷新列表信息 */
	public void refreash(int index) {
		this.index = index;
		notifyDataSetChanged();
	}
}
