package com.mapgis.mmt.module.gis.toolbar.accident.gas;

import android.app.Activity;
import android.content.Intent;

import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;

import org.json.JSONObject;

import java.util.HashMap;

public class BurstAnalysisAnnotationListener extends DefaultMapViewAnnotationListener {

    private Activity activity;
    private HashMap<String, JSONObject> detailHashmap;
    private Boolean hideTapListener = false;

    public Boolean getHideTapListener() {
        return hideTapListener;
    }

    public void setHideTapListener(Boolean hideTapListener) {
        this.hideTapListener = hideTapListener;
    }

    public BurstAnalysisAnnotationListener(Activity activity, HashMap<String, JSONObject> detailHashmap) {
        super();
        this.activity = activity;
        this.detailHashmap = detailHashmap;
    }

    @Override
    public void mapViewClickAnnotation(MapView mapview, Annotation annotation) {
        super.mapViewClickAnnotation(mapview, annotation);
        annotation.showAnnotationView();
        this.hideTapListener = true;
    }

    @Override
    public boolean mapViewWillShowAnnotationView(MapView mapview, AnnotationView annotationview) {
        return super.mapViewWillShowAnnotationView(mapview, annotationview);
    }

    @Override
    public boolean mapViewWillHideAnnotationView(MapView mapview, AnnotationView annotationview) {
        return super.mapViewWillHideAnnotationView(mapview, annotationview);
    }

    @Override
    public AnnotationView mapViewViewForAnnotation(MapView mapview, Annotation annotation) {
        return super.mapViewViewForAnnotation(mapview, annotation);
    }

    @Override
    public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
        super.mapViewClickAnnotationView(mapview, annotationview);
        try {
            Intent i = new Intent(activity, BurstDetailActivity.class);
            JSONObject jsdata = detailHashmap.get(annotationview.getAnnotation().getDescription());
            Object attrs = jsdata.get("attributes");
            i.putExtra("data", attrs.toString());
            activity.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
