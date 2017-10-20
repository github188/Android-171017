package com.repair.zhoushan.module.devicecare;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.SimpleBaseAdapter;

import java.util.ArrayList;

public class ScheduleTaskAdapter extends SimpleBaseAdapter {

    private static final String TAG = "ScheduleTaskAdapter";

    private final ArrayList<ScheduleTask> dataList;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

    // private final List<Integer> idList = new ArrayList<Integer>();
    private final int[] ids = new int[20];
    // GIS编号,设备名称,任务编号,位置,开始时间,结束时间,距离
    private final int[] defWeights = {6, 4, 6, 4, 10, 10, 6, 4};

    // 列表项中最多允许显示的字段的个数
    private final int maxShowColCount = 10;

    public ArrayList<ScheduleTask> getDataList() {
        return dataList;
    }

    public ScheduleTaskAdapter(Context context, ArrayList<ScheduleTask> eventItems) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.dataList = eventItems;

//        final ScheduleTask sampleTask = dataList.get(0);
//        if (sampleTask.MobileRow == null || sampleTask.MobileRow.size() == 0) {
//            int copyColumnCount = sampleTask.WebRow.size() > 6 ? 6 : sampleTask.WebRow.size();
//            for (ScheduleTask scheduleTask : dataList) {
//                scheduleTask.MobileRow = new ArrayList<ScheduleTask.TableColumn>();
//                scheduleTask.MobileRow.addAll(scheduleTask.WebRow.subList(0, copyColumnCount));
//            }
//        }

        for (int i = 0; i < ids.length; i++) {
            ids[i] = (int) ((Math.random() * 9 + 1) * 100000);
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

        int rowFieldCount = scheduleTask.MobileRow.size();

        if (convertView == null) {

            // Log.d(TAG, "Create " + position);

            convertView = mLayoutInflater.inflate(R.layout.devicecare_list_item, null);
            final LinearLayout mainContentView = (LinearLayout) convertView.findViewById(R.id.mainContentLayout);
            mainContentView.setOrientation(LinearLayout.VERTICAL);

            int weightIndex = 0;
            TableColumn tableColumn;
            LinearLayout rowLayout = null;
            LinearLayout.LayoutParams subLp;
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_VERTICAL;

            int weights = 0;
            boolean textAlignRight;
            for (int i = 0; i < rowFieldCount && i < maxShowColCount; i++) {

                tableColumn = scheduleTask.MobileRow.get(i);

                if (tableColumn.Weight == 0) {
                    tableColumn.Weight = defWeights[weightIndex++];
                    if (weightIndex >= defWeights.length)
                        weightIndex %= defWeights.length;
                }
                weights += tableColumn.Weight;

                if (weights > 10) {
                    if (rowLayout.getChildCount() == 1) {
                        TextView tv = (TextView) rowLayout.getChildAt(0);
                        tv.setSingleLine(false);
                        tv.setMaxLines(2);
                    }
                    mainContentView.addView(rowLayout);
                    rowLayout = null;
                    weights = tableColumn.Weight;
                }

                if (rowLayout == null) {
                    rowLayout = new LinearLayout(mContext);
                    rowLayout.setId(rowLayout.hashCode());
                    rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                    rowLayout.setLayoutParams(lp);

                    textAlignRight = false;
                } else {
                    textAlignRight = true;
                }

                TextView rowSubView = new TextView(mContext);
                rowSubView.setId(ids[i]);
                rowSubView.setSingleLine();
                rowSubView.setEllipsize(TextUtils.TruncateAt.END);
                if (textAlignRight) {
                    rowSubView.setGravity(Gravity.RIGHT);
                }
                // Set bold typeface for the first column
                if (i == 0) {
                    rowSubView.getPaint().setFakeBoldText(true);
                }

                // Font size.
                if (!TextUtils.isEmpty(tableColumn.FontSize)) {
                    rowSubView.setTextSize(Integer.valueOf(tableColumn.FontSize));
                } else {
                    if (textAlignRight) {
                        rowSubView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
                    } else {
                        rowSubView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.0f);
                    }
                }

                // Font color.
                if (!TextUtils.isEmpty(tableColumn.FontColor)) {
                    rowSubView.setTextColor(Color.parseColor(tableColumn.FontColor));
                }

                // Font weight.
                subLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                subLp.weight = tableColumn.Weight;
                rowSubView.setLayoutParams(subLp);
                rowSubView.setPadding(DimenTool.dip2px(mContext, 3.0f), DimenTool.dip2px(mContext, 1.0f),
                        DimenTool.dip2px(mContext, 5.0f), DimenTool.dip2px(mContext, 1.0f));

                rowLayout.addView(rowSubView);
            }
            mainContentView.addView(rowLayout);
        }

        ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex)).setText((position + 1) + ".");

        ImageView ivDelay = MmtViewHolder.get(convertView, R.id.iv_delay);
        ivDelay.setVisibility(scheduleTask.HasDelay == 1 ? View.VISIBLE : View.GONE);

        TableColumn tableColumn;
        for (int i = 0; i < rowFieldCount && i < maxShowColCount; i++) {
            tableColumn = scheduleTask.MobileRow.get(i);
            ((TextView) MmtViewHolder.get(convertView, ids[i])).setText(
                    TextUtils.isEmpty(tableColumn.FieldValue) ? tableColumn.FieldName : tableColumn.FieldValue);
        }

        return convertView;
    }
}
