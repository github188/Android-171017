package com.maintainproduct.v2.callback;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import com.maintainproduct.v2.caselist.GDItem;
import com.maintainproduct.v2.caselist.MaintainConstant;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.constant.MapMenuRegistry;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.R;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;

public class GDAllLocationOnMapCallback extends BaseMapCallback {
	
	private ArrayList<GDItem> dataList;
	private Class<?> annotationToThisActivity ;
	
	public GDAllLocationOnMapCallback(ArrayList<GDItem> dataList, Class<?> annotationToThisActivity) {
		super();
		this.dataList = dataList;
		this.annotationToThisActivity = annotationToThisActivity;
	}

	public boolean handleMessage2(Message msg) {
		
		return false;
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
		
		// 显示定位按钮
		mapGISFrame.getBaseRightImageView().setVisibility(View.VISIBLE);
		mapGISFrame.getBaseRightImageView().setImageResource(R.drawable.action_mode_detail);
		mapGISFrame.getBaseRightImageView().setOnClickListener( new OnClickListener() {
					@Override
					public void onClick(View v) {
						AppManager.resetActivityStack(mapGISFrame);
					}
				});

//		((TextView) mapGISFrame.findViewById(R.id.baseActionBarRightTextView)).setText("列表");
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
		
		//  所有点的 外包 矩形
		GraphicPolygon polygon = new GraphicPolygon();
		
		try{
			for( int i = 0 ; i < dataList.size() ; i++ ){
				String position = dataList.get(i).Position;
				if( position.length() ==0 )
					continue;
				Dot dot = new Dot( Double.parseDouble( position.split(",")[0] ),  Double.parseDouble( position.split(",")[1] ));
				polygon.appendPoint(dot);
				Annotation annotation = new Annotation(dataList.get(i).ReportType+"/"+dataList.get(i).ReportContent , dataList.get(i).CaseCode, dot, bitmap);
				mapView.getAnnotationLayer().addAnnotation(annotation);
			}

			mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
				@Override
				public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
//					AppManager.resetActivityStack(mapGISFrame);
					int position = 0 ;
					for( int i = 0 ; i < dataList.size() ; i++ ){
						if( dataList.get(i).CaseCode.equals( annotationview.getAnnotation().getDescription() ) ){
							position = i;
							break;
						}
					}
					Intent intent = new Intent(mapGISFrame, annotationToThisActivity);
					intent.putExtra("isMapToMe", true);
					intent.putExtra("OneLocTitle", dataList.get(position).ReportType+"/"+dataList.get(position).ReportContent);
					intent.putExtra("OneLocDescription", dataList.get(position).CaseCode);
					intent.putExtra("GDState", dataList.get(position).State);
					intent.putExtra("CaseID", dataList.get(position).CaseID);
					intent.putExtra("ListItemEntity", dataList.get(position).build() );
					mapGISFrame.startActivityForResult(intent, MaintainConstant.DEFAULT_REQUEST_CODE);
					MyApplication.getInstance().startActivityAnimation(mapGISFrame);
				}
			});
		}catch(Exception e){
		}

//		float zoom = mapView.getZoom() <= 6 ? 6 : mapView.getZoom();
		Rect rect = polygon.getBoundingRect();
		rect = new Rect(rect.xMin - 200, rect.yMin - 200, rect.xMax + 200, rect.yMax + 200);
		mapView.zoomToRange(rect, false);
		
		mapView.refresh();
		return false;
	}

}
