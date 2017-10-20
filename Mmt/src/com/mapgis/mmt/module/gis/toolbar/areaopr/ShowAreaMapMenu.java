package com.mapgis.mmt.module.gis.toolbar.areaopr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;

/**
 * Created by liuyunfan on 2016/3/21.
 */
public class ShowAreaMapMenu extends BaseMapMenu {
    Context context;
    String value;

    public ShowAreaMapMenu(MapGISFrame mapGISFrame, Context context, String value) {
        super(mapGISFrame);
        this.context = context;
        this.value = value;
        AreaOprUtil.clearArea(mapView);
        if (!TextUtils.isEmpty(value)) {
            tip = AreaOprUtil.painArea(mapView, value);
        }
    }

    View viewBar;
    TextView tipView;
    String tip;

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);
        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("区域展示");
        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }
        mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);

        viewBar = mapGISFrame.getLayoutInflater().inflate(R.layout.map_select_point_bar, null);
        viewBar.findViewById(R.id.tvAddr2).setVisibility(View.GONE);
        tipView = (TextView) viewBar.findViewById(R.id.tvAddr1);

        tipView.setText(TextUtils.isEmpty(tip) ? AreaOprUtil.areatip : tip);

        viewBar.findViewById(R.id.tvOk).setVisibility(View.INVISIBLE);


        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(-1, -2);

        params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        viewBar.setLayoutParams(params1);
        mapView.addView(viewBar);
        return true;
    }

    @Override
    public boolean onBackPressed() {
        backActivity();

        return true;
    }

    private void backActivity() {
        try {
            Intent intent = ((Activity) context).getIntent();
            intent.setClass(mapGISFrame, context.getClass());
            intent.removeExtra("area");
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            mapGISFrame.startActivity(intent);
            MyApplication.getInstance().startActivityAnimation(mapGISFrame);
            mapView.removeView(viewBar);
            mapGISFrame.resetMenuFunction();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
