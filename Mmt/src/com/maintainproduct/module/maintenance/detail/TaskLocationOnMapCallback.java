package com.maintainproduct.module.maintenance.detail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.constant.MapMenuRegistry;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.R;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

public class TaskLocationOnMapCallback extends BaseMapCallback {
	private Dot dot;
	private String text;
	private String arg;

	public TaskLocationOnMapCallback(Dot dot, String text, String arg) {
		this.dot = dot;
		this.text = BaseClassUtil.isNullOrEmptyString(text) ? "工单定位" : text;
		this.arg = arg;

	}

	public TaskLocationOnMapCallback(String locationStr, String text, String arg) {
		try {
			this.dot = new Dot(Double.valueOf(locationStr.split(",")[0]), Double.valueOf(locationStr.split(",")[1]));
			this.text = BaseClassUtil.isNullOrEmptyString(text) ? "工单定位" : text;
			this.arg = arg;
		} catch (Exception e) {
			Toast.makeText(mapGISFrame, "坐标值不符合规范", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (mapView == null) {
			return false;
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

		Toast.makeText(mapGISFrame, "点击标签返回", Toast.LENGTH_LONG).show();

		Bitmap bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_lcoding);

		Annotation annotation = new Annotation(text, arg, dot, bitmap);

		mapView.getAnnotationLayer().addAnnotation(annotation);

        annotation.showAnnotationView();

		mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
			@Override
			public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
				mapGISFrame.clearMapview();
				AppManager.resetActivityStack(mapGISFrame);
			}
		});

		mapView.zoomToCenter(dot, mapView.getZoom(), true);
		mapView.refresh();

		return false;
	}
}
