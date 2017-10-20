package com.patrol.module.KeyPoint;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.patrol.entity.KeyPoint;

import java.util.ArrayList;
import java.util.HashMap;

public class PointDetailFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return inflater.inflate(R.layout.my_plan_point_detail, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        try {
            final KeyPoint kp = getArguments().getParcelable("kp");

            if (kp == null)
                return;

            ArrayList<String> data = new ArrayList<>();

            try {
                data.add("图层`" + kp.GisLayer);

                HashMap<String, String> graphicMap = (HashMap<String, String>) getArguments().getSerializable("attr");

                if (graphicMap != null) {
                    String[] columnNames = getArguments().getStringArray("names");

                    if (columnNames == null)
                        columnNames = graphicMap.keySet().toArray(new String[graphicMap.size()]);

                    for (String key : columnNames) {
                        String value = graphicMap.get(key);

                        // 跳过类似这种自定义的字段 $图层名称$
                        if (key.startsWith("$") && key.endsWith("$")) {
                            continue;
                        }

                        // 判断key是否包含中文，如果没有中文不做显示
                        boolean isExistChinese = false;

                        for (char k : key.toCharArray()) {
                            isExistChinese = String.valueOf(k).matches("[\\u4e00-\\u9fa5]+");

                            if (isExistChinese) {
                                break;
                            }
                        }

                        if (!isExistChinese) {
                            continue;
                        }

                        if (TextUtils.isEmpty(value) || value.equalsIgnoreCase("null"))
                            value = "-";

                        data.add(key + "`" + value);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (data.size() == 1)
                data.add(kp.FieldName + "`" + kp.FieldValue);

            ((ListView) view.findViewById(R.id.lvDetail)).setAdapter(new EmsPipeDetailActivityAdapter(getActivity(), data));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class EmsPipeDetailActivityAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final ArrayList<String> arrayList;

        public EmsPipeDetailActivityAdapter(Context context, ArrayList<String> arrayList) {
            mInflater = LayoutInflater.from(context);
            this.arrayList = arrayList;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return position >= 0 && position < arrayList.size() ? arrayList.get(position) : ":";
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.asset_detail_props_item, parent, false);
                holder = new ViewHolder();
                holder.assetKey = (TextView) convertView.findViewById(R.id.asset_key);
                holder.assetValue = (TextView) convertView.findViewById(R.id.asset_value);
                holder.editTextView = (TextView) convertView.findViewById(R.id.asset_value_text);
                holder.position = position;
                convertView.setTag(holder);

                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String value = ((TextView) v.findViewById(R.id.asset_value)).getText().toString();

                        Toast.makeText(getActivity(), value, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String keyValue = arrayList.get(position);
            String[] keyValueArr = keyValue.indexOf('`') >= 0 ? keyValue.split("`") : null;
            if (keyValueArr != null) {
                if (keyValueArr.length >= 2) {
                    holder.assetKey.setText(keyValueArr[0] != null ? keyValueArr[0] : "");
                    holder.assetValue.setText(null != keyValueArr[1] ? keyValueArr[1] : "");
                } else {
                    holder.assetKey.setText(keyValueArr[0] != null ? keyValueArr[0] : "");
                    holder.assetValue.setText("");
                }
            }

            convertView.setTag(holder);
            return convertView;
        }

        class ViewHolder {
            public TextView assetKey;
            public TextView assetValue;
            public TextView editTextView;
            public int position;
        }
    }
}
