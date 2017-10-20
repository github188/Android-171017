package com.repair.beihai.poj.hbpoj.module.construct;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.R;
import com.repair.beihai.poj.hbpoj.entity.ConstructEvent;
import com.repair.zhoushan.module.SimpleBaseAdapter;

import java.util.List;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class ConstructEventAdapter extends SimpleBaseAdapter {

    private final List<ConstructEvent> dataList;
    private final LayoutInflater mLayoutInflater;
    private final Activity mContext;
    private String constructType;

    public ConstructEventAdapter(Activity mActivity, List<ConstructEvent> eventItems, String constructType) {
        this.mContext = mActivity;
        this.mLayoutInflater = LayoutInflater.from(mActivity);
        this.dataList = eventItems;
        this.constructType = constructType;
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
            convertView = mLayoutInflater.inflate(R.layout.bh_constructevent_list_item, parent, false);
        }

        ConstructEvent eventItem = dataList.get(position);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_index)).setText((position + 1) + ".");
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_top_left)).setText(eventItem.caseno);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_top_right)).setText(eventItem.fbCount);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_one_left)).setText("工程名称：" + eventItem.pojName);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_two_left)).setText("申报人姓名：" + eventItem.applyMan);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_three_left)).setText("申报业务类型：" + eventItem.applyBizType);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_four_left)).setText("业务性质：" + eventItem.bizType);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_five_left)).setText("设计图编号：" + eventItem.designNo);


        ((TextView) MmtViewHolder.get(convertView, R.id.tv_address)).setText(eventItem.pojAdds);

        return convertView;
    }

    @Override
    public void onItemClick(int position) {
        super.onItemClick(position);

        String caseno = dataList.get(position).caseno;
        String tableName = "";
        if ("户表报装施工监管".equals(constructType)) {
            tableName = "户表报装施工监管表";
        }
        if ("户表报装施工进度".equals(constructType)) {
            tableName = "户表报装施工进度表";
        }
        if ("户表报装施工过程".equals(constructType)) {
            tableName = "户表报装施工过程表";
        }

        Intent intent = new Intent(mContext, ConstructReportListAcitvity.class);
        intent.putExtra("fbBizName", constructType);
        intent.putExtra("caseNo", caseno);
        intent.putExtra("tablename", tableName);

        mContext.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(mContext);

    }
}
