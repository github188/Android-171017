package com.repair.beihai.poj.hbpoj.module.userwatermeter;

import android.content.Intent;
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
import com.repair.beihai.poj.hbpoj.entity.WatermeterPoj;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordActivity;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import java.util.List;

/**
 * Created by liuyunfan on 2016/7/6.
 */
public class UserWatermeterPojAdapter extends SimpleBaseAdapter implements View.OnClickListener {

    protected List<WatermeterPoj> dataList;
    protected BaseActivity context;
    protected LayoutInflater inflater;

    public UserWatermeterPojAdapter(BaseActivity activity, List<WatermeterPoj> data) {
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
            convertView = inflater.inflate(R.layout.bh_userwatermeterpoj_list_item, parent, false);
        }

        final WatermeterPoj caseItem = dataList.get(position);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_index))
                .setText(String.valueOf(position + 1));

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_top_left)).setText(caseItem.caseno);

        TextView tvHandleNode = MmtViewHolder.get(convertView, R.id.tv_mid_one_left);
        // tvHandleNode.getPaint().setFakeBoldText(true);
        tvHandleNode.setText("受理编号:" + caseItem.acceptCode);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_two_left))
                .setText("工程地址: " + caseItem.pojAdd);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_three_left)).setText("工程名称: " + caseItem.pojName);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_four_left)).setText("用户数:" + caseItem.userCount);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_five_left)).setText("已安装:" + caseItem.hasLinkCount);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_time)).setText(caseItem.doingTime);
        TextView tvLoc = MmtViewHolder.get(convertView, R.id.tv_loc);
        tvLoc.setTag(caseItem.xyStr);
        tvLoc.setOnClickListener(this);

        TextView tvDtatil = MmtViewHolder.get(convertView, R.id.detail);
        tvDtatil.setTag(caseItem.pojID);
        tvDtatil.setOnClickListener(this);

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
            return;
        }

        if (v.getId() == R.id.detail) {

            final Object tag = v.getTag();
            if (tag == null || TextUtils.isEmpty(tag.toString())) {
                Toast.makeText(context, "无效工程", Toast.LENGTH_SHORT).show();
            } else {
                String pojID = tag.toString();

                Intent intent = new Intent(context, UserWatermeterPojDetail.class);
                intent.putExtra("tableName", "户表报装申请");
                intent.putExtra("ID", pojID);
                intent.putExtra("viewMode", TabltViewMode.READ.getTableViewMode());
                context.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(context);
            }

        }
    }

    @Override
    public void onItemClick(int position) {
        super.onItemClick(position);

        WatermeterPoj watermeterPoj = dataList.get(position);

        Intent intent = new Intent(context, WatermeterUserListActivity.class);
        intent.putExtra("caseno", watermeterPoj.caseno);
        context.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(context);

    }
}