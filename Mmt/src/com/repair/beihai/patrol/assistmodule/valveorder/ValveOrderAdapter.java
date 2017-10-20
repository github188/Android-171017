package com.repair.beihai.patrol.assistmodule.valveorder;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;

import java.util.ArrayList;

public class ValveOrderAdapter extends RecyclerView.Adapter<ValveOrderAdapter.ViewHolder> implements View.OnClickListener {

    private final ArrayList<ValveModel> dataList;
    private final LayoutInflater inflater;
    private final Activity context;
    private final boolean isOpen;

    public ValveOrderAdapter(Activity context, ArrayList<ValveModel> dataList, boolean isOpen) {
        this.dataList = dataList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.isOpen = isOpen;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.case_doingbox_list_item, parent, false);
        return new ValveOrderAdapter.ViewHolder(view);
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
        int clickPos = Integer.parseInt(view.getTag().toString());
        final ValveModel valveModel = dataList.get(clickPos);

        if (onListItemClickListener != null) {
            onListItemClickListener.onItemClicked(valveModel);
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

            final ValveModel valveModel = dataList.get(position);

            itemView.setTag(position);
            itemView.setOnClickListener(ValveOrderAdapter.this);

            tvIndex.setText((position + 1) + ".");
            tvCaseNo.setText(valveModel.no);

            if (!TextUtils.isEmpty(valveModel.state)) {
                tvTag1.setVisibility(View.VISIBLE);
                tvTag1.setText(valveModel.state);
            } else {
                tvTag1.setVisibility(View.GONE);
            }
            tvTag2.setVisibility(View.GONE);
            tvTag3.setVisibility(View.GONE);

            tvCurrentNode.setText(valveModel.gisLayer + " - " + valveModel.specification + " - " + valveModel.address);

            tvCurrentNode.getPaint().setFakeBoldText(true);
            // String desc = isOpen ? valveModel.openDesc : valveModel.closeDesc;
            String desc = TextUtils.isEmpty(valveModel.closeDesc) ? "暂无描述" : valveModel.closeDesc;
            tvSummary.setText(desc);

            tvUnderTakeTime.setText(valveModel.closeTime);

            // Coordinate
            if (!TextUtils.isEmpty(valveModel.coordinate)) {
                tvDistance.setVisibility(View.VISIBLE);
                tvDistance.setText("");
                tvDistance.setTag(position);
                tvDistance.setOnClickListener(locClickListener);
            } else {
                tvDistance.setVisibility(View.INVISIBLE);
            }
        }
    }

    // 点击定位的事件监听器
    private View.OnClickListener locClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int position = Integer.parseInt(view.getTag().toString());
            ValveModel valveModel = dataList.get(position);

            if (!TextUtils.isEmpty(valveModel.coordinate)) {
                onLocation(position);
            } else {
                Toast.makeText(context, "无效的坐标信息", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void onLocation(int position) {
        ValveModel valveModel = dataList.get(position);
        BaseMapCallback callback = new ShowMapPointCallback(context, valveModel.coordinate,
                valveModel.no, valveModel.state + " - " + valveModel.gisLayer, -1);
        MyApplication.getInstance().sendToBaseMapHandle(callback);
    }

    private ValveOrderAdapter.OnListItemClickListener onListItemClickListener;

    public void setOnListItemClickListener(ValveOrderAdapter.OnListItemClickListener onListItemClickListener) {
        this.onListItemClickListener = onListItemClickListener;
    }

    public interface OnListItemClickListener {
        void onItemClicked(ValveModel valveModel);
    }
}
