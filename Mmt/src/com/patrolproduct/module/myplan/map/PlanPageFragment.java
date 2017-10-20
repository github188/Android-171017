package com.patrolproduct.module.myplan.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.R;
import com.patrolproduct.module.myplan.MyPlanUtil;
import com.patrolproduct.module.myplan.entity.PatrolTask;
import com.patrolproduct.module.myplan.list.PlanDetailActivity;

/**
 * 显示在地图上滑动的Framgent
 */
public class PlanPageFragment extends Fragment {

    private final PatrolTask patrolTask;

    private TextView tvTaskState;

    public PlanPageFragment(PatrolTask patrolTask) {
        this.patrolTask = patrolTask;
    }

    public void setTaskState(int taskID) {
        if (tvTaskState != null && MyPlanUtil.getTaskStateString(taskID).trim().length() != 0) {
            tvTaskState.setText(MyPlanUtil.getTaskStateString(taskID));
        }
    }

    public void setTaskState() {
        setTaskState(Integer.valueOf(patrolTask.TaskID));
    }

    public boolean isNextArrowShow = true;
    public boolean isPreArrowShow = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout relativeLayout = new RelativeLayout(getActivity());
        relativeLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        TextView preTextView = new TextView(getActivity());
        preTextView.setId(preTextView.hashCode());
        LayoutParams preParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        preParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        preParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        preTextView.setLayoutParams(preParams);
        preTextView.setTextAppearance(getActivity(), R.style.default_text_tip);
        preTextView.setTextColor(getResources().getColor(R.color.progressbar_blue));
        preTextView.setText("←");

        preTextView.setVisibility(isPreArrowShow ? View.VISIBLE : View.INVISIBLE);

        TextView nextTextView = new TextView(getActivity());
        nextTextView.setId(nextTextView.hashCode());
        LayoutParams nextParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        nextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        nextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        nextTextView.setLayoutParams(nextParams);
        nextTextView.setTextAppearance(getActivity(), R.style.default_text_tip);
        nextTextView.setTextColor(getResources().getColor(R.color.progressbar_blue));
        nextTextView.setText("→");

        nextTextView.setVisibility(isNextArrowShow ? View.VISIBLE : View.INVISIBLE);

        TextView nameTextView = new TextView(getActivity());
        nameTextView.setId(nameTextView.hashCode());
        LayoutParams nameParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        nameParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        nameParams.addRule(RelativeLayout.RIGHT_OF, preTextView.getId());
        nameParams.addRule(RelativeLayout.LEFT_OF, nextTextView.getId());
        nameParams.setMargins(0, DimenTool.dip2px(getActivity(), 10), 0, 0);
        nameTextView.setLayoutParams(nameParams);
        nameTextView.setTextAppearance(getActivity(), R.style.default_text_medium_1);
        nameTextView.setSingleLine(true);
        nameTextView.setText(patrolTask.PlanInfo.PlanName);

        TextView typeTextView = new TextView(getActivity());
        typeTextView.setId(typeTextView.hashCode());
        LayoutParams typeParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        typeParams.addRule(RelativeLayout.BELOW, nameTextView.getId());
        typeParams.addRule(RelativeLayout.RIGHT_OF, preTextView.getId());
        typeTextView.setLayoutParams(typeParams);
        typeTextView.setTextAppearance(getActivity(), R.style.default_text_small_purity);
        typeTextView.setText(patrolTask.PlanInfo.PlanType);

        TextView timeTextView = new TextView(getActivity());
        timeTextView.setId(timeTextView.hashCode());
        LayoutParams timeParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        timeParams.addRule(RelativeLayout.BELOW, nameTextView.getId());
        timeParams.addRule(RelativeLayout.RIGHT_OF, typeTextView.getId());
        timeParams.leftMargin = DimenTool.dip2px(getActivity(), 10);
        timeTextView.setLayoutParams(timeParams);
        timeTextView.setTextAppearance(getActivity(), R.style.default_text_small_purity);
        timeTextView.setText(patrolTask.EndTime.split(" ")[0]);

        if (MyPlanUtil.getTaskStateString(Integer.valueOf(patrolTask.TaskID)).trim().length() != 0) {
            tvTaskState = new TextView(getActivity());
            tvTaskState.setId(tvTaskState.hashCode());
            LayoutParams tvTaskStateParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            tvTaskStateParams.addRule(RelativeLayout.RIGHT_OF, preTextView.getId());
            tvTaskStateParams.addRule(RelativeLayout.BELOW, typeTextView.getId());
            tvTaskState.setLayoutParams(tvTaskStateParams);
            tvTaskState.setTextAppearance(getActivity(), R.style.default_text_small_purity);
            tvTaskState.setText(MyPlanUtil.getTaskStateString(Integer.valueOf(patrolTask.TaskID)));
        }

        relativeLayout.addView(preTextView);
        relativeLayout.addView(nextTextView);

        relativeLayout.addView(nameTextView);
        relativeLayout.addView(typeTextView);
        relativeLayout.addView(timeTextView);
        relativeLayout.addView(tvTaskState);

        relativeLayout.setBackgroundResource(R.drawable.map_pager_bg);

        relativeLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PlanDetailActivity.class);
                intent.putExtra("planDetail", patrolTask);
                startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(getActivity());
            }
        });

        return relativeLayout;
    }
}
