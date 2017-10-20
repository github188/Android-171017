package com.patrolproduct.module.spatialquery;

import android.support.v4.app.Fragment;
import android.view.View;

import com.mapgis.mmt.module.gis.spatialquery.PipeDetailFragment;
import com.mapgis.mmt.R;

import java.util.HashMap;

public class PipeDetailForPatrolFragment extends PipeDetailFragment {

    @SuppressWarnings("unchecked")
    @Override
    protected void afterViewCreated(View view) {

        if (getArguments().getBoolean("fromPlan", false)) {
            Fragment fragment = new PipeDetailToolbarFeedbackFragment((HashMap<String, String>) getArguments().getSerializable(
                    "graphicMap"));

            getChildFragmentManager().beginTransaction().replace(R.id.frag_pipe_detail_toolbar, fragment).commit();
        }
    }
}
