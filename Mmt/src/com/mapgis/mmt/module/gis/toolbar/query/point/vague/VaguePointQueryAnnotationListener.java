package com.mapgis.mmt.module.gis.toolbar.query.point.vague;

import android.graphics.BitmapFactory;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.List;

public class VaguePointQueryAnnotationListener extends MmtAnnotationListener {

	Annotation blueAnnotation;
	Dot blueDot;
	List<Graphic> graphics;

	/** 1-10 的 点标签， 11 小点标签 */
	int[] icons = { R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc, R.drawable.icon_markd,
			R.drawable.icon_marke, R.drawable.icon_markf, R.drawable.icon_markg, R.drawable.icon_markh, R.drawable.icon_marki,
			R.drawable.icon_markj, R.drawable.icon_mark_pt };

	/** 地图上界面上点击的图标的角标 */
	public int clickWhichIndex = -1;

	/** 标识 是否 隐藏 因 点击 Annotation 引起的 mapView的 TapListener 事件 */
	private Boolean hideTapListener = false;

	@Override
	public Boolean getHideTapListener() {
		return hideTapListener;
	}

	public Dot getBlueDot() {
		return blueDot;
	}

	public Annotation getBlueAnnotation() {
		return blueAnnotation;
	}

	public void setBlueAnnotation(Annotation blueAnnotation) {
		this.blueAnnotation = blueAnnotation;
	}

	public void setBlueDot(Dot blueDot) {
		this.blueDot = blueDot;
	}

	@Override
	public void setHideTapListener(Boolean hideTapListener) {
		this.hideTapListener = hideTapListener;
	}

	public VaguePointQueryAnnotationListener setGraphics(List<Graphic> graphics) {
		this.graphics = graphics;

		return this;
	}

	@Override
	public void mapViewClickAnnotation(MapView mapview, Annotation annotation) {
		super.mapViewClickAnnotation(mapview, annotation);
		this.hideTapListener = true;

		// 将变蓝显示的图标还原
		if (clickWhichIndex != -1) {
			mapview.getAnnotationLayer()
					.getAnnotation(clickWhichIndex)
					.setImage(
							BitmapFactory.decodeResource(mapview.getResources(),
									icons[clickWhichIndex > 9 ? 10 : clickWhichIndex]));
		}

		clickWhichIndex = mapview.getAnnotationLayer().indexOf(annotation);
		annotation.setImage(BitmapFactory.decodeResource(mapview.getResources(), R.drawable.icon_lcoding));

		if (this.blueAnnotation != null) {
			mapview.getAnnotationLayer().removeAnnotation(this.blueAnnotation);
			this.blueAnnotation = null;
		}

		mapview.refresh();
	}
}
