package com.mapgis.mmt.module.gis;

import android.content.Intent;
import android.graphics.Color;

import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;

/**
 * 处理 外部应用  打开 巡检产品  
 * @author meikai
 */
public class OutIntentListener {
	
	private Intent outIntent ;
	private MapView mapView;
	
	public OutIntentListener(Intent outIntent , MapView mapView) {
		super();
		this.outIntent = outIntent;
		this.mapView = mapView ;
	}
	
	/**  显示 外部应用  传入 的 范围   */
	public void showExtent( ){
		String param = outIntent.getStringExtra("outPara");
		String dots[] = param.split(";");
		ArrayList<Dot> dotList = new ArrayList<Dot>();
		for( int i=0 ; i < dots.length ; i++ ){
			String dot = dots[i];
			dotList.add( new Dot( Double.parseDouble(dot.split(",")[0])  , Double.parseDouble(dot.split(",")[1])));
		}
		dotList.add( new Dot( Double.parseDouble(dots[0].split(",")[0])  , Double.parseDouble(dots[0].split(",")[1])));
		
		GraphicPolygon polygon = new GraphicPolygon( dotList.toArray( new Dot[]{} ) );
		polygon.setBorderlineWidth(2);
		polygon.setBorderlineColor(Color.RED);
		polygon.setColor(Color.argb(33, 0, 0, 160));
		
		mapView.getGraphicLayer().addGraphic(polygon);
		mapView.refresh();
		
	}

}
