package com.repair.zhoushan.module.devicecare.consumables;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.common.widget.customview.NumberPickerView;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoBean;

import java.util.List;

public class AddMaterialListAdpater extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    public List<WuLiaoBean> mList;

    public AddMaterialListAdpater(Context context, List<WuLiaoBean> list) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.add_material_list_item, parent, false);
        }

        final WuLiaoBean entity = mList.get(position);

        ((TextView) MmtViewHolder.get(convertView, R.id.txt_code)).setText(entity.getCode());

        String unit = TextUtils.isEmpty(entity.getUnit()) ? "" : String.format(" (%s)", entity.getUnit());
        ((TextView) MmtViewHolder.get(convertView, R.id.txt_name)).setText(entity.getName() + unit);

        NumberPickerView numberPickerView = MmtViewHolder.get(convertView, R.id.quantityView);
        numberPickerView.setOnNumberChangedListener(new NumberPickerView.OnNumberChangedListener() {
            @Override
            public void onNumberChanged(int value) {
                entity.setNum(value);
            }
        });
        numberPickerView.setValue(entity.getNum());

        final CheckBox checkBox = MmtViewHolder.get(convertView, R.id.select_checkbox);
        checkBox.setTag(position);
        checkBox.setOnClickListener(checkBoxClickListener);
        checkBox.setChecked(entity.isCheck());

        MmtViewHolder.get(convertView, R.id.contentInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.performClick();
            }
        });

        return convertView;
    }

    private View.OnClickListener checkBoxClickListener = new View.OnClickListener() {

        Object obj;
        int position;

        @Override
        public void onClick(View v) {

            obj = v.getTag();

            if (obj != null && obj instanceof Integer) {
                position = (Integer) obj;
                WuLiaoBean entity = mList.get(position);
                entity.setIsCheck(((CheckBox) v).isChecked());

                udpateView(position);
            }
        }
    };

    private void udpateView(int position) {

    }
}
