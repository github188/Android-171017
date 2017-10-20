package com.repair.beihai.poj.gdpoj.module.construct;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.R;
import com.repair.beihai.poj.gdpoj.entity.GDConstructEvent;
import com.repair.beihai.poj.hbpoj.module.construct.ConstructReportListAcitvity;
import com.repair.zhoushan.module.SimpleBaseAdapter;

import java.util.List;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class GDConstructEventAdapter extends SimpleBaseAdapter {

    private final List<GDConstructEvent> dataList;
    private final LayoutInflater mLayoutInflater;
    private final Activity mContext;
    private final String constructType;
    private final String tableName;

    public GDConstructEventAdapter(Activity mActivity, List<GDConstructEvent> eventItems,String constructType,String tableName) {
        this.mContext = mActivity;
        this.mLayoutInflater = LayoutInflater.from(mActivity);
        this.dataList = eventItems;
        this.constructType=constructType;
        this.tableName=tableName;
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

        GDConstructEvent eventItem = dataList.get(position);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_index)).setText((position + 1) + ".");
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_top_left)).setText(eventItem.caseno);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_one_left)).setText("工程名称：" + eventItem.pojName);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_two_left)).setText("上报人：" + eventItem.reportMan);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_three_left)).setText("工程性质：" + eventItem.pojType);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_four_left)).setText("立项日期：" + eventItem.startTime);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_address)).setText(eventItem.pojAdds);

        return convertView;
    }

    @Override
    public void onItemClick(int position) {
        super.onItemClick(position);

        String caseno = dataList.get(position).caseno;

        Intent intent = new Intent(mContext, ConstructReportListAcitvity.class);
        intent.putExtra("fbBizName", constructType);
        intent.putExtra("caseNo", caseno);
        intent.putExtra("tablename", tableName);

        mContext.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(mContext);

    }
}
