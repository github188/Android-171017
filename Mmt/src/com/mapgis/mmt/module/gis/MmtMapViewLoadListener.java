package com.mapgis.mmt.module.gis;

import android.util.Log;
import android.widget.Toast;

import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.android.mapview.MapView.MapViewMapLoadListener;

public class MmtMapViewLoadListener implements MapViewMapLoadListener {

	MapGISFrame mapGISFrame;

	public MmtMapViewLoadListener(MapGISFrame mapGISFrame) {
		this.mapGISFrame = mapGISFrame;
	}

	@Override
	public void mapViewWillStartLoadingMap(MapView arg0) {
		Log.i(getClass().getSimpleName(), "mapViewWillStartLoadingMap");

		mapGISFrame.setProgressBarIndeterminateVisibility(true);
	}

	@Override
	public void mapViewDidFinishLoadingMap(MapView arg0) {
		Log.i(getClass().getSimpleName(), "mapViewDidFinishLoadingMap");

		mapGISFrame.setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void mapViewDidFailLoadingMap(MapView arg0) {
		Log.i(getClass().getSimpleName(), "mapViewDidFailLoadingMap");

		mapGISFrame.setProgressBarIndeterminateVisibility(false);

		Toast.makeText(mapGISFrame, "抱歉，地图加载失败，请联系技术人员解决问题", Toast.LENGTH_LONG).show();
	}
}
