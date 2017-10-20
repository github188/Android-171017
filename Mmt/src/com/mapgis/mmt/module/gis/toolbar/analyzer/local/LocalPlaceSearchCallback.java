package com.mapgis.mmt.module.gis.toolbar.analyzer.local;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;

import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.analyzer.local.LocalPlaceSearchResult.LocalPlaceSearchResultItem;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.geometry.Dot;

public class LocalPlaceSearchCallback extends BaseMapCallback {

	LocalPlaceSearchResult result;
	LocalPlaceSearchResultItem oneResult;
	/**  单点定位  注解 */
	static Annotation oneAnnotation; 

	public LocalPlaceSearchCallback(LocalPlaceSearchResult result) {
		super();
		this.result = result;
	}

	public LocalPlaceSearchCallback(LocalPlaceSearchResultItem oneResult) {
		super();
		this.oneResult = oneResult;
	}

	@Override
	public boolean handleMessage(Message msg) {
		Bitmap bitmapOne = BitmapFactory.decodeResource( mapView.getResources(), R.drawable.icon_gcoding);
		Bitmap bitmapAll = BitmapFactory.decodeResource( mapView.getResources(), R.drawable.icon_lcoding);
		
		if( this.oneResult != null ){
			if( oneAnnotation != null )
				mapView.getAnnotationLayer().removeAnnotation(oneAnnotation);
			
			Dot dot = new Dot(this.oneResult.loc_x, this.oneResult.loc_y);
			oneAnnotation = new Annotation( this.oneResult.addressName, "", dot, null);
			oneAnnotation.setImage(bitmapOne);
			mapView.getAnnotationLayer().addAnnotation(oneAnnotation);
			oneAnnotation.showAnnotationView();
		}
		
		if( result != null && result.dataList.size() > 0 ){
			mapView.getGraphicLayer().removeAllGraphics();
			mapView.getAnnotationLayer().removeAllAnnotations();
			
			Dot dot0 = new Dot(result.dataList.get(0).loc_x, result.dataList.get(0).loc_y);
			Annotation annotation0 = new Annotation( result.dataList.get(0).addressName, "", dot0, null);
			annotation0.setImage(bitmapAll);
			mapView.getAnnotationLayer().addAnnotation(annotation0);
			annotation0.showAnnotationView();
			
			for (int i = 1; i < result.dataList.size(); i++) {
				Dot dot = new Dot(result.dataList.get(i).loc_x, result.dataList.get(i).loc_y);
				Annotation annotation = new Annotation( result.dataList.get(i).addressName, "", dot, null);
				annotation.setImage(bitmapAll);
				mapView.getAnnotationLayer().addAnnotation(annotation);
			}
		}

		mapView.refresh();

		return false;
	}

}
