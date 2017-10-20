package com.mapgis.mmt.module.gis.toolbar.online.query.point;

import android.app.Activity;
import android.content.Intent;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;

public class OnlinePointQueryAnnotationListener extends DefaultMapViewAnnotationListener {
	protected OnlineFeature result;

	public OnlinePointQueryAnnotationListener setGraphic(OnlineFeature result) {
		this.result = result;

		return this;
	}

	@Override
	public void mapViewClickAnnotationView(MapView arg0, AnnotationView arg1) {
		Intent intent = new Intent(arg0.getContext(), PipeDetailActivity.class);

		intent.putExtra("graphicMap", result.attributes);
        intent.putExtra("graphicMapStr", new Gson().toJson(result.attributes));

		intent.putExtra("fromWhere", "gisDevice");

		String place = result.attributes.get("位置");

		if (BaseClassUtil.isNullOrEmptyString(place)) {
			place = result.attributes.get("所在位置");
		}

		if (BaseClassUtil.isNullOrEmptyString(place)) {
			place = result.attributes.get("道路名");
		}

		if (BaseClassUtil.isNullOrEmptyString(place)) {
			place = "";
		}

		intent.putExtra("place", place);
		intent.putExtra("xy", result.geometry.toDot().toString());
		// intent.putExtra("layerName", result.attributes.get("$图层名称$"));
		// intent.putExtra("pipeNo", result.attributes.get("编号"));
		arg0.getContext().startActivity(intent);
	}
}
