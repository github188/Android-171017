package com.repair.zhoushan.module.devicecare.patrolmonitor;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.R;
import com.repair.zhoushan.entity.CrashPointRecord;
import com.repair.zhoushan.module.SimpleBaseAdapter;

import java.util.List;

/**
 * Created by liuyunfan on 2016/7/6.
 */
public class TouchListAdapter extends SimpleBaseAdapter implements View.OnClickListener {

    protected List<CrashPointRecord> dataList;
    protected BaseActivity context;
    protected LayoutInflater inflater;

    public TouchListAdapter(BaseActivity activity, List<CrashPointRecord> data) {
        this.inflater = LayoutInflater.from(activity);
        this.context = activity;
        this.dataList = data;
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.touch_list_item, parent, false);
        }

        final CrashPointRecord caseItem = dataList.get(position);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_index))
                .setText(String.valueOf(position + 1));

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_top_left)).setText(caseItem.CaseNO);

        TextView tvHandleNode = MmtViewHolder.get(convertView, R.id.tv_mid_one_left);
        // tvHandleNode.getPaint().setFakeBoldText(true);
        tvHandleNode.setText("碰接现场安全措施:" + caseItem.CrashSafeMethod);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_two_left))
                .setText("异常情况记录: " + caseItem.ErrorRecord);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_three_left)).setText("环境勘查: " + caseItem.EnvironemntProspect);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_four_left)).setText("情况说明:" + caseItem.Remark);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_time)).setText(caseItem.CrashPointPosition);
        TextView tvLoc = MmtViewHolder.get(convertView, R.id.tv_loc);
        tvLoc.setTag(caseItem.CrashPointPosition);
        tvLoc.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View v) {

        // 定位按钮
        if (v.getId() == R.id.tv_loc) {

            final Object tag = v.getTag();
            if (tag == null || TextUtils.isEmpty(tag.toString())) {
                Toast.makeText(context, "无坐标信息", Toast.LENGTH_SHORT).show();
            } else {
                BaseMapCallback callback = new ShowMapPointCallback(context, tag.toString(), "", "", -1);
                MyApplication.getInstance().sendToBaseMapHandle(callback);
            }
        }
    }
}