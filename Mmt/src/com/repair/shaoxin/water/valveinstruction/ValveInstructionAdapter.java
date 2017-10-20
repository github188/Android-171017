package com.repair.shaoxin.water.valveinstruction;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.Constants;

import java.util.ArrayList;
import java.util.Collections;

public class ValveInstructionAdapter extends RecyclerView.Adapter<ValveInstructionAdapter.ViewHolder> implements View.OnClickListener {

    private final ArrayList<ValveModel> dataList;
    private final LayoutInflater inflater;
    private final BaseActivity activity;

    // 当前点击的item的位置，方便从list中移除该item
    private int curClickPos = 0;

    public int getCurrentClickPosition() {
        return this.curClickPos;
    }

    public ValveModel getItem(int position) {
        if (position >= 0 && position < dataList.size()) {
            return dataList.get(position);
        }
        return null;
    }

    public ArrayList<ValveModel> getDataList() {
        return dataList;
    }

    public ValveInstructionAdapter(BaseActivity activity, ArrayList<ValveModel> dataList) {
        this.dataList = dataList;
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity);
    }

    @Override
    public ValveInstructionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.case_doingbox_list_item, parent, false);
        return new ValveInstructionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ValveInstructionAdapter.ViewHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onClick(View view) {

        curClickPos = Integer.parseInt(view.getTag().toString());

        ValveModel valveModel = dataList.get(curClickPos);

        Intent intent = new Intent(activity, ValveDevicePropertyActivity.class);
        intent.putExtra("ListItemEntity", valveModel);
        activity.startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
        MyApplication.getInstance().startActivityAnimation(activity);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvIndex;
        TextView tvCaseNo;

        TextView tvTag1;
        TextView tvTag2;
        TextView tvTag3;

        TextView tvCurrentNode;
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
            tvSummary = (TextView) itemView.findViewById(R.id.tv_mid_two_left);

            tvUnderTakeTime = (TextView) itemView.findViewById(R.id.tv_time);
            tvDistance = (TextView) itemView.findViewById(R.id.tv_loc);
        }

        void bindData(final int position) {

            final ValveModel valveModel = dataList.get(position);

            itemView.setTag(position);
            itemView.setOnClickListener(ValveInstructionAdapter.this);

            tvIndex.setText((position + 1) + ".");
            tvCaseNo.setText(valveModel.valveCode);

            tvTag1.setVisibility(View.GONE);
            tvTag2.setVisibility(View.GONE);

            tvCurrentNode.setText(String.format("%s - %s - %s",
                    valveModel.introductionContent, valveModel.gisLayer, valveModel.size));
            tvCurrentNode.getPaint().setFakeBoldText(true);
            tvSummary.setText(valveModel.location + " - " + valveModel.gisCode);
            tvUnderTakeTime.setText("");

            tvDistance.setText("");
            tvDistance.setTag(position);
            tvDistance.setOnClickListener(locClickListener);
        }
    }

    // 点击定位的事件监听器
    private View.OnClickListener locClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int position = Integer.parseInt(view.getTag().toString());
            ValveModel caseItem = dataList.get(position);

            if (!TextUtils.isEmpty(caseItem.X + caseItem.Y)) {
                onLocation(position);
            } else {
                Toast.makeText(activity, "无效的坐标信息", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 点击定位查看地图位置
     */
    public void onLocation(int position) {

        ValveModel valveModel = dataList.get(position);
        ArrayList<ValveModel> valveModels = new ArrayList<>(Collections.singletonList(valveModel));
        ShowAllValveCallback callback = new ShowAllValveCallback(activity, valveModels);

//        String xy = valveModel.X + "," + valveModel.Y;
//        BaseMapCallback callback = new ShowMapPointCallback(activity, xy, valveModel.valveCode, valveModel.gisLayer, -1);
//        BaseMapCallback callback = new ShowAreaAndPointMapCallback(context, valveModel.XY, valveModel.GeoArea,
//                valveModel.IsArrive, valveModel.IsFeedback, valveModel.EventCode, valveModel.Summary, -1);

        MyApplication.getInstance().sendToBaseMapHandle(callback);
    }
}
