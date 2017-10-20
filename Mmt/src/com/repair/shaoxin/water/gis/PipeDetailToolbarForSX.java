package com.repair.shaoxin.water.gis;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.eventreport.ReportEventTask;

import java.util.HashMap;

/**
 * 增加属性编辑功能
 */
public class PipeDetailToolbarForSX extends Fragment implements OnClickListener {
    Intent sIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sIntent = getActivity().getIntent();

        View view = inflater.inflate(R.layout.pipe_detail_toolbar_sx, container, false);

        try {
            view.findViewById(R.id.layoutAttEdit).setOnClickListener(this);

            view.findViewById(R.id.layoutProblemEvent).setOnClickListener(this);

            view.findViewById(R.id.layoutLeakEvent).setOnClickListener(this);

            view.findViewById(R.id.layoutPatrolEvent).setOnClickListener(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.layoutAttEdit) {
                Intent intent = new Intent(getActivity(), AttributeEditActivityForSX.class);

                intent.putExtras(getActivity().getIntent());

                startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(getActivity());
            } else if (v.getId() == R.id.layoutProblemEvent
                    || v.getId() == R.id.layoutLeakEvent || v.getId() == R.id.layoutPatrolEvent) {
                Bundle bundle = new Bundle();

                bundle.putString("layerName", sIntent.getStringExtra("layerName"));
                bundle.putString("filed", "编号");
                bundle.putString("filedVal", sIntent.getStringExtra("pipeNo"));

                bundle.putString("position", sIntent.getStringExtra("xy"));

                HashMap<String, String> graphicMap = (HashMap<String, String>) sIntent.getSerializableExtra("graphicMap");

                bundle.putString("addr", graphicMap.get("位置"));
                bundle.putBoolean("closePreActivity", false);

                if (v.getId() == R.id.layoutLeakEvent) {
                    bundle.putString("patrolNo", "111");

                    new ReportEventTask(getActivity(), "", "检漏事件", bundle).mmtExecute();
                } else if (v.getId() == R.id.layoutPatrolEvent) {
                    bundle.putString("patrolNo", "222");

                    new ReportEventTask(getActivity(), "", "临时事件", bundle).mmtExecute();
                } else {
                    bundle.putString("patrolNo", "333");

                    new ReportEventTask(getActivity(), "", "隐患事件", bundle).mmtExecute();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
