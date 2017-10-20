package com.repair.zhoushan.common;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.devicecare.TableColumn;

import java.util.List;

/**
 * Created by liuyunfan on 2016/3/23.
 */
public class ConfigFieldsAdapter extends SimpleBaseAdapter {


    private final List<List<TableColumn>> dataList;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

    // private final List<Integer> idList = new ArrayList<Integer>();
    private final int[] ids = new int[20];
    // GIS编号,设备名称,任务编号,位置,开始时间,结束时间,距离
    private final int[] defWeights = {6, 4, 6, 4, 10, 10, 6, 4};

    // 列表项中最多允许显示的字段的个数
    private final int maxShowColCount = 10;

    public ConfigFieldsAdapter(Context context, List<List<TableColumn>> eventItems) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.dataList = eventItems;

//        final ScheduleTask sampleTask = dataList.get(0);
//        if (sampleTask.MobileRow == null || sampleTask.MobileRow.size() == 0) {
//
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

        List<TableColumn> mobileRow = dataList.get(position);

        int rowFieldCount = mobileRow.size();

        if (convertView == null) {

            // Log.d(TAG, "Create " + position);

            convertView = mLayoutInflater.inflate(R.layout.devicecare_list_item, null);
            convertView.setBackgroundColor(Color.WHITE);
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
            boolean textAlignRight = false;
            for (int i = 0; i < rowFieldCount && i < maxShowColCount; i++) {

                tableColumn = mobileRow.get(i);

                if (tableColumn.Weight == 0) {
                    tableColumn.Weight = defWeights[weightIndex++];
                    if (weightIndex >= defWeights.length)
                        weightIndex %= defWeights.length;
                }
                weights += tableColumn.Weight;

                if (weights > 10) {
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

                // Font size.
                if (!TextUtils.isEmpty(tableColumn.FontSize)) {
                    rowSubView.setTextSize(Integer.valueOf(tableColumn.FontSize));
                } else {
                    rowSubView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f);
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

        TableColumn tableColumn;
        for (int i = 0; i < rowFieldCount && i < maxShowColCount; i++) {
            tableColumn = mobileRow.get(i);
            ((TextView) MmtViewHolder.get(convertView, ids[i])).setText(
                    TextUtils.isEmpty(tableColumn.FieldValue) ? tableColumn.FieldName : tableColumn.FieldValue);
        }

        return convertView;
    }
}