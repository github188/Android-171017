package com.mapgis.mmt.module.gis.toolbar.query.point;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;


/**
 * Created by cmios on 2017/3/10.
 */

public class PanoramaAnnotationListener extends DefaultMapViewAnnotationListener {
    @Override
    public void mapViewClickAnnotation(MapView mapview, Annotation annotation) {
        super.mapViewClickAnnotation(mapview, annotation);
    }

    @Override
    public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
        super.mapViewClickAnnotationView(mapview, annotationview);
        Activity activity = (Activity) mapview.getContext();
        Annotation annotation = annotationview.getAnnotation();
        MmtAnnotation mmtAnnotation = (MmtAnnotation)annotation;
        double x = mmtAnnotation.getPoint().x ;
        double y = mmtAnnotation.getPoint().y;



        Location destLoc = GpsReceiver.getInstance()
                .getLastLocationConverse(new GpsXYZ(x,y));
        if (destLoc !=null){
            Intent intent = new Intent(activity,PanoramaActivity.class);
            intent.putExtra("latitude",destLoc.getLatitude());
            intent.putExtra("longitude",destLoc.getLongitude());
            activity.startActivity(intent);
        }else {
            //114.419071,30.471953
            Intent intent = new Intent(activity,PanoramaActivity.class);
            intent.putExtra("latitude",30.471953);
            intent.putExtra("longitude",114.419071);
            activity.startActivity(intent);
            MyApplication.getInstance().showMessageWithHandle("目的地坐标转换失败，无法展示全景地图");
        }




    }
}
