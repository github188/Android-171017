package com.mapgis.mmt.module.gis.toolbar.analyzer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Message;

import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.toolbar.analyzer.LocatorGeocodeResult.Candidate;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;

public class AddressSearchResultCallback extends BaseMapCallback {
	private final int SHOW_ALL = 0;
	private final int SHOW_SINGLE = 1;
	private final int SHOW_MINE = 2;
	private int mode = SHOW_ALL;

	private LocatorGeocodeResult.Candidate candidate;

	public AddressSearchResultCallback(LocatorGeocodeResult.Candidate candidate) {
		this.candidate = candidate;
		this.mode = SHOW_SINGLE;
	}

	private Dot dot;

	public AddressSearchResultCallback(Dot dot) {
		this.dot = dot;
		this.mode = SHOW_MINE;
	}

	private LocatorGeocodeResult geocodeResult;

	public AddressSearchResultCallback(LocatorGeocodeResult geocodeResult) {
		this.geocodeResult = geocodeResult;
		this.mode = SHOW_ALL;
	}

	@Override
	public boolean handleMessage(Message arg0) {
		mapView.getGraphicLayer().removeAllGraphics();
		mapView.getAnnotationLayer().removeAllAnnotations();

		switch (mode) {
		case SHOW_ALL:// 绘制全部
			for (LocatorGeocodeResult.Candidate c : geocodeResult.candidates) {
				showAddressOnMapByType(mapView, c, c.attributes[0].Value);
			}
			break;
		case SHOW_SINGLE:// 绘制单一坐标
			showAddressOnMapByType(mapView, candidate, candidate.attributes[0].Value);
			zoomToCandidate(candidate);
			break;
		case SHOW_MINE:// 绘制自定义坐标
			Bitmap bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.mark);
			showDotOnMap(mapView, dot, "自定义坐标点", null, bitmap);
			break;
		}
		return false;
	}

	private void showAddressOnMapByType(MapView mapView, LocatorGeocodeResult.Candidate candidate, String type) {
		Bitmap bitmap;
		if (type == null || type.trim().length() == 0) {
			bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.mark);
		} else if (type.equals("address")) {
			bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.address);
		} else if (type.equals("positionline")) {
			bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.positionline);
		} else {
			bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.road);
		}
		showAddressOnMap(mapView, candidate, bitmap);
	}

	// 绘制道路 地址 定位线
	private void showAddressOnMap(MapView mapView, LocatorGeocodeResult.Candidate candidate, Bitmap bitmap) {
		if (candidate.attributes[0].Value.equals("address")) {// 位置
			Dot dot = new Dot(candidate.location.x, candidate.location.y);

			showDotOnMap(mapView, dot, candidate.address, candidate.location.x + "," + candidate.location.y, bitmap);

		} else {
			Dots dots = new Dots();

			double locations[][] = candidate.geometries.paths[0];

			for (double[] point : locations) {
				dots.append(new Dot(point[0], point[1]));
			}

			showDotsOnMap(mapView, dots, candidate.address, null, bitmap);
		}

	}

	// 绘制点
	private void showDotOnMap(MapView mapView, Dot dot, String str1, String str2, Bitmap bitmap) {
		Annotation annotation = new Annotation(str1, str2, dot, null);

		annotation.setImage(bitmap);

		mapView.getAnnotationLayer().addAnnotation(annotation);

		mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
			@Override
			public void mapViewClickAnnotationView(MapView arg0, AnnotationView arg1) {
				arg1.getAnnotation().showAnnotationView();
				arg0.refresh();
				return;
			}
		});

		mapView.refresh();

	}

	// 绘制线
	private void showDotsOnMap(MapView mapView, Dots dots, String str1, String str2, Bitmap bitmap) {
		GraphicPolylin polylin = new GraphicPolylin(dots);

		polylin.setLineWidth(5);
		polylin.setColor(Color.BLACK);

		mapView.getGraphicLayer().addGraphic(polylin);

		Dot dot = dots.get(dots.size() / 2);

		showDotOnMap(mapView, dot, str1, str2, bitmap);
	}

	// 缩放到指定位置
	private void zoomToCandidate(Candidate candidate) {
		Dot dot = null;
		if (candidate.attributes[0].Value.equals("address")) {// 位置
			dot = new Dot(candidate.location.x, candidate.location.y);
		} else {
			Dots dots = new Dots();

			double locations[][] = candidate.geometries.paths[0];

			for (double[] point : locations) {
				dots.append(new Dot(point[0], point[1]));
			}

			dot = dots.get(dots.size() / 2);
		}

		mapView.zoomToCenter(dot, 0.5f, true);
	}
}
