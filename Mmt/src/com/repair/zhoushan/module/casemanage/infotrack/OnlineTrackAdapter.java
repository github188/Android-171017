package com.repair.zhoushan.module.casemanage.infotrack;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maintainproduct.module.maintenance.detail.TaskLocationOnMapCallback;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.zondy.mapgis.geometry.Dot;

import java.text.DecimalFormat;
import java.util.List;

public class OnlineTrackAdapter extends SimpleBaseAdapter {

    protected final List<HotlineModel> dataList;

    protected final LayoutInflater inflater;
    protected final BaseActivity activity;

    public OnlineTrackAdapter(BaseActivity activity, List<HotlineModel> data) {
        this.dataList = data;
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity);
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.case_list_item, null);
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
            viewHolder.maintenanceListItemStatus = (ImageView) convertView.findViewById(R.id.ivStatus);
            viewHolder.maintenanceListItemStatusRightTip = (ImageView) convertView.findViewById(R.id.ivStatusRightTip);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        convertView.setBackgroundColor(Color.WHITE);

        final HotlineModel hotlineModel = dataList.get(position);

        viewHolder.maintenanceListItemLocation.setText("定位");
        viewHolder.maintenanceListItemLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(hotlineModel.XY)) {
                    onLocation(position);
                } else {
                    activity.showToast("无效的坐标信息");
                }
            }
        });

        viewHolder.maintenanceListItemDetail.setText("详情");

        viewHolder.maintenanceListItemDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(position);
            }
        });

        viewHolder.maintenanceListItemIndex.setText((position + 1) + ".");

        viewHolder.maintenanceListItemLeftTop.setText(hotlineModel.EventCode);
        viewHolder.maintenanceListItemLeftTop.getPaint().setFakeBoldText(true);

        // viewHolder.maintenanceListItemRightTop.setVisibility(View.GONE);
        viewHolder.maintenanceListItemRightTop.setTextColor(Color.parseColor("#773ab7")); //#5677fc
        viewHolder.maintenanceListItemRightTop.setText(hotlineModel.EventName + " - " + hotlineModel.ReporterDepart);

        viewHolder.maintenanceListItemMiddle.setText(hotlineModel.Summary);
        viewHolder.maintenanceListItemLeftBottom.setText(hotlineModel.ReportTime);

        viewHolder.maintenanceListItemDate.setText(TextUtils.isEmpty(hotlineModel.FollowState) ? "跟踪中" : hotlineModel.FollowState);

        viewHolder.maintenanceListItemStatus.setVisibility(View.GONE);
        viewHolder.maintenanceListItemStatusRightTip.setVisibility(View.GONE);

        viewHolder.maintenanceListItemDistance.setText(hotlineModel.DistanceStr);

        return convertView;
    }

    /**
     * 点击定位查看地图位置
     */
    public void onLocation(int position) {
        HotlineModel hotlineModel = dataList.get(position);

        String positionStr = hotlineModel.XY;
        Dot dot = new Dot(Double.valueOf(positionStr.split(",")[0]), Double.valueOf(positionStr.split(",")[1]));

        MyApplication.getInstance().sendToBaseMapHandle(
                new TaskLocationOnMapCallback(dot, hotlineModel.Summary, hotlineModel.EventCode));
        Intent intent = new Intent(activity, MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }

    /**
     * 点击列表项查看详情
     */
    @Override
    public void onItemClick(int position) {
        super.onItemClick(position);

        HotlineModel hotlineModel = dataList.get(position);

        Intent intent = new Intent(activity, OnlineTrackDetailActivity.class);
        intent.putExtra("ListItemEntity", hotlineModel);
        activity.startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
        MyApplication.getInstance().startActivityAnimation(activity);
    }

    final class ViewHolder {
        public TextView maintenanceListItemIndex;

        public TextView maintenanceListItemLeftTop;
        public TextView maintenanceListItemRightTop;
        public TextView maintenanceListItemMiddle;
        public TextView maintenanceListItemLeftBottom;
        public TextView maintenanceListItemDate;
        public TextView maintenanceListItemDistance;

        public TextView maintenanceListItemLocation;
        public TextView maintenanceListItemDetail;

        public ImageView maintenanceListItemStatus;
        public ImageView maintenanceListItemStatusRightTip;
    }

    @Override
    public void notifyDataSetChanged() {

        calcDistance();

        super.notifyDataSetChanged();
    }

    private DecimalFormat decimalFormat = new DecimalFormat(".0");
    private GpsXYZ gpsXYZ;

    private void calcDistance() {

        gpsXYZ = GpsReceiver.getInstance().getLastLocalLocation();

        if (gpsXYZ != null && gpsXYZ.isUsefull()) {
            for (HotlineModel hotlineModel : dataList) {
                hotlineModel.Distance = 0;
                hotlineModel.DistanceStr = "未含有坐标信息";

                try {
                    if (!TextUtils.isEmpty(hotlineModel.XY)) {

                        double x = Double.valueOf(hotlineModel.XY.split(",")[0]);
                        double y = Double.valueOf(hotlineModel.XY.split(",")[1]);
                        double gpsX = gpsXYZ.getX();
                        double gpsY = gpsXYZ.getY();

                        double distance = Math.sqrt((gpsX - x) * (gpsX - x) + (gpsY - y) * (gpsY - y));

                        hotlineModel.Distance = distance;

                        if (distance >= 1000) {
                            hotlineModel.DistanceStr = (decimalFormat.format(distance / 1000)) + "千米";
                        } else {
                            hotlineModel.DistanceStr = ((int) distance) + "米";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (HotlineModel hotlineModel : dataList) {
                hotlineModel.Distance = 0;
                hotlineModel.DistanceStr = "未能获取当前坐标";
            }
        }
    }
}
