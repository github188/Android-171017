package com.mapgis.mmt.module.gis.toolbar.online.query.point;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;

public class OnlinePointQueryMapMenu extends BaseMapMenu {

	public OnlinePointQueryMapMenu(MapGISFrame mapGISFrame) {
		super(mapGISFrame);
	}

	@Override
	public boolean onOptionsItemSelected() {
		if (mapView == null || mapView.getMap() == null) {
			mapGISFrame.stopMenuFunction();
			return false;
		}

		mapView.setTapListener(new OnlinePointQueryListener(mapGISFrame, mapView));

		return true;
	}

	@Override
	public View initTitleView() {
		View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);
		((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("正在进行 点击查询");
		view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mapGISFrame.resetMenuFunction();
			}
		});
		return view;
	}
}
