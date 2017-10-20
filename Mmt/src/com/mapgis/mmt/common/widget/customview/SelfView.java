package com.mapgis.mmt.common.widget.customview;

import android.graphics.PointF;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

public class SelfView {

	private MapView mapView;
	public View view;
	public Dot dot;
	public int viewWidth;
	public int veiwHeight;
	
	public SelfView( MapView mapView, View view, int width , int height , Dot dot ) {
		super();
		this.mapView = mapView;
		this.view = view;
		this.dot = dot;
		this.viewWidth = width;
		this.veiwHeight = height;
	}
	
	public void refreshView( ){
		PointF screenPoint = mapView.mapPointToViewPoint(dot);
		screenPoint.x -= viewWidth/2;
		screenPoint.y -= veiwHeight/2;
		
		RelativeLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins( (int)screenPoint.x, (int)screenPoint.y, 0 ,0);
		
		this.view.setLayoutParams(lp);
	}

	public void setDot(Dot dot) {
		this.dot = dot;
	}

	public Dot getDot() {
		return dot;
	}
	
	
}
