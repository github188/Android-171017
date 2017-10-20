package com.patrol.module.patrolanalyze;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapgis.mmt.R;

import java.util.List;

public class PatrolAnalyzeAdapter extends BaseAdapter {
    private Context context;
    private List<AnalyzeResultBean.ResultBody> list;

    public PatrolAnalyzeAdapter(Context context, List list) {
        this.context = context;
        this.list = list;
    }

    public void setList(List<AnalyzeResultBean.ResultBody> list){
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.analyze_list_item_view, null);

            holder.userName = (TextView) convertView.findViewById(R.id.tv_userName);
            holder.arriveNum = (TextView) convertView.findViewById(R.id.tv_arriveNum);
            holder.coverLineLength = (TextView) convertView.findViewById(R.id.tv_coverLineLength);
            holder.feedbackNum = (TextView) convertView.findViewById(R.id.tv_feedbackNum);
            holder.onLineRoadDaySum = (TextView) convertView.findViewById(R.id.tv_onLineRoadDaySum);
            holder.onLineRoadSum = (TextView) convertView.findViewById(R.id.tv_onLineRoadSum);
            holder.onLineTimeDaySum = (TextView) convertView.findViewById(R.id.tv_onLineTimeDaySum);
            holder.onLineTimeSum = (TextView) convertView.findViewById(R.id.tv_onLineTimeSum);
            holder.reportIncidentNum = (TextView) convertView.findViewById(R.id.tv_reportIncidentNum);
            holder.stationName = (TextView) convertView.findViewById(R.id.tv_stationName);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AnalyzeResultBean.ResultBody body = list.get(position);

        holder.userName.setText((position + 1) + "." + body.userName);
        holder.onLineRoadDaySum.setText(String.valueOf(body.onLineRoadDaySum));
        holder.onLineRoadSum.setText(body.onLineRoadSum);
        holder.onLineTimeDaySum.setText(body.onLineTimeDaySum);
        holder.onLineTimeSum.setText(body.onLineTimeSum);
        holder.stationName.setText(body.stationName);
        holder.coverLineLength.setText(body.coverLineLength);
        holder.feedbackNum.setText(body.feedbackNum + "");
        holder.arriveNum.setText(body.arriveNum + "");
        holder.reportIncidentNum.setText(body.reportIncidentNum + "");

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder {
        public TextView userName;
        public TextView arriveNum;
        public TextView coverLineLength;
        public TextView feedbackNum;
        public TextView onLineRoadDaySum;
        public TextView onLineRoadSum;
        public TextView onLineTimeDaySum;
        public TextView onLineTimeSum;
        public TextView reportIncidentNum;
        public TextView stationName;
    }
}
