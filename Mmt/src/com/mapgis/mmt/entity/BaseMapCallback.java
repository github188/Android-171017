package com.mapgis.mmt.entity;

import android.os.Handler.Callback;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.android.mapview.MapView;

public abstract class BaseMapCallback implements Callback {
	protected MapGISFrame mapGISFrame;
	protected MapView mapView;

	public void setMapGISFrame(MapGISFrame mapGISFrame) {
		this.mapGISFrame = mapGISFrame;
		this.mapView = mapGISFrame.getMapView();
	}
}
