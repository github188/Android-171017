package com.repair.zhoushan.module.devicecare.careoverview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.TableColumn;
import com.repair.zhoushan.module.devicecare.careoverview.detail.CareOverviewDetailActivity;

import java.util.ArrayList;

public class CareOverviewListAdapter extends BaseAdapter {

    private final ArrayList<ScheduleTask> dataList;
    private final LayoutInflater inflater;
    private final Context mContext;

    private final int[] ids = new int[20];

    public CareOverviewListAdapter(Context context, ArrayList<ScheduleTask> dataList) {
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
        this.dataList = dataList;

        for (int i = 0; i < ids.length; i++) {
            ids[i] = BaseClassUtil.generateViewId();
        }
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

        final ScheduleTask scheduleTask = dataList.get(position);
        final int rowFieldCount = scheduleTask.WebRow.size();

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.devicecare_list_item, parent, false);
            convertView.setBackgroundColor(Color.WHITE);
            final LinearLayout mainContentView = (LinearLayout) convertView.findViewById(R.id.mainContentLayout);
            mainContentView.setOrientation(LinearLayout.VERTICAL);

            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_VERTICAL;

            for (int i = 0; i < rowFieldCount; i++) {

                TextView rowView = new TextView(mContext);
                rowView.setId(ids[i]);
                rowView.setSingleLine();
                rowView.setEllipsize(TextUtils.TruncateAt.END);
                rowView.setLayoutParams(lp);

                mainContentView.addView(rowView);
            }
        }

        ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex))
                .setText(mContext.getString(R.string.string_listitem_index, (position + 1)));
        TableColumn row;
        for (int i = 0; i < rowFieldCount; i++) {
            row = scheduleTask.WebRow.get(i);
            TextView tv = MmtViewHolder.get(convertView, ids[i]);
            tv.setText(mContext.getString(R.string.string_two_with_colon, row.FieldName, row.FieldValue));
        }

        return convertView;
    }

    public void onItemSelected(int position) {

        final ScheduleTask scheduleTask = dataList.get(position);

        Intent intent = new Intent(mContext, CareOverviewDetailActivity.class);
        intent.putExtra("ScheduleTask", scheduleTask);
        mContext.startActivity(intent);
    }

}
