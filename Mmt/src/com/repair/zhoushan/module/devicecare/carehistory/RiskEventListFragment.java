package com.repair.zhoushan.module.devicecare.carehistory;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.BaseDetailFragment;

import java.util.List;

/**
 * 隐患事件列表（“工商户安检”存在“隐患事件上报”）
 */
public class RiskEventListFragment extends BaseDetailFragment<HiddenDangerEventModel> {

    private String taskCode;

    public static RiskEventListFragment getInstance(String taskCode) {

        RiskEventListFragment fragment = new RiskEventListFragment();

        Bundle args = new Bundle();
        args.putString("TaskCode", taskCode);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey("TaskCode")) {
            this.taskCode = args.getString("TaskCode");
        }
    }

    @Override
    protected void fillContentView(ResultData<HiddenDangerEventModel> resultData) {

        ListView listView = new ListView(getActivity());
        listView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        listView.setSelector(R.drawable.item_focus_bg);
        listView.setDividerHeight(DimenTool.dip2px(getActivity(), 8));

        EventListAdapter listAdapter = new EventListAdapter(getActivity(), resultData.DataList);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HiddenDangerEventModel model = (HiddenDangerEventModel) parent.getAdapter().getItem(position);
                // TODO: 9/9/16
            }
        });

        addContentView(listView);
    }

    @Override
    protected String getRequestUrl() {

        return ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetHiddenEventListByTaskCode?taskCode="
                + taskCode;
    }

    private class EventListAdapter extends BaseAdapter {

        private final Context mContext;
        private final LayoutInflater inflater;
        private final List<HiddenDangerEventModel> dataList;

        EventListAdapter(Context context, List<HiddenDangerEventModel> dataList) {
            this.mContext = context;
            this.inflater = LayoutInflater.from(context);
            this.dataList = dataList;
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

            final HiddenDangerEventModel event = dataList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex))
                    .setText(mContext.getString(R.string.string_listitem_index, position + 1));

            TextView eventCode = MmtViewHolder.get(convertView, R.id.desc_top_left);
            eventCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            eventCode.setText(event.EventCode);

            TextView scheduleCareType = MmtViewHolder.get(convertView, R.id.desc_top_right);
            scheduleCareType.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            scheduleCareType.setText(event.EventType);

            TextView location = MmtViewHolder.get(convertView, R.id.desc_mid_left);
            location.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            location.setText(TextUtils.isEmpty(event.EventContent) ? "暂无描述" : event.EventContent);

            TextView finishTime = MmtViewHolder.get(convertView, R.id.desc_bottom_left);
            finishTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            finishTime.setText(event.Reporter);

            TextView scheduleCarePerson = MmtViewHolder.get(convertView, R.id.desc_bottom_right);
            scheduleCarePerson.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            scheduleCarePerson.setText(event.ReportTime);

            MmtViewHolder.get(convertView, R.id.desc_mid_bottom_left).setVisibility(View.GONE);

            return convertView;
        }
    }

}
