package com.patrolproduct.module.myplan.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapgis.mmt.common.widget.customview.MyProgressbarView;
import com.mapgis.mmt.R;
import com.patrolproduct.module.myplan.MyPlanUtil;
import com.patrolproduct.module.myplan.entity.PatrolTask;

import java.util.ArrayList;

/**
 * 自定义适配器
 */
class PlanAdapter extends BaseAdapter {
    private final ArrayList<PatrolTask> paraList;
    private final LayoutInflater inflater;

    public PlanAdapter(Context context, ArrayList<PatrolTask> paraList) {
        this.paraList = paraList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return paraList.size();
    }

    @Override
    public Object getItem(int position) {
        return paraList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.patrol_plan_item, null);

            viewHolder = new ViewHolder();
            viewHolder.planItemName = (TextView) convertView.findViewById(R.id.planItemName);
            viewHolder.planItemType = (TextView) convertView.findViewById(R.id.planItemType);
            viewHolder.planItemTime = (TextView) convertView.findViewById(R.id.planItemTime);

            viewHolder.planItemCycle = (TextView) convertView.findViewById(R.id.planItemCycle);

            viewHolder.planItemArrive = (TextView) convertView.findViewById(R.id.planItemArrive);
            viewHolder.planItemFeedback = (TextView) convertView.findViewById(R.id.planItemFeedback);

            viewHolder.arriveProgressbar = (MyProgressbarView) convertView.findViewById(R.id.arriveProgressbar);
            viewHolder.feedbackProgressbar = (MyProgressbarView) convertView.findViewById(R.id.feedbackProgressbar);

            viewHolder.planBarParentLayout = convertView.findViewById(R.id.planBarParentLayout);
            viewHolder.planBarFeedbackLayout = convertView.findViewById(R.id.planBarFeedbackLayout);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.planItemName.setText(paraList.get(position).PlanInfo.PlanName);
        viewHolder.planItemType.setText(paraList.get(position).PlanInfo.PlanType);
        viewHolder.planItemTime.setText(paraList.get(position).EndTime.split(" ")[0]);

        // 计划周期原先在Patrol_PLANTBL中，然而现在是分派再决定周期，导致原有的无效，暂时不显示
        // ((TextView)
        // view.findViewById(R.id.planItemCycle)).setText(paraList.get(position).PlanInfo.PlanCycle);
        viewHolder.planItemCycle.setVisibility(View.GONE);

        String counts = MyPlanUtil.getTaskStateCount(Integer.valueOf(paraList.get(position).TaskID));

        String[] str = counts.split(",");

        if (str.length > 1) {
            viewHolder.planItemArrive.setText(str[0] + "/" + str[str.length - 1]);
            viewHolder.planItemFeedback.setText("0/" + str[str.length - 1]);

            viewHolder.arriveProgressbar.setMaxProgress(Integer.valueOf(str[str.length - 1]));
            viewHolder.arriveProgressbar.setProgress(Integer.valueOf(str[0]));

            viewHolder.feedbackProgressbar.setMaxProgress(Integer.valueOf(str[str.length - 1]));

            if (str.length == 3) {
                viewHolder.planItemFeedback.setText(str[1] + "/" + str[str.length - 1]);
                viewHolder.feedbackProgressbar
                        .setProgress(Integer.valueOf(str[1]) > Integer.valueOf(str[str.length - 1]) ? Integer
                                .valueOf(str[str.length - 1]) : Integer.valueOf(str[1]));
            }

            /**
             // 区域巡检不显示到位数跟反馈数
             if (Integer.valueOf(paraList.get(position).PlanInfo.PType) == MyPlanUtil.PLAN_AREA_PID) {
             viewHolder.planBarParentLayout.setVisibility(View.GONE);
             }

             // 路径巡检和管线巡检不显示反馈数
             if (Integer.valueOf(paraList.get(position).PlanInfo.PType) == MyPlanUtil.PLAN_PATH_PID
             || Integer.valueOf(paraList.get(position).PlanInfo.PType) == MyPlanUtil.PLAN_PIPE_PID) {
             viewHolder.planBarFeedbackLayout.setVisibility(View.GONE);
             }
             **/
        } else {
            viewHolder.planBarParentLayout.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {
        public TextView planItemName;
        public TextView planItemType;
        public TextView planItemTime;

        public TextView planItemCycle;

        public TextView planItemArrive;
        public TextView planItemFeedback;

        public MyProgressbarView arriveProgressbar;
        public MyProgressbarView feedbackProgressbar;

        public View planBarParentLayout;
        public View planBarFeedbackLayout;
    }
}