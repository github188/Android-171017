package com.mapgis.mmt.module.gis.toolbar.query;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;
import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;

public class MmtAnnotationListener extends DefaultMapViewAnnotationListener {

	int[] icons = { R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc, R.drawable.icon_markd, R.drawable.icon_marke,
			R.drawable.icon_markf, R.drawable.icon_markg, R.drawable.icon_markh, R.drawable.icon_marki, R.drawable.icon_markj };

	/** 地图上界面上点击的图标的角标 */
	public int clickWhichIndex = -1;

	/** 标识 是否 隐藏 因 点击 Annotation 引起的 mapView的 TapListener 事件 */
	private Boolean hideTapListener = false;

	private boolean isBlueShow = true;

	public void setBlueShow(boolean isBlueShow) {
		this.isBlueShow = isBlueShow;
	}

	public Boolean getHideTapListener() {
		return hideTapListener;
	}

	public void setHideTapListener(Boolean hideTapListener) {
		this.hideTapListener = hideTapListener;
	}

	@Override
	public void mapViewClickAnnotation(MapView mapview, Annotation annotation) {
		try {
			super.mapViewClickAnnotation(mapview, annotation);
			this.hideTapListener = true;

			if (!isBlueShow) {
				return;
			}

			// 将变蓝显示的图标还原
			if (clickWhichIndex != -1) {
				mapview.getAnnotationLayer()
						.getAnnotation(clickWhichIndex)
						.setImage(
								BitmapFactory.decodeResource(mapview.getResources(),
										clickWhichIndex >= icons.length ? R.drawable.icon_gcoding : icons[clickWhichIndex]));
			}

			clickWhichIndex = mapview.getAnnotationLayer().indexOf(annotation);
			annotation.setImage(BitmapFactory.decodeResource(mapview.getResources(), R.drawable.icon_gcoding));

			mapview.refresh();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void mapViewClickAnnotationView(MapView arg0, AnnotationView arg1) {
		Annotation annotation = arg1.getAnnotation();

		if (!(annotation instanceof MmtAnnotation)) {
			return;
		}

		MmtAnnotation mmtAnnotation = (MmtAnnotation) annotation;

		Activity activity = (Activity) arg0.getContext();

		Intent intent = new Intent(activity, PipeDetailActivity.class);

		intent.putExtra("graphicMap", mmtAnnotation.attrMap);
        //activity之间只能传递无序的hashmap,增加graphicMapStr是为了获取有序的属性值
        intent.putExtra("graphicMapStr",new Gson().toJson(mmtAnnotation.attrMap));
		intent.putExtra("fromWhere", "gisDevice");
		intent.putExtra("place", mmtAnnotation.getDescription());
		intent.putExtra("xy", mmtAnnotation.getPoint().toString());
		intent.putExtra("layerName", mmtAnnotation.attrMap.get("$图层名称$"));
		intent.putExtra("pipeNo", mmtAnnotation.attrMap.get("编号"));
		intent.putExtra("needLoc", false);

		activity.startActivityForResult(intent, 0);
	}
}
