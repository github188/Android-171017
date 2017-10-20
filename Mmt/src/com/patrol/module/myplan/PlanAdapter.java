package com.patrol.module.myplan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.common.widget.customview.MyProgressbarView;
import com.patrol.entity.TaskInfo;

import java.text.MessageFormat;
import java.util.List;

/**
 * 自定义适配器
 */
public class PlanAdapter extends BaseAdapter {
    private final List<TaskInfo> taskList;
    private final LayoutInflater inflater;

    public PlanAdapter(Context context, List<TaskInfo> paraList) {
        this.taskList = paraList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.my_plan_task_item, parent, false);

        TaskInfo info = taskList.get(position);

        ((TextView) MmtViewHolder.get(convertView, R.id.tvSN)).setText(MessageFormat.format("{0}.", info.Index + 1));
        ((TextView) MmtViewHolder.get(convertView, R.id.tvName)).setText(info.Name);
        ((TextView) MmtViewHolder.get(convertView, R.id.tvArea)).setText(info.Code);
        ((TextView) MmtViewHolder.get(convertView, R.id.tvEndTime)).setText(info.CycleName);
        ((TextView) MmtViewHolder.get(convertView, R.id.tvCycle)).setText(info.EndTime.replace("/", "-"));

        if (info.TotalSum > 0) {
            MmtViewHolder.get(convertView, R.id.layoutPoint).setVisibility(View.VISIBLE);

            int arrive = info.ArriveSum, feedback = info.FeedbackSum, total = info.TotalSum;

            ((TextView) MmtViewHolder.get(convertView, R.id.tvArrive)).setText(MessageFormat.format("{0}/{1}", arrive, total));
            ((MyProgressbarView) MmtViewHolder.get(convertView, R.id.pbArrive)).setMaxProgress(total);
            ((MyProgressbarView) MmtViewHolder.get(convertView, R.id.pbArrive)).setProgress(arrive);
            ((TextView) MmtViewHolder.get(convertView, R.id.tvArriveRate)).setText(MessageFormat.format("{0}%", (int) (arrive * 100f / total)));

            int totalFBSum = info.TotalFBSum;
            if (totalFBSum > 0) {
                MmtViewHolder.get(convertView, R.id.layoutFb).setVisibility(View.VISIBLE);
                ((TextView) MmtViewHolder.get(convertView, R.id.tvFeedback)).setText(MessageFormat.format("{0}/{1}", feedback, totalFBSum));
                ((MyProgressbarView) MmtViewHolder.get(convertView, R.id.pbFeedback)).setMaxProgress(totalFBSum);
                ((MyProgressbarView) MmtViewHolder.get(convertView, R.id.pbFeedback)).setProgress(feedback);
                ((TextView) MmtViewHolder.get(convertView, R.id.tvFeedbackRate)).setText(MessageFormat.format("{0}%", (int) (feedback * 100f / totalFBSum)));
            } else {
                MmtViewHolder.get(convertView, R.id.layoutFb).setVisibility(View.GONE);
            }
        } else {
            MmtViewHolder.get(convertView, R.id.layoutPoint).setVisibility(View.GONE);
        }

        if (info.TotalLength > 0) {
            MmtViewHolder.get(convertView, R.id.layoutPipe).setVisibility(View.VISIBLE);

            String text = Convert.FormatLength(info.PipeLenth) + "/" + Convert.FormatLength(info.TotalLength);
            ((TextView) MmtViewHolder.get(convertView, R.id.tvArrivePipe)).setText(text);

            ((MyProgressbarView) MmtViewHolder.get(convertView, R.id.pbArrivePipe)).setMaxProgress((int) info.TotalLength);
            ((MyProgressbarView) MmtViewHolder.get(convertView, R.id.pbArrivePipe)).setProgress((int) info.PipeLenth);
            ((TextView) MmtViewHolder.get(convertView, R.id.tvArrivePipeRate)).setText(MessageFormat.format("{0}%", (int) (info.PipeLenth * 100f / info.TotalLength)));
        } else {
            MmtViewHolder.get(convertView, R.id.layoutPipe).setVisibility(View.GONE);
        }

        return convertView;
    }
}