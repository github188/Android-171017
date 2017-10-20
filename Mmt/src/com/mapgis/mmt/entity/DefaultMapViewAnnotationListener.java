package com.mapgis.mmt.entity;

import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.android.mapview.MapView.MapViewAnnotationListener;

public class DefaultMapViewAnnotationListener implements MapViewAnnotationListener {

	@Override
	public void mapViewClickAnnotation(MapView mapview, Annotation annotation) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean mapViewWillShowAnnotationView(MapView mapview, AnnotationView annotationview) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mapViewWillHideAnnotationView(MapView mapview, AnnotationView annotationview) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AnnotationView mapViewViewForAnnotation(MapView mapview, Annotation annotation) {
		return new AnnotationView(annotation, mapview.getContext());
	}

	@Override
	public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
		// TODO Auto-generated method stub

	}

}
