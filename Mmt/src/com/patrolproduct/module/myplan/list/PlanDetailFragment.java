package com.patrolproduct.module.myplan.list;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.R;
import com.patrolproduct.constant.PlanDisplayNameMap;
import com.patrolproduct.module.myplan.entity.PatrolTask;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PlanDetailFragment extends ListFragment {

    private final ArrayList<String> arrayList = new ArrayList<>();

    private PatrolTask patrolTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey("planDetail")) {
            patrolTask = args.getParcelable("planDetail");
        }

        if (patrolTask != null) {

            LinkedHashMap<String, String> intentData = patrolTask.toLinkedHashMap();

            for (String key : intentData.keySet()) {

                String value = intentData.get(key);

                if (PlanDisplayNameMap.getDataMap().containsKey(key)) {
                    key = PlanDisplayNameMap.getDataMap().get(key);
                }

                if (BaseClassUtil.isNullOrEmptyString(value)) {
                    continue;
                }

                if (!String.valueOf(key).matches("[\\u4e00-\\u9fa5]+")) {// 不是中文不显示
                    continue;
                }

                arrayList.add(key + "`" + ((value.equalsIgnoreCase("null") || value.equalsIgnoreCase("")) ? "-" : value));
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setCacheColorHint(0);
        getListView().setBackgroundResource(R.color.white);
        setListAdapter(new PlanDetailAdapter(getActivity(), arrayList));
    }


    class PlanDetailAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final ArrayList<String> arrayList;

        PlanDetailAdapter(Context context, ArrayList<String> arrayList) {
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
            final PlanDetailAdapter.ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.asset_detail_props_item, parent, false);
                holder = new PlanDetailAdapter.ViewHolder();
                holder.assetKey = (TextView) convertView.findViewById(R.id.asset_key);

                holder.assetValue = (TextView) convertView.findViewById(R.id.asset_value);
                holder.assetValue.setSingleLine(false);
                holder.editTextView = (TextView) convertView.findViewById(R.id.asset_value_text);
                holder.position = position;
                convertView.setTag(holder);
            } else {
                holder = (PlanDetailAdapter.ViewHolder) convertView.getTag();
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
