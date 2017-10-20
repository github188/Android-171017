package com.patrolproduct.module.spatialquery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.constant.ActivityAlias;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.R;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.feedback.MyPlanFeedback;
import com.repair.zhoushan.module.eventreport.ReportEventTask;

import java.util.HashMap;

/**
 * 巡检计划反馈功能
 *
 * @author Zoro
 */
public class PipeDetailToolbarFeedbackFragment extends Fragment {
    private final HashMap<String, String> graphicMap;

    public PipeDetailToolbarFeedbackFragment(HashMap<String, String> graphicMap) {
        this.graphicMap = graphicMap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pipe_detail_toolbar_feedback, container, false);

        //计划反馈
        view.findViewById(R.id.ivMakeSureLyt).setOnClickListener(onClickListener);

        //事件上报
        view.findViewById(R.id.asset_flowreport).setOnClickListener(onClickListenerforEventReport);
        return view;
    }

    OnClickListener onClickListenerforEventReport = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent sIntent = getActivity().getIntent();

            if (MyApplication.getInstance().getConfigValue("EventReportVersion", 2) == 3) {
                Bundle bundle = new Bundle();

                PatrolDevice device = (PatrolDevice) sIntent.getSerializableExtra("device");

                bundle.putString("layerName", device.LayerName);
                bundle.putString("filed", "编号");
                bundle.putString("filedVal", device.PipeNo);

                bundle.putString("position", device.X + "," + device.Y);

                HashMap<String, String> graphicMap = (HashMap<String, String>) sIntent.getSerializableExtra("graphicMap");

                bundle.putString("addr", graphicMap.get("位置"));
                bundle.putBoolean("closePreActivity", false);

                bundle.putString("patrolNo", "222");

                new ReportEventTask(getActivity(), "", "临时事件", bundle).mmtExecute();
            } else {
                Class<?> patrolReportActivity = ActivityClassRegistry.getInstance().getActivityClass(ActivityAlias.PATROL_REPORT_ACTIVITY);

                if (patrolReportActivity == null) {
                    return;
                }

                HashMap<String, String> map = (HashMap<String, String>) sIntent.getSerializableExtra("graphicMap");

                Intent intent = new Intent(getActivity(), patrolReportActivity);

                intent.putExtra("selectCoordinate", sIntent.getStringExtra("xy"));
                intent.putExtra("address", GisUtil.getPlaceField(map));
                intent.putExtra("identityField", GisUtil.getIdentityField(map));

                startActivity(intent);

                MyApplication.getInstance().startActivityAnimation(getActivity());
            }
        }
    };
    OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            String planName = graphicMap.get("planName");

            String PlanTypeID = graphicMap.get("PlanTypeID");

            String state = graphicMap.get("$反馈状态$");

            if (state == null || state.equals("未到位")) {
                Toast.makeText(getActivity(), "该设备巡检点尚未到位，请到位后反馈", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), MyPlanFeedback.class);
                intent.putExtra("device", getActivity().getIntent().getSerializableExtra("device"));
                intent.putExtra("planName", planName);
                intent.putExtra("PlanTypeID", PlanTypeID);

                startActivity(intent);
                getActivity().finish();
            }
        }
    };
}
