package com.repair.zhoushan.module.eventmanage.eventoverview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.entity.EventItem;

import java.util.ArrayList;

public class EventAdapter extends BaseAdapter {

    private final ArrayList<EventItem> dataList;
    private final LayoutInflater mLayoutInflater;
    private final Activity mContext;


    private int curClickPos;

    public int getCurClickPos() {
        return curClickPos;
    }

    public EventAdapter(Activity mActivity, ArrayList<EventItem> eventItems) {
        this.mContext = mActivity;
        this.mLayoutInflater = LayoutInflater.from(mActivity);
        this.dataList = eventItems;
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

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.case_list_item, null);
        }
        convertView.setBackgroundColor(Color.WHITE);

        final EventItem eventItem = dataList.get(position);

        ((TextView) MmtViewHolder.get(convertView, R.id.maintenanceListItemIndex)).setText((position + 1) + ".");

        TextView eventListItemLeftTop = MmtViewHolder.get(convertView, R.id.maintenanceListItemLeftTop);
        eventListItemLeftTop.getPaint().setFakeBoldText(true);
        eventListItemLeftTop.setText(eventItem.EventCode);

        TextView eventListItemRightTop = MmtViewHolder.get(convertView, R.id.maintenanceListItemRightTop);
        eventListItemRightTop.setTextColor(Color.parseColor("#773ab7"));
        eventListItemRightTop.setText(eventItem.EventName + "-" + eventItem.ReporterDepart + "-" + eventItem.ReporterName);

        ((TextView) MmtViewHolder.get(convertView, R.id.maintenanceListItemMiddle)).setText(eventItem.Summary);
        ((TextView) MmtViewHolder.get(convertView, R.id.maintenanceListItemLeftBottom)).setText(eventItem.UpdateTime);
        ((TextView) MmtViewHolder.get(convertView, R.id.maintenanceListItemDate)).setText(eventItem.EventState);
        ((TextView) MmtViewHolder.get(convertView, R.id.maintenanceListItemLocation)).setText("定位");
        ((TextView) MmtViewHolder.get(convertView, R.id.maintenanceListItemDetail)).setText("详情");
        ((TextView) MmtViewHolder.get(convertView, R.id.maintenanceListItemDistance)).setText(eventItem.DistanceStr);

        TextView updateStateTxt = MmtViewHolder.get(convertView, R.id.maintenanceListItemMiddleBelow);
        updateStateTxt.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(eventItem.UpdateState)) {
            updateStateTxt.setVisibility(View.VISIBLE);
            updateStateTxt.setText(eventItem.UpdateState);
            updateStateTxt.setTextColor(Color.RED);
        }


        if (eventItem.IsStick == 1) {
            ImageView rightTip = MmtViewHolder.get(convertView, R.id.ivStatusRightTip);
            rightTip.setImageResource(R.drawable.right_tip_top);
            rightTip.setVisibility(View.VISIBLE);
        } else {
            MmtViewHolder.get(convertView, R.id.ivStatusRightTip).setVisibility(View.GONE);
        }

        MmtViewHolder.get(convertView, R.id.maintenanceListItemDetail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDetailClick(position);
            }
        });

        MmtViewHolder.get(convertView, R.id.maintenanceListItemLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BaseClassUtil.isNullOrEmptyString(eventItem.XY)) {
                    onLocation(position);
                } else {
                    Toast.makeText(mContext, "无效的坐标信息", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return convertView;
    }

    private void onLocation(int position) {

        String positionStr = dataList.get(position).XY;
        BaseMapCallback callback = new ShowMapPointCallback(mContext, positionStr, "", "", -1);
        MyApplication.getInstance().sendToBaseMapHandle(callback);
    }

    public void onDetailClick(int position) {

        this.curClickPos = position;

        Intent intent = new Intent(mContext, EventDetailActivity.class);
        intent.putExtra("ListItemEntity", dataList.get(position));
        mContext.startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
        MyApplication.getInstance().startActivityAnimation(mContext);
    }
}
