package com.mapgis.mmt.module.gis.toolbar.accident.entity;

import android.graphics.Bitmap;

import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.geometry.Dot;

public class AccidentAnnotation extends Annotation {
	private static final long serialVersionUID = -3597140635795941686L;

	public AccidentFeature accidentFeature;

	public AccidentAnnotation(long arg0) {
		super(arg0);
	}

	public AccidentAnnotation(String arg0, String arg1, Dot arg3, Bitmap arg4) {
		super(arg0, arg1, arg3, arg4);
	}

	public AccidentAnnotation(String arg0, String arg1, String arg2, Dot arg3, Bitmap arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
	}

}
