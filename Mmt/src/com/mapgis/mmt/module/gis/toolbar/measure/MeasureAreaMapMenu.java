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
import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView.MapViewTapListener;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;

/**
 * 测量面积
 * 
 * @author Administrator
 * 
 */
public class MeasureAreaMapMenu extends BaseMapMenu implements MapViewTapListener {
	public MeasureAreaMapMenu(MapGISFrame mapGISFrame) {
		super(mapGISFrame);
	}

	Dots dots;

	@Override
	public boolean onOptionsItemSelected() {
		if (mapView == null || mapView.getMap() == null) {
			mapGISFrame.stopMenuFunction();
			return false;
		}

		this.dots = new Dots();

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
		((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("测量面积");
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

		point.setColor(Color.RED);

		mapView.getGraphicLayer().addGraphic(point);

		dots.append(dot);

		mapView.getGraphicLayer().removeGraphicByAttribute("area", "NoPoints");

		if (dots.size() > 2) {
			Dots fullDots = new Dots();

			fullDots.append(dots);
			fullDots.append(dots.get(0));

			GraphicPolygon polygon = new GraphicPolygon(fullDots);

			polygon.setColor(Color.argb(100, 0, 255, 0));
			polygon.setAttributeValue("area", "NoPoints");

			mapView.getGraphicLayer().addGraphic(polygon);

			// 兼容“逆时针在地图上点击范围时，测量结果是负值”问题
			double area = Math.abs(polygon.getArea());

			String areaString = "";

			// 1平方公里=1000000平方米
			if (area < 1000000) {
				areaString = String.format("%.1f", area) + "平方米";
			} else {
				areaString = String.format("%.1f", area / 1000000) + "平方公里";
			}

			Annotation annotation = new Annotation("总面积", areaString, dot, null);

			mapView.getAnnotationLayer().removeAllAnnotations();
			mapView.getAnnotationLayer().addAnnotation(annotation);

			annotation.showAnnotationView();
		} else if (dots.size() > 1) {
			GraphicPolylin polylin = new GraphicPolylin(dots);

			polylin.setLineWidth(5);
			polylin.setColor(Color.BLACK);
			polylin.setAttributeValue("area", "NoPoints");

			mapView.getGraphicLayer().addGraphic(polylin);
		}

		mapView.refresh();
	}
}
