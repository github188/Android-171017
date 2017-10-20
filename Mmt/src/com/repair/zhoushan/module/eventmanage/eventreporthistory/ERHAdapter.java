package com.repair.zhoushan.module.eventmanage.eventreporthistory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.entity.EventItem;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.eventmanage.eventoverview.EventDetailActivity;

import java.util.ArrayList;

public class ERHAdapter extends SimpleBaseAdapter {

    private final ArrayList<EventItem> dataList;
    private final LayoutInflater mLayoutInflater;
    private final Activity mContext;

    private final String template = "<font color=#808A87>%s</font><font color=#292421>%s</font>";

    public ERHAdapter(Activity mActivity, ArrayList<EventItem> eventItems) {
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
        return this.dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.eventreport_history_list_item, parent, false);
        }

        EventItem eventItem = dataList.get(position);

        ((TextView) MmtViewHolder.get(convertView, R.id.eventReportListItemIndex)).setText((position + 1) + ".");

        ((TextView) MmtViewHolder.get(convertView, R.id.txtEventCode)).setText(
                Html.fromHtml(String.format(template, "编号：", eventItem.EventCode)));
        ((TextView) MmtViewHolder.get(convertView, R.id.txtEventName)).setText(
                Html.fromHtml(String.format(template, "名称：", eventItem.EventName)));
        ((TextView) MmtViewHolder.get(convertView, R.id.txtEventSummary)).setText(
                Html.fromHtml(String.format(template, "状态：", eventItem.EventState)));
        ((TextView) MmtViewHolder.get(convertView, R.id.addressText)).setText(
                Html.fromHtml(String.format(template, "时间：", eventItem.ReportTime)));
        ((TextView) MmtViewHolder.get(convertView, R.id.reportTimeText)).setText("摘要：");
        ((TextView) MmtViewHolder.get(convertView, R.id.line5right)).setText(
                TextUtils.isEmpty(eventItem.Summary) ? "暂无描述" : eventItem.Summary);

        TextView stateText = MmtViewHolder.get(convertView, R.id.stateText);
        stateText.setVisibility(View.GONE);
        if (eventItem.IsRelatedCase == 0) {
            stateText.setText("可撤回");
            stateText.setTextColor(Color.parseColor("#eeff0000"));
            stateText.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public void onItemClick(int position) {
        super.onItemClick(position);

        Intent intent = new Intent(mContext, EventDetailActivity.class);
        intent.putExtra("SourceFlag", "EventReportHistory");
        intent.putExtra("ListItemEntity", dataList.get(position));

        mContext.startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
        MyApplication.getInstance().startActivityAnimation(mContext);
    }
}
