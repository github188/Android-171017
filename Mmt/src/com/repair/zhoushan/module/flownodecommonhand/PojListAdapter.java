package com.repair.zhoushan.module.flownodecommonhand;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.R;
import com.repair.beihai.poj.hbpoj.module.construct.ConstructReportListAcitvity;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.casemanage.mycase.ShowAreaAndPointMapCallback;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordActivity;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import java.util.List;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class PojListAdapter extends SimpleBaseAdapter {

    private final List<CaseItem> dataList;
    private final LayoutInflater mLayoutInflater;
    private final Activity mContext;
    private final String bizName;
    private final String tableName;

    public PojListAdapter(Activity mActivity, List<CaseItem> eventItems, String bizName, String tableName) {
        this.mContext = mActivity;
        this.mLayoutInflater = LayoutInflater.from(mActivity);
        this.dataList = eventItems;
        this.bizName = bizName;
        this.tableName = tableName;
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
            convertView = mLayoutInflater.inflate(R.layout.case_doing_fb_box_list_item, parent, false);
        }

        CaseItem eventItem = dataList.get(position);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_index)).setText((position + 1) + ".");
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_top_left)).setText(eventItem.CaseNo);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_one_left)).setText(String.format("%s - %s", eventItem.FlowName, eventItem.ActiveName));
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_two_left)).setText(eventItem.Summary);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_time)).setText(eventItem.UnderTakeTime);

        TextView tvDistance = MmtViewHolder.get(convertView, R.id.tv_loc);
        tvDistance.setTag(position);
        tvDistance.setOnClickListener(locClickListener);

        TextView txt_detail = MmtViewHolder.get(convertView, R.id.tv_detail);
        txt_detail.setTag(position);
        txt_detail.setOnClickListener(locClickListener);

        return convertView;
    }

    @Override
    public void onItemClick(int position) {
        super.onItemClick(position);

        String caseno = dataList.get(position).CaseNo;

        Intent intent = new Intent(mContext, ConstructReportListAcitvity.class);
        intent.putExtra("fbBizName", bizName);
        intent.putExtra("caseNo", caseno);
        intent.putExtra("tablename", tableName);

        mContext.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(mContext);

    }

    // 点击定位的事件监听器
    private View.OnClickListener locClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int position = Integer.parseInt(view.getTag().toString());
            CaseItem caseItem = dataList.get(position);

            if (view.getId() == R.id.tv_loc) {
                if (!TextUtils.isEmpty(caseItem.XY)) {
                    BaseMapCallback callback = new ShowAreaAndPointMapCallback(mContext, caseItem.XY, caseItem.GeoArea,
                            caseItem.IsArrive, caseItem.IsFeedback, caseItem.EventCode, caseItem.Summary, -1);

                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                } else {
                    Toast.makeText(mContext, "无效的坐标信息", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            if (view.getId() == R.id.tv_detail) {

                Intent intent = new Intent(mContext, TableOneRecordActivity.class);
                intent.putExtra("tableName", caseItem.EventMainTable);
                intent.putExtra("key", "事件编号");
                intent.putExtra("value", caseItem.EventCode);
                intent.putExtra("viewMode", TabltViewMode.READ.getTableViewMode());
                mContext.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(mContext);
            }
        }
    };

}
