package com.mapgis.mmt.module.gis.toolbar.accident2;

import android.view.View;
import android.widget.ImageView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailFragment;

public class DetailListFragment extends PipeDetailFragment {

    @Override
    protected void afterViewCreated(View view) {
        ImageView btnBack = (ImageView) view.findViewById(R.id.detail_revert_btn);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseActivity baseActivity = (BaseActivity) getActivity();
                baseActivity.onBackPressed();
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        this.getActivity().finish();
        return true;
    }
}
