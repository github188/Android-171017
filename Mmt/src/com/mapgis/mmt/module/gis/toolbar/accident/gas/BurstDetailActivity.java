package com.mapgis.mmt.module.gis.toolbar.accident.gas;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.module.gis.toolbar.accident.gas.entity.AttrbuiteItem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class BurstDetailActivity extends BaseActivity {
    private BurstDetailFragmetn fragment;

    private JSONObject data;
    ArrayList<AttrbuiteItem> attrbuiteItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            data = new JSONObject(getIntent().getStringExtra("data"));
            Iterator it = data.keys();
            while (it.hasNext()) {
                String key = it.next().toString();
                Object valueobject = data.get(key);
                if (valueobject != null && !BaseClassUtil.isNullOrEmptyString(valueobject.toString())) {
                    attrbuiteItemList.add(new AttrbuiteItem(key, valueobject.toString()));
                }
            }

        } catch (Exception ex) {

        }
        fragment = new BurstDetailFragmetn();
        addFragment(fragment);
    }

    class BurstDetailFragmetn extends Fragment {

        private ListView listView;
        private DetailAdapter adapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            listView = new ListView(getActivity());
            return listView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            adapter = new DetailAdapter(attrbuiteItemList);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }


        class DetailAdapter extends BaseAdapter {

            private ArrayList<AttrbuiteItem> attrbuiteItemList;

            private LayoutInflater inflater;

            public DetailAdapter(ArrayList<AttrbuiteItem> attrbuiteItemList) {
                super();
                this.attrbuiteItemList = attrbuiteItemList;
                inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            }

            @Override
            public int getCount() {
                return attrbuiteItemList.size();
            }

            @Override
            public Object getItem(int position) {
                return attrbuiteItemList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder = null;
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.attrbuite_item, null);
                    viewHolder.attrNameTV = (TextView) convertView.findViewById(R.id.attrName);
                    viewHolder.attrValueTV = (TextView) convertView.findViewById(R.id.attrValue);
                    convertView.setTag(viewHolder);
                }
                viewHolder = (ViewHolder) convertView.getTag();
                viewHolder.attrNameTV.setText(attrbuiteItemList.get(position).filedName);
                viewHolder.attrValueTV.setText(attrbuiteItemList.get(position).filedVal);

                return convertView;
            }

            class ViewHolder {
                public TextView attrNameTV;
                public TextView attrValueTV;
            }

        }

    }
}
