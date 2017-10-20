package com.mapgis.mmt.module.gis.toolbar.accident.entity;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;
import com.zondy.mapgis.android.annotation.Annotation;

public class AccidentFeature {
	@Expose
	public AccidentAttribute attributes;
	@Expose
	public AccidentGeometry geometry;

    /*
     * 用来标识是否需要显示到地图上
     *      默认所有的信息都要展示到地图上
     */
    @Expose
    public boolean isShowInMap = true;

	/** 自定义属性，用来标识需要二次关阀时，此设备是否被选中 */
    @Expose
	public boolean isChecked;

	/** 标注，自定义属性,用来在地图上显示该设备 */
	public AccidentAnnotation annotation;

	/** 图片，自定义属性,用来在地图上区分显示 */
	public Bitmap bitmap;

	public Annotation createAnnotation(String desc, Bitmap bitmap) {
		this.bitmap = bitmap;

		if (geometry != null) {

			// String attrStr = new
			// GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
			// .toJson(this, AccidentFeature.class);

			// annotation = new AccidentAnnotation(attrStr, desc, "",
			// geometry.getCenterDot(), bitmap);
			annotation = new AccidentAnnotation(desc, "", geometry.getCenterDot(), bitmap);
			annotation.setCanShowAnnotationView(true);
			annotation.accidentFeature = this;

			return annotation;
		}

		return null;
	}
}
