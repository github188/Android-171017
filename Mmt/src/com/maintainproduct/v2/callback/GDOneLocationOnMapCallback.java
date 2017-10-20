package com.maintainproduct.v2.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.constant.MapMenuRegistry;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.R;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

public class GDOneLocationOnMapCallback extends BaseMapCallback {
	private Dot dot;
	private String text;
	private String arg;

	public GDOneLocationOnMapCallback(Dot dot, String text, String arg) {
		this.dot = dot;
		this.text = (text == null || text.length() == 0) ? "工单定位" : text;
		this.arg = arg;

	}

	public GDOneLocationOnMapCallback(String locationStr, String text, String arg) {
		try {
			this.dot = new Dot(Double.valueOf(locationStr.split(",")[0]), Double.valueOf(locationStr.split(",")[1]));
			this.text = (text == null || text.length() == 0) ? "工单定位" : text;
			this.arg = arg;
		} catch (Exception e) {
			Toast.makeText(mapGISFrame, "坐标值不符合规范", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (mapView == null) {
			return false;
		}else{
//			mapView.removeAllViews();
//			mapGISFrame.clearMapview();
			mapView.getAnnotationLayer().removeAllAnnotations();
		}

		mapGISFrame.getBaseLeftImageView().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mapView.getAnnotationLayer().removeAllAnnotations();
				AppManager.resetActivityStack(mapGISFrame);
				mapGISFrame.getBaseLeftImageView().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						BaseMapMenu oldMenu = mapGISFrame.getFragment().menu;
						mapGISFrame.getFragment().menu = MapMenuRegistry.getInstance().getMenuInstance("返回主页", mapGISFrame);
						mapGISFrame.getFragment().menu.onOptionsItemSelected();
						mapGISFrame.getFragment().menu = oldMenu;
					}
				});
			}
		});

		Bitmap bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_lcoding);

		Annotation annotation = new Annotation(text, arg, dot, bitmap);

		mapView.getAnnotationLayer().addAnnotation(annotation);

		mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
			@Override
			public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
				mapGISFrame.clearMapview();
				AppManager.resetActivityStack(mapGISFrame);
			}
		});

		mapView.zoomToCenter(dot, 6.0f, false);

		mapView.refresh();

		return false;
	}
}
