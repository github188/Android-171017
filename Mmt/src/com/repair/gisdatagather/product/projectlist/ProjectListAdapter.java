package com.repair.gisdatagather.product.projectlist;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.R;
import com.repair.zhoushan.entity.CaseItem;

import java.util.ArrayList;

public class ProjectListAdapter extends BaseAdapter {

    protected final ArrayList<CaseItem> dataList;

    protected final LayoutInflater inflater;
    protected final BaseActivity activity;

    /**
     * 标记当前的列表排序方式，以控制相应字段的显示透明度
     */
    protected int index = 0;

    /**
     * 当前点击的item的位置，方便从list中移除该item
     */
    public int curClickPos = 0;
    private boolean isdoingBox = false;

    public ProjectListAdapter(ArrayList<CaseItem> data, BaseActivity activity, boolean isdoingBox) {
        this.dataList = data;
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity);
        this.isdoingBox = isdoingBox;
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
            convertView = inflater.inflate(R.layout.gis_project_list_item, null);
            viewHolder = new ViewHolder();

            viewHolder.maintenanceListItemIndex = (TextView) convertView.findViewById(R.id.maintenanceListItemIndex);

            viewHolder.maintenanceListItemLeftTop = (TextView) convertView.findViewById(R.id.maintenanceListItemLeftTop);
            viewHolder.maintenanceListItemRightTop = (TextView) convertView.findViewById(R.id.maintenanceListItemRightTop);
            viewHolder.maintenanceListItemMiddle = (TextView) convertView.findViewById(R.id.maintenanceListItemMiddle);
            viewHolder.maintenanceListItemLeftBottom = (TextView) convertView.findViewById(R.id.maintenanceListItemLeftBottom);
            viewHolder.maintenanceListItemDate = (TextView) convertView.findViewById(R.id.maintenanceListItemDate);

            viewHolder.maintenanceListItemStatus = (ImageView) convertView.findViewById(R.id.ivStatus);
            viewHolder.maintenanceListItemStatusRightTip = (ImageView) convertView.findViewById(R.id.ivStatusRightTip);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final CaseItem caseItem = dataList.get(position);

        viewHolder.maintenanceListItemIndex.setText((position + 1) + ".");

        viewHolder.maintenanceListItemLeftTop.setText(caseItem.CaseNo);
        viewHolder.maintenanceListItemLeftTop.getPaint().setFakeBoldText(true);
        viewHolder.maintenanceListItemRightTop.setTextColor(Color.parseColor("#773ab7")); //#5677fc
        if (isdoingBox) {
            viewHolder.maintenanceListItemRightTop.setText(caseItem.FlowName + " - " + caseItem.ActiveName);
        } else {
            viewHolder.maintenanceListItemRightTop.setText(caseItem.FlowName);
        }
        viewHolder.maintenanceListItemMiddle.setText(caseItem.Summary);
        viewHolder.maintenanceListItemLeftBottom.setText(caseItem.UnderTakeTime);

        String eventState = caseItem.EventState;
        viewHolder.maintenanceListItemDate.setText(eventState);

        viewHolder.maintenanceListItemStatus.setVisibility(View.GONE);
        viewHolder.maintenanceListItemStatusRightTip.setVisibility(View.GONE);

        if (isdoingBox && BaseClassUtil.isNullOrEmptyString(caseItem.ReadCaseTime)) {
            viewHolder.maintenanceListItemStatusRightTip.setImageResource(R.drawable.right_tip_new);
            viewHolder.maintenanceListItemStatusRightTip.setVisibility(View.VISIBLE);
        }

        switch (eventState) {
            case "已办结":
                viewHolder.maintenanceListItemDate.setText("");
                viewHolder.maintenanceListItemStatus.setVisibility(View.VISIBLE);
                break;
            case "退单":
                viewHolder.maintenanceListItemStatusRightTip.setVisibility(View.VISIBLE);
                break;
        }
        return convertView;
    }

    final class ViewHolder {
        public TextView maintenanceListItemIndex;

        public TextView maintenanceListItemLeftTop;
        public TextView maintenanceListItemRightTop;
        public TextView maintenanceListItemMiddle;
        public TextView maintenanceListItemLeftBottom;
        public TextView maintenanceListItemDate;

        public ImageView maintenanceListItemStatus;
        public ImageView maintenanceListItemStatusRightTip;
    }
}
