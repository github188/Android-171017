package com.mapgis.mmt.module.gis.toolbar.accident.entity;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.Graphic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class AccidentIdentify{
	@Expose
	public int totalRcdNum;
	@Expose
	public String errorMsg;
	@Expose
	public String displayFieldName;
	@Expose
	public String geometryType;
	@Expose
	public AccidentFeature[] features;
	@Expose
	public int layerId;

	/** 自定义字段，用来标识是否在地图上显示 */
    @Expose
	public boolean isAnnotationShow = false;

	/**
	 * 获取属性信息，采用字符串显示
	 * 
	 * @return 该条件下所有设备的属性信息集合
	 */
	public List<String> getAttrStr() {
		List<String> attrsStr = new ArrayList<>();

		if (features != null) {

			for (AccidentFeature feature : features) {

				if (feature != null && feature.attributes != null && feature.attributes.ID != null) {
					attrsStr.add(feature.attributes.ID);
				}
			}
		}

		return attrsStr;
	}

	/**
	 * 获取属性信息，采用键值对显示
	 * 
	 * @return 该条件下所有设备的属性信息的键值对
	 */
	public List<LinkedHashMap<String, String>> getAttrMap() {
		List<LinkedHashMap<String, String>> attrsStr = new ArrayList<>();

		if (features != null) {

			for (AccidentFeature feature : features) {

				if (feature != null && feature.attributes != null && feature.attributes.ID != null) {
					attrsStr.add(feature.attributes.attrStrToMap());
				}
			}
		}

		return attrsStr;
	}

	/** 获取该图层下所有的线图形 */
	public List<Graphic> getPolylins() {
		List<Graphic> polylins = new ArrayList<>();

		if (features != null) {
			for (AccidentFeature feature : features) {
				if (feature != null && feature.geometry != null) {
					polylins.add(feature.geometry.createPolylin());
				}
			}
		}

		return polylins;
	}

	/** 获取该图层下所有的标注 */
	public List<Annotation> getAnnotations(String desc, Bitmap bitmap) {
		List<Annotation> annotations = new ArrayList<>();

		if (features != null) {
			for (AccidentFeature feature : features) {
				if (feature != null && feature.geometry != null && feature.isShowInMap) {
					Annotation annotation = feature.createAnnotation(desc, bitmap);
					if (annotation != null) {
						annotations.add(annotation);
					}
				}
			}
		}

		return annotations;
	}

    /** 获取该图层下所有的标注 */
    public List<Annotation> getAnnotations(Bitmap bitmap) {
        List<Annotation> annotations = new ArrayList<>();

        if (features != null) {
            for (AccidentFeature feature : features) {
                if (feature != null && feature.geometry != null && feature.isShowInMap) {

                    String desc = feature.attributes.attrStrToMap().get("ElemID");
                    Annotation annotation = feature.createAnnotation(desc, bitmap);

                    if (annotation != null) {
                        annotations.add(annotation);
                    }
                }
            }
        }

        return annotations;
    }
}
