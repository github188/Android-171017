package com.mapgis.mmt.module.gis.toolbar.accident;

import android.graphics.Color;
import android.graphics.PointF;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.android.mapview.MapView.MapViewTapListener;
import com.zondy.mapgis.geometry.Dot;

public class AccidentPointSearchListener implements MapViewTapListener {
	private final MapView mapView;
	private final PipeAccidentMenu menu;

	public AccidentPointSearchListener(PipeAccidentMenu menu, MapGISFrame mapGISFrame) {
		this.menu = menu;
		this.mapView = mapGISFrame.getMapView();
	}

	@Override
	public void mapViewTap(PointF arg0) {
		Dot tagMapDot = mapView.viewPointToMapPoint(arg0);

		// 将手指点的位置的标注图形显示在地图上
		GraphicPoint tagGraphicPoint = new GraphicPoint();
		tagGraphicPoint.setColor(Color.RED);
		tagGraphicPoint.setSize(10);
		tagGraphicPoint.setPoint(tagMapDot);
		mapView.getGraphicLayer().addGraphic(tagGraphicPoint);

		mapView.refresh();

		menu.tagMapDot = tagMapDot;

		// 根据点击的点，进行爆管分析
		menu.startQuery();
	}

}
