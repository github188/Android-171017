package com.repair.zhoushan.module.projectmanage.threepartconstruct.supervisereporthistory;


import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;

import java.util.ArrayList;

public class SuperviseReportHistoryAdapter extends BaseAdapter {

    protected final ArrayList<ConstructSupervision> data;

    protected final LayoutInflater inflater;
    protected final BaseActivity activity;

    public SuperviseReportHistoryAdapter(ArrayList<ConstructSupervision> data, BaseActivity activity) {
        this.data = data;
        this.activity = activity;
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
            convertView = inflater.inflate(R.layout.supervise_report_list_item, null);
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

        ConstructSupervision itemData = data.get(position);

        viewHolder.maintenanceListItemLocation.setText("定位");
        viewHolder.maintenanceListItemLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (data.get(position).containsKey("XY")
//                        && !BaseClassUtil.isNullOrEmptyString(data.get(position).get("XY"))) {
//                    onLocation(position);
//                } else {
//                    activity.showToast("无效的坐标信息");
//                }
            }
        });

        viewHolder.maintenanceListItemDetail.setText("详情");

        viewHolder.maintenanceListItemDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                switch (detailBtnText) {
//                    case "详情":
//                        onDetailClick(position);
//                        break;
//                    case "办理":
//                        onHandleCaseClick(position);
//                        break;
//                    case "撤回":
//                        onDrawbackClick(position);
//                        break;
//                }
            }
        });

        viewHolder.maintenanceListItemIndex.setText((position + 1) + ".");
        viewHolder.maintenanceListItemLeftTop.setText(itemData.CaseNo);
        // viewHolder.maintenanceListItemRightTop.setTextColor(Color.parseColor("#FF8000")); //#5677fc
        viewHolder.maintenanceListItemRightTop.setText(itemData.Progress);
        viewHolder.maintenanceListItemMiddle.setText(itemData.ReporterName);
//        if (itemData.IsNormal.equalsIgnoreCase("是")) {
//            viewHolder.maintenanceListItemLeftBottom.setText("施工正常");
//        } else {
//           // viewHolder.maintenanceListItemLeftBottom.setText("施工异常");
//        }
        viewHolder.maintenanceListItemLeftBottom.setText("");

        viewHolder.maintenanceListItemDate.setText(itemData.ReportTime);

        viewHolder.maintenanceListItemStatus.setVisibility(View.GONE);
        viewHolder.maintenanceListItemStatusRightTip.setVisibility(View.GONE);


        viewHolder.maintenanceListItemDistance.setText(itemData.Distance + "米");

        return convertView;
    }

    /**
     * 点击定位查看地图位置
     *
     * @param position
     */
    public void onLocation(int position) {

    }

    /**
     * 点击列表项CaseItem查看详情
     *
     * @param position
     */
    public void onDetailClick(int position) {

        Intent intent = new Intent(activity, SuperviseReportHistoryDetailActivity.class);
        intent.putExtra("ConstructSupervision", (ConstructSupervision) getItem(position));

        activity.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(activity);
    }

    /**
     * 刷新列表信息
     */
    public void refreash() {
        notifyDataSetChanged();
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
}
