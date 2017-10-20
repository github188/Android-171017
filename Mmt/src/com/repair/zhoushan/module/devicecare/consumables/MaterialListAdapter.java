package com.repair.zhoushan.module.devicecare.consumables;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoBean;

import java.util.List;

public class MaterialListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater mLayoutInflater;
    private List<WuLiaoBean> materialList;

    private boolean isExistCostCenter;
    public void setExistCostCenter(boolean existCostCenter) {
        isExistCostCenter = existCostCenter;
    }

    public MaterialListAdapter(Context context, List<WuLiaoBean> materialList, boolean isExistCostCenter) {
        this.context = context;
        this.materialList = materialList;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.isExistCostCenter = isExistCostCenter;
    }

    @Override
    public int getCount() {
        return materialList.size();
    }

    @Override
    public Object getItem(int position) {
        return materialList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.base_list_item, null);
        }

        WuLiaoBean material = materialList.get(position);

        ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex)).setText(context.getString(R.string.string_listitem_index, (position + 1)));
        ((TextView) MmtViewHolder.get(convertView, R.id.desc_top_left)).setText(material.getName());

        TextView txtNum = MmtViewHolder.get(convertView, R.id.desc_top_right);
        txtNum.setTextColor(0xFF5677FC);
        txtNum.setText(material.getNum() + " "
                + (TextUtils.isEmpty(material.getUnit()) ? "" : material.getUnit()));
        ((TextView) MmtViewHolder.get(convertView, R.id.desc_mid_left)).setText(material.GongChang.getName()
                + (isExistCostCenter ? (" - " + material.ChenBenCenter.getName()) : ""));
        MmtViewHolder.get(convertView, R.id.desc_mid_bottom_left).setVisibility(View.GONE);
        ((TextView) MmtViewHolder.get(convertView, R.id.desc_bottom_left)).setText(material.WuLiaoKu.getName()
                + " - " + material.WuLiaoZu.getName());

        return convertView;
    }

}