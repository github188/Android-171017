package com.repair.zhoushan.module.devicecare.carehistory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.TableColumn;
import com.repair.zhoushan.module.devicecare.TaskDetailActivity;

import java.util.ArrayList;

public class CareHistoryAdapter extends SimpleBaseAdapter {

    private final ArrayList<ScheduleTask> dataList;
    private final LayoutInflater inflater;
    private final Activity mContext;

    private final int[] ids = new int[20];

    public CareHistoryAdapter(Activity context, ArrayList<ScheduleTask> eventItems) {
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
        this.dataList = eventItems;

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

            // tv.setText(mContext.getString(R.string.string_two_with_colon, row.FieldName, row.FieldValue));
            tv.setText(Html.fromHtml(String.format("<font color=#808A87>%s : </font><font color=#292421>%s</font>", row.FieldName, row.FieldValue)));

        }

        return convertView;
    }

    @Override
    public void onItemClick(int position) {
        super.onItemClick(position);

        Intent intent = new Intent(mContext, TaskDetailActivity.class);
        intent.putExtra("ListItemEntity", (ScheduleTask) getItem(position));
        intent.putExtra("ComeFrom", TaskDetailActivity.Source.FromCareHistory);

        mContext.startActivity(intent);
    }
}
