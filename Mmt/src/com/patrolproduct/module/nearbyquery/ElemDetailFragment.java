package com.patrolproduct.module.nearbyquery;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailFragment;

public class ElemDetailFragment extends PipeDetailFragment {

    // 重写管件详情里面定位操作
    private ImageView btnLocate;

    // 重写返回按钮的事件
    private ImageView btnBack;

    @Override
    protected void afterViewCreated(View view) {

        btnLocate = (ImageView) view.findViewById(com.mapgis.mmt.R.id.detail_loc_btn);
        btnBack = (ImageView) view.findViewById(com.mapgis.mmt.R.id.detail_revert_btn);

        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapGISFrame.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                getActivity().startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onBackPressed() {

        if (!getArguments().getBoolean("needLoc", true)) {
            getActivity().finish();
            return true;
        }
        return super.onBackPressed();
    }
}
