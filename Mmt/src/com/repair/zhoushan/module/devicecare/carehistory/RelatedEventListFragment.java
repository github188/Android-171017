package com.repair.zhoushan.module.devicecare.carehistory;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.R;
import com.repair.zhoushan.entity.EventItem;

import java.util.ArrayList;

public class RelatedEventListFragment extends Fragment {

    private ArrayList<EventItem> dataList;

    public static Fragment getInstance(ArrayList<EventItem> relatedEvents) {

        RelatedEventListFragment fragment = new RelatedEventListFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("ItemList", relatedEvents);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataList = getArguments().getParcelableArrayList("ItemList");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ListView listView = new ListView(getActivity());
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        listView.setDividerHeight(DimenTool.dip2px(getActivity(), 8));

        listView.setAdapter(new EventListAdapter(getActivity()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventItem item = dataList.get(position);
                Intent intent = new Intent(getActivity(), RelatedEventDetailActivity.class);
                intent.putExtra("ListItemEntity", item);
                startActivity(intent);
            }
        });

        return listView;
    }

    private class EventListAdapter extends BaseAdapter {

        private final Context mContext;
        private final LayoutInflater inflater;

        EventListAdapter(Context context) {
            mContext = context;
            inflater = LayoutInflater.from(context);
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
                convertView = inflater.inflate(R.layout.base_list_item, null);
            }

            final EventItem eventItem = dataList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex))
                    .setText(mContext.getString(R.string.string_listitem_index, position + 1));

            TextView eventCode = MmtViewHolder.get(convertView, R.id.desc_top_left);
            eventCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            eventCode.setText(eventItem.EventCode);

            TextView scheduleCareType = MmtViewHolder.get(convertView, R.id.desc_top_right);
            scheduleCareType.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            scheduleCareType.setText(eventItem.EventState);

            TextView location = MmtViewHolder.get(convertView, R.id.desc_mid_left);
            location.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            location.setText(TextUtils.isEmpty(eventItem.Summary) ? "暂无描述" : eventItem.Summary);

            TextView finishTime = MmtViewHolder.get(convertView, R.id.desc_bottom_left);
            finishTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            finishTime.setText(eventItem.EventName);

            TextView scheduleCarePerson = MmtViewHolder.get(convertView, R.id.desc_bottom_right);
            scheduleCarePerson.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            scheduleCarePerson.setText(eventItem.ReportTime);

            MmtViewHolder.get(convertView, R.id.desc_mid_bottom_left).setVisibility(View.GONE);

            return convertView;
        }
    }
}
