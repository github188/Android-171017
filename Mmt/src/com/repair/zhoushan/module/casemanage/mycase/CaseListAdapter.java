package com.repair.zhoushan.module.casemanage.mycase;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.R;
import com.repair.zhoushan.entity.CaseItem;

import java.util.ArrayList;

public class CaseListAdapter extends RecyclerView.Adapter<CaseListAdapter.ViewHolder> implements View.OnClickListener {

    private final ArrayList<CaseItem> dataList;
    private final LayoutInflater inflater;
    private final Activity context;

    // 展示事件编号或工单编号
    private final boolean showEventCode;

    /**
     * 标记当前的列表排序方式，以控制相应字段的显示透明度
     */
    protected int index = 0;

    /**
     * 当前点击的item的位置，方便从list中移除该item
     */
    private int curClickPos = 0;



    public int getCurrentClickPosition() {
        return this.curClickPos;
    }

    public CaseItem getItem(int position) {
        if (position >= 0 && position < dataList.size()) {
            return dataList.get(position);
        }
        return null;
    }

    public CaseListAdapter(Activity activity, ArrayList<CaseItem> dataList, boolean showEventCode) {
        this.dataList = dataList;
        this.context = activity;
        this.inflater = LayoutInflater.from(context);
        this.showEventCode = showEventCode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.case_doingbox_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onClick(View view) {

        curClickPos = Integer.parseInt(view.getTag().toString());
        final CaseItem caseItem = dataList.get(curClickPos);

        if (onListItemClickListener != null) {
            onListItemClickListener.onItemClicked(caseItem);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvIndex;
        TextView tvCaseNo;

        TextView tvTag1;
        TextView tvTag2;
        TextView tvTag3;

        TextView tvCurrentNode;
        TextView tvOverTimeInfo;
        TextView tvSummary;
        TextView tvUnderTakeTime;
        TextView tvDistance;

        public ViewHolder(View itemView) {
            super(itemView);

            tvIndex = (TextView) itemView.findViewById(R.id.tv_index);
            tvCaseNo = (TextView) itemView.findViewById(R.id.tv_top_left);

            tvTag1 = (TextView) itemView.findViewById(R.id.tv_top_right1);
            tvTag2 = (TextView) itemView.findViewById(R.id.tv_top_right2);
            tvTag3 = (TextView) itemView.findViewById(R.id.tv_top_right3);

            tvCurrentNode = (TextView) itemView.findViewById(R.id.tv_mid_one_left);
            tvOverTimeInfo = (TextView) itemView.findViewById(R.id.tv_mid_one_right);
            tvSummary = (TextView) itemView.findViewById(R.id.tv_mid_two_left);

            tvUnderTakeTime = (TextView) itemView.findViewById(R.id.tv_time);
            tvDistance = (TextView) itemView.findViewById(R.id.tv_loc);
        }

        void bindData(final int position) {

            final CaseItem caseItem = dataList.get(position);

            itemView.setTag(position);
            itemView.setOnClickListener(CaseListAdapter.this);

            tvIndex.setText((position + 1) + ".");
            tvCaseNo.setText(showEventCode ? caseItem.EventCode : caseItem.CaseNo);

            if (TextUtils.isEmpty(caseItem.ReadCaseTime)) {
                tvTag1.setVisibility(View.VISIBLE);
                tvTag1.setText("新");
            } else {
                tvTag1.setVisibility(View.GONE);
            }
            if ("-1".equals(caseItem.Direction)) {
                tvTag2.setVisibility(View.VISIBLE);
                tvTag2.setText("退");
            } else {
                tvTag2.setVisibility(View.GONE);
            }
            if (caseItem.IsFeedback == 1) {
                tvTag3.setVisibility(View.VISIBLE);
                tvTag3.setText("已反馈");
            } else {
                tvTag3.setVisibility(View.GONE);
            }

            tvCurrentNode.setText(String.format("%s - %s", caseItem.FlowName, caseItem.ActiveName));
            tvCurrentNode.getPaint().setFakeBoldText(true);
            tvSummary.setText(caseItem.Summary);
            tvUnderTakeTime.setText(caseItem.UnderTakeTime);

            // Coordinate
            if (!TextUtils.isEmpty(caseItem.XY)) {
                tvDistance.setVisibility(View.VISIBLE);
                tvDistance.setText(caseItem.DistanceStr);
                tvDistance.setTag(position);
                tvDistance.setOnClickListener(locClickListener);
            } else {
                tvDistance.setVisibility(View.INVISIBLE);
            }

            // Overtime info
            if (!TextUtils.isEmpty(caseItem.IsOverTime)) {
                try {
                    int index = caseItem.OverTimeInfo.indexOf("：");
                    String overTimeInfo = caseItem.OverTimeInfo.substring(index + 1);
                    if ("1".equals(caseItem.IsOverTime)) {
                        overTimeInfo = "超时" + overTimeInfo;
                        tvOverTimeInfo.setTextColor(Color.RED);
                    } else if ("0".equals(caseItem.IsOverTime)) {
                        tvOverTimeInfo.setTextColor(Color.DKGRAY);
                    }
                    tvOverTimeInfo.setText(overTimeInfo);
                    tvOverTimeInfo.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                tvOverTimeInfo.setText("");
                tvOverTimeInfo.setVisibility(View.INVISIBLE);
            }
        }
    }

    // 点击定位的事件监听器
    private View.OnClickListener locClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int position = Integer.parseInt(view.getTag().toString());
            CaseItem caseItem = dataList.get(position);

            if (!TextUtils.isEmpty(caseItem.XY)) {
                onLocation(position);
            } else {
                Toast.makeText(context, "无效的坐标信息", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 点击定位查看地图位置
     */
    public void onLocation(int position) {
        CaseItem caseItem = dataList.get(position);

//        BaseMapCallback callback = new ShowMapPointCallback(activity, caseItem.XY, caseItem.EventCode, caseItem.Summary, -1);
        BaseMapCallback callback = new ShowAreaAndPointMapCallback(context, caseItem.XY, caseItem.GeoArea,
                caseItem.IsArrive, caseItem.IsFeedback, caseItem.EventCode, caseItem.Summary, -1);

        MyApplication.getInstance().sendToBaseMapHandle(callback);
    }

    /**
     * 刷新列表信息
     */
    public void refresh(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < getItemCount()) {
            dataList.remove(position);
            notifyDataSetChanged();
        }
    }

    private OnListItemClickListener onListItemClickListener;
    public void setOnListItemClickListener(OnListItemClickListener onListItemClickListener) {
        this.onListItemClickListener = onListItemClickListener;
    }

    public interface OnListItemClickListener {
        void onItemClicked(CaseItem caseItem);
    }

}
