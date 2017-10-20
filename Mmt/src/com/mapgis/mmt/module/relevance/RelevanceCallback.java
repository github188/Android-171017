package com.mapgis.mmt.module.relevance;

import android.content.Intent;
import android.os.Message;
import android.widget.Toast;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.online.query.point.OnlinePointQueryListener;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.mapgis.mmt.module.gis.toolbar.query.point.PointQueryListener;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;

public class RelevanceCallback extends BaseMapCallback {

	@Override
	public boolean handleMessage(Message msg) {

		Toast.makeText(mapGISFrame, "点击地图选择一个设备", Toast.LENGTH_SHORT).show();

		if (MobileConfig.MapConfigInstance != null && !MobileConfig.MapConfigInstance.IsVectorQueryOnline) {

			if (MobileConfig.MapConfigInstance.IsVectorQueryOnline) {
				onlineProcess();
			} else {
				offlineProcess();
			}
		}
		return false;
	}

	private void offlineProcess() {
		PointQueryListener pointQueryListener = new PointQueryListener(mapGISFrame, mapView, null);

		pointQueryListener.setPointQueryAnnotationListener(new MmtAnnotationListener() {

			@Override
			public void mapViewClickAnnotationView(MapView arg0, AnnotationView arg1) {

				Annotation annotation = arg1.getAnnotation();

				if (!(annotation instanceof MmtAnnotation)) {
					return;
				}

				MmtAnnotation mmtAnnotation = (MmtAnnotation) annotation;

				String no = mmtAnnotation.attrMap.get("编号");
				String guid = mmtAnnotation.attrMap.get("GUID");

				if (BaseClassUtil.isNullOrEmptyString(no) && BaseClassUtil.isNullOrEmptyString(guid)) {
					mapGISFrame.showToast("设备中不含有<编号>或<GUID>属性,或者属性值为空，不能使用该功能");

					return;
				}

				Intent intent = new Intent(mapGISFrame, RelevanceReportActivity.class);

				intent.putExtra("graphicMap", mmtAnnotation.attrMap);
				intent.putExtra("layerName", mmtAnnotation.attrMap.get("$图层名称$"));

				if (!BaseClassUtil.isNullOrEmptyString(no)) {
					intent.putExtra("fieldName", "编号");
					intent.putExtra("deviceKey", no);
				} else {
					intent.putExtra("fieldName", "GUID");
					intent.putExtra("deviceKey", guid);
				}

				mapGISFrame.startActivity(intent);
			}
		});

		mapView.setTapListener(pointQueryListener);

	}

	private void onlineProcess() {
		OnlinePointQueryListener pointQueryListener = new OnlinePointQueryListener(mapGISFrame, mapView);

		pointQueryListener.setPointQueryAnnotationListener(new MmtAnnotationListener() {

			@Override
			public void mapViewClickAnnotationView(MapView arg0, AnnotationView arg1) {

				Annotation annotation = arg1.getAnnotation();

				if (!(annotation instanceof MmtAnnotation)) {
					return;
				}

				MmtAnnotation mmtAnnotation = (MmtAnnotation) annotation;

				String deviceKey = "";
				String fieldName = "";

				if (mmtAnnotation.attrMap.containsKey("GUID")
						&& !BaseClassUtil.isNullOrEmptyString(mmtAnnotation.attrMap.get("GUID"))) {

					fieldName = "GUID";
					deviceKey = mmtAnnotation.attrMap.get("GUID");

				} else if (mmtAnnotation.attrMap.containsKey("编号")
						&& !BaseClassUtil.isNullOrEmptyString(mmtAnnotation.attrMap.get("编号"))) {

					fieldName = "编号";
					deviceKey = mmtAnnotation.attrMap.get("编号");

				}

				if (BaseClassUtil.isNullOrEmptyString(deviceKey)) {
					mapGISFrame.showToast("设备中不含有<GUID>或<编号>属性,不能使用该功能");
					return;
				}

				Intent intent = new Intent(mapGISFrame, RelevanceReportActivity.class);

				intent.putExtra("graphicMap", mmtAnnotation.attrMap);
				intent.putExtra("layerName", mmtAnnotation.attrMap.get("$图层名称$"));
				intent.putExtra("pipeNo", mmtAnnotation.attrMap.get("编号"));
				intent.putExtra("deviceKey", deviceKey);
				intent.putExtra("fieldName", fieldName);

				mapGISFrame.startActivity(intent);
			}
		});

		mapView.setTapListener(pointQueryListener);
	}
}
