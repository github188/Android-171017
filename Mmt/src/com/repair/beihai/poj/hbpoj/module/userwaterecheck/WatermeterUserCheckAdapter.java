package com.repair.beihai.poj.hbpoj.module.userwaterecheck;

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
import com.repair.beihai.poj.hbpoj.entity.UserInfoModel;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import java.util.List;

/**
 * Created by liuyunfan on 2016/7/6.
 */
public class WatermeterUserCheckAdapter extends SimpleBaseAdapter implements View.OnClickListener {

    protected List<UserInfoModel> dataList;
    protected BaseActivity context;
    protected LayoutInflater inflater;

    public WatermeterUserCheckAdapter(BaseActivity activity, List<UserInfoModel> data) {
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
            convertView = inflater.inflate(R.layout.bh_watermeteruser_list_item, parent, false);
        }

        final UserInfoModel caseItem = dataList.get(position);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_index))
                .setText(String.valueOf(position + 1));

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_top_left)).setText(caseItem.FileCode);

        TextView tvHandleNode = MmtViewHolder.get(convertView, R.id.tv_mid_one_left);
        // tvHandleNode.getPaint().setFakeBoldText(true);
        tvHandleNode.setText("用户名称:" + caseItem.UserName);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_one_right))
                .setText(TextUtils.isEmpty(caseItem.checkState) ? "未审核" : caseItem.checkState);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_two_left))
                .setText("用户地址: " + caseItem.Addr);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_three_left)).setText("水表口径: " + caseItem.MeterDN);
        ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_four_left)).setText("表身号:" + caseItem.watermeterNo);

        ((TextView) MmtViewHolder.get(convertView, R.id.tv_waterType)).setText(caseItem.waterType);

        TextView tvLoc = MmtViewHolder.get(convertView, R.id.tv_loc);
        tvLoc.setTag(caseItem.x + "," + caseItem.y);

        tvLoc.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View v) {

        // 定位按钮
        if (v.getId() == R.id.tv_loc) {

            final Object tag = v.getTag();

            if (tag == null) {
                Toast.makeText(context, "无坐标信息", Toast.LENGTH_SHORT).show();

                return;
            }

            if (TextUtils.isEmpty(tag.toString())) {
                Toast.makeText(context, "无坐标信息", Toast.LENGTH_SHORT).show();

                return;
            }

            if (tag.toString().split(",").length == 0) {
                Toast.makeText(context, "无坐标信息", Toast.LENGTH_SHORT).show();

                return;
            }

            if (TextUtils.isEmpty(tag.toString().split(",")[0])) {
                Toast.makeText(context, "无坐标信息", Toast.LENGTH_SHORT).show();
                return;
            }

            BaseMapCallback callback = new ShowMapPointCallback(context, tag.toString(), "", "", -1);
            MyApplication.getInstance().sendToBaseMapHandle(callback);
        }

    }

    @Override
    public void onItemClick(int position) {
        super.onItemClick(position);

        UserInfoModel watermeterModel = dataList.get(position);
        Intent intent = new Intent(context, UserWatermeterCheckEditActivity.class);
        intent.putExtra("tableName", "户表报装用户水表表");
        intent.putExtra("ID", watermeterModel.ID);
        intent.putExtra("viewMode", TabltViewMode.EDIT.getTableViewMode());
        context.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(context);
    }
}