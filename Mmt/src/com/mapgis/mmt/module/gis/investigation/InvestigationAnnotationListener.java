package com.mapgis.mmt.module.gis.investigation;

import android.app.Activity;
import android.content.Intent;

import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.List;

public class InvestigationAnnotationListener extends DefaultMapViewAnnotationListener {

	private Dot clickDot;
	private List<Dot> clickDotList;
	private DeviceType deviceType;

	public enum DeviceType {
		PointDevice, LineDevice
	}

    public void setClickDot(Dot clickDot) {
		this.clickDot = clickDot;
	}

	public void setClickDotList(List<Dot> clickDotList) {
		this.clickDotList = clickDotList;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	@Override
	public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
		Intent i = null;
		switch (this.deviceType) {
		case PointDevice:
			i = new Intent(mapview.getContext(), DeviceReportActivity.class);
			i.putExtra("DeviceType", 1);
			i.putExtra("dot", clickDot.toString());
			((Activity) mapview.getContext()).startActivityForResult(i, 100);
			break;
		case LineDevice:
			String dotListStr = "";
			for (Dot one : clickDotList) {
				dotListStr += one.toString() + ";";
			}
			dotListStr.substring(0, dotListStr.length() - 1);

			i = new Intent(mapview.getContext(), DeviceReportActivity.class);
			i.putExtra("DeviceType", 2);
			i.putExtra("dotList", dotListStr);
			((Activity) mapview.getContext()).startActivityForResult(i, 100);
			break;
		default:
		}
	}

}
