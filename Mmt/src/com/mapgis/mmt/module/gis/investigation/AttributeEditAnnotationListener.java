package com.mapgis.mmt.module.gis.investigation;

import android.app.Activity;
import android.content.Intent;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.mapview.MapView;

import java.util.HashMap;

public class AttributeEditAnnotationListener extends DefaultMapViewAnnotationListener {
	protected Graphic graphic;

	public AttributeEditAnnotationListener setGraphic(Graphic graphic) {
		this.graphic = graphic;
		return this;
	}

	@Override
	public void mapViewClickAnnotationView(MapView arg0, AnnotationView arg1) {
		Intent intent = new Intent("com.mapgis.mmt.module.gis.toolbar.investigation.AttributeEditActivity");

		HashMap<String, String> graphicMap = new HashMap<String, String>();
		for (int m = 0; m < graphic.getAttributeNum(); m++) {
			graphicMap.put(graphic.getAttributeName(m), graphic.getAttributeValue(m));
		}

		intent.putExtra("graphicMap", graphicMap);

		intent.putExtra("fromWhere", "gisDevice");

		String place = graphic.getAttributeValue("位置");

		if (BaseClassUtil.isNullOrEmptyString(place)) {
			place = graphic.getAttributeValue("所在位置");
		}

		if (BaseClassUtil.isNullOrEmptyString(place)) {
			place = graphic.getAttributeValue("道路名");
		}

		if (BaseClassUtil.isNullOrEmptyString(place)) {
			place = "";
		}

		intent.putExtra("graphic", graphic);
		intent.putExtra("place", place);
		intent.putExtra("xy", graphic.getCenterPoint().toString());
		intent.putExtra("layerName", graphic.getAttributeValue("$图层名称$"));
		intent.putExtra("pipeNo", graphic.getAttributeValue("编号"));

		((Activity) arg0.getContext()).startActivityForResult(intent, 0);
	}
}