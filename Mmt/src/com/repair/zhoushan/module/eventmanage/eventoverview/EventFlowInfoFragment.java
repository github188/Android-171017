package com.repair.zhoushan.module.eventmanage.eventoverview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.EventFlowItem;
import com.repair.zhoushan.entity.EventItem;

import java.util.ArrayList;
import java.util.UUID;

public class EventFlowInfoFragment extends ListFragment {

    private final ArrayList<EventFlowItem> dataList = new ArrayList<>();

    private LayoutInflater inflater;

    private EventItem mEventItem;

    private boolean isInit = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && !isInit) {
            loadData();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mEventItem = getArguments().getParcelable("ListItemEntity");
        this.inflater = LayoutInflater.from(getActivity());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Intent intent = new Intent(getActivity(), CaseProcedureActivity.class);
        intent.putExtra("CaseNo", dataList.get(position).CaseNo);
        startActivity(intent);
    }

    private void loadData() {

        new MmtBaseTask<Void, Void, String>(getActivity(), false) {
            @Override
            protected String doInBackground(Void... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetEventFlowLog?eventCode="
                        + mEventItem.EventCode + "&_token=" + UUID.randomUUID().toString();

                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String jsonResult) {

                isInit = true;

                ResultData<EventFlowItem> newData
                        = Utils.json2ResultDataToast(EventFlowItem.class, getActivity(), jsonResult, "获取数据失败", true);

                String emptyMsg = "暂无该事件相关的流程信息";
                if (newData == null) {
                    emptyMsg = "获取流程信息失败!";
                } else {
                    dataList.addAll(newData.DataList);
                }

                setEmptyText(emptyMsg);
                setListAdapter(new FlowListAdapter());
            }
        }.mmtExecute();
    }

    private final class FlowListAdapter extends BaseAdapter {

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
                convertView = inflater.inflate(R.layout.base_list_item, parent, false);
            }

            final EventFlowItem flowItem = dataList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex))
                    .setText(getString(R.string.string_listitem_index, (position + 1)));

            TextView tvFlowName = MmtViewHolder.get(convertView, R.id.desc_top_left);
            tvFlowName.getPaint().setFakeBoldText(true);
            tvFlowName.setText(flowItem.FlowName);

            TextView tvIsOver = MmtViewHolder.get(convertView, R.id.desc_top_right);
            tvIsOver.setTextColor(0xFF5677FC);
            tvIsOver.setText("1".equals(flowItem.IsOver) ? "已结束" : "未结束");

            TextView tvUserName = MmtViewHolder.get(convertView, R.id.desc_mid_left);
            tvUserName.setTextColor(0xFF000000);
            ((TextView) MmtViewHolder.get(convertView, R.id.desc_mid_left)).setText(flowItem.CreateName + " / " + flowItem.CreateDepart);

            MmtViewHolder.get(convertView, R.id.desc_mid_bottom_left).setVisibility(View.GONE);

            TextView tvCaseNo = MmtViewHolder.get(convertView, R.id.desc_bottom_left);
            tvCaseNo.setTextColor(0xFF000000);
            tvCaseNo.setText(flowItem.CaseNo);

            ((TextView) MmtViewHolder.get(convertView, R.id.desc_bottom_right)).setText(flowItem.CreateTime);

            return convertView;
        }
    }
}
