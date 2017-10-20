package com.repair.zhoushan.module.devicecare.consumables;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.common.widget.customview.NumberPickerView;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoBean;

import java.util.List;

public class UpdateMaterialAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;
    private List<WuLiaoBean> materialList;

    public UpdateMaterialAdapter(Context context, List<WuLiaoBean> materialList) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.materialList = materialList;
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
            convertView = mLayoutInflater.inflate(R.layout.material_list_item, null);
        }

        final WuLiaoBean material = materialList.get(position);

        MmtViewHolder.get(convertView, R.id.itemIndex).setVisibility(View.GONE);
        ((TextView) MmtViewHolder.get(convertView, R.id.desc_top_left)).setText(material.getName());
        ((TextView) MmtViewHolder.get(convertView, R.id.desc_top_right)).setText(material.getNum() + " "
                + (TextUtils.isEmpty(material.getUnit()) ? "" : material.getUnit()));
        ((TextView) MmtViewHolder.get(convertView, R.id.desc_mid_left)).setText(material.WuLiaoZu.getName());
        ((TextView) MmtViewHolder.get(convertView, R.id.desc_bottom_left)).setText(material.GongChang.getName()
                + " - " + material.WuLiaoKu.getName());

        MmtViewHolder.get(convertView, R.id.desc_mid_bottom_left).setVisibility(View.GONE);

        MmtViewHolder.get(convertView, R.id.divider_line).setVisibility(View.VISIBLE);
        MmtViewHolder.get(convertView, R.id.bottom_layout).setVisibility(View.VISIBLE);

        NumberPickerView numberPickerView = MmtViewHolder.get(convertView, R.id.quantity_view);
        numberPickerView.setOnNumberChangedListener(new NumberPickerView.OnNumberChangedListener() {
            @Override
            public void onNumberChanged(int value) {
                material.setTagValue(value);
            }
        });
        numberPickerView.setMaxNumber(material.getNum());
        numberPickerView.setValue(material.getTagValue());

        return convertView;
    }
}
