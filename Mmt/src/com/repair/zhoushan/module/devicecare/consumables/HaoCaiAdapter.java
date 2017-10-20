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
import com.repair.zhoushan.module.devicecare.consumables.entity.Material;

import java.util.List;

public class HaoCaiAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;
    private List<Material> materialList;
    private boolean showOnly;

    public HaoCaiAdapter(Context context, List<Material> materialList, boolean showOnly) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.materialList = materialList;
        this.showOnly = showOnly;
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

        if (!showOnly) {

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.add_material_list_item, null);
            }

            final Material material = materialList.get(position);

            TextView name = MmtViewHolder.get(convertView, R.id.txt_code);
            // name.getPaint().setFakeBoldText(true);
            name.setText(material.Name
                    + (TextUtils.isEmpty(material.Unit) ? "" : (" (" + material.Unit + ")")));

            MmtViewHolder.get(convertView, R.id.select_checkbox).setVisibility(View.GONE);
            MmtViewHolder.get(convertView, R.id.txt_name).setVisibility(View.GONE);

            NumberPickerView numberPickerView = MmtViewHolder.get(convertView, R.id.quantityView);
            // numberPickerView.setMaxNumber(material.Stock < NumberPickerView.MAX_NUMBER ? (int) material.Stock : NumberPickerView.MAX_NUMBER);
            numberPickerView.setOnNumberChangedListener(new NumberPickerView.OnNumberChangedListener() {
                @Override
                public void onNumberChanged(int value) {
                    material.Num = value;
                }
            });
            numberPickerView.setValue(material.Num);

        } else {

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.base_list_item, null);
            }

            final Material material = materialList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex)).setText((position + 1) + ".");
            ((TextView) MmtViewHolder.get(convertView, R.id.desc_top_left)).setText(material.Name);

            TextView txtNum = MmtViewHolder.get(convertView, R.id.desc_top_right);
            txtNum.setTextColor(0xFF5677FC);
            txtNum.setText(material.Num + " "
                    + (TextUtils.isEmpty(material.Unit) ? "" : material.Unit));

            MmtViewHolder.get(convertView, R.id.desc_mid_left).setVisibility(View.GONE);
            MmtViewHolder.get(convertView, R.id.desc_mid_bottom_left).setVisibility(View.GONE);
            MmtViewHolder.get(convertView, R.id.desc_bottom_left).setVisibility(View.GONE);
            MmtViewHolder.get(convertView, R.id.desc_bottom_right).setVisibility(View.GONE);
        }

        return convertView;
    }
}