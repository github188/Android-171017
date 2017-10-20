package com.mapgis.mmt.module.gis.toolbar.pointoper;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;

/**
 * Created by liuyunfan on 2016/6/23.
 */
public class MmtAnnotationListenerDialog extends DefaultMapViewAnnotationListener {

    @Override
    public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
        try {
            Annotation annotation = annotationview.getAnnotation();

            if (!(annotation instanceof MmtAnnotation)) {
                return;
            }
            BaseActivity activity = (BaseActivity) mapview.getContext();
            MmtAnnotation mmtAnnotation = (MmtAnnotation) annotation;

            GISDetailDialogFragment fragment = new GISDetailDialogFragment();
            Bundle args = new Bundle();
            args.putSerializable("attr", mmtAnnotation.attrMap);
            fragment.setArguments(args);
            fragment.show(activity.getSupportFragmentManager(), "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
