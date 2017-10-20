package com.mapgis.mmt.module.gis.toolbar.measure;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView.MapViewTapListener;
import com.zondy.mapgis.geometry.Dot;

/**
 * 测量距离
 * 
 * @author Administrator
 * 
 */
public class MeasureLengthMapMenu extends BaseMapMenu implements MapViewTapListener {

	public MeasureLengthMapMenu(MapGISFrame mapGISFrame) {
		super(mapGISFrame);
	}

	Dot preDot = null;
	double length = 0;

	@Override
	public boolean onOptionsItemSelected() {
		if (mapView == null || mapView.getMap() == null) {
			mapGISFrame.stopMenuFunction();
			return false;
		}

		this.preDot = null;
		this.length = 0;

		// mapGISFrame.setNeedBackButton(true);
		// CustomViewManager.setMapMeasureActionEnabled(mapGISFrame, mapView);

		mapView.getGraphicLayer().removeAllGraphics();
		mapView.getAnnotationLayer().removeAllAnnotations();

		mapView.setTapListener(this);
		mapView.setAnnotationListener(new DefaultMapViewAnnotationListener());
		mapView.refresh();

		return true;
	}

	@Override
	public View initTitleView() {
		View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);
		((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("测量距离");
		view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mapGISFrame.resetMenuFunction();
			}
		});
		return view;
	}

	@Override
	public void mapViewTap(PointF arg0) {
		Dot dot = mapView.viewPointToMapPoint(arg0);

		GraphicPoint point = new GraphicPoint(dot, 10);

		point.setColor(preDot == null ? Color.GREEN : Color.RED);

		mapView.getGraphicLayer().addGraphic(point);

		if (preDot != null) {
			GraphicPolylin polylin = new GraphicPolylin(new Dot[] { preDot, dot });

			polylin.setLineWidth(5);
			polylin.setColor(Color.BLACK);

			mapView.getGraphicLayer().addGraphic(polylin);

			this.length += polylin.getLength();
			String len = "";

			if (this.length < 1) {
				len = String.format("%.1f", this.length) + "米";
			} else if (this.length < 1000) {
				len = String.format("%.0f", this.length) + "米";
			} else {
				len = String.format("%.1f", this.length / 1000) + "公里";
			}

			Annotation annotation = new Annotation("总长", len, dot, null);

			mapView.getAnnotationLayer().removeAllAnnotations();
			mapView.getAnnotationLayer().addAnnotation(annotation);

			annotation.showAnnotationView();
		}

		preDot = dot;
		mapView.refresh();
	}
}
