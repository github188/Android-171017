package com.mapgis.mmt.module.gis.toolbar.accident.entity;

import com.google.gson.annotations.Expose;

import java.util.LinkedHashMap;

public class AccidentAttribute {
	// 属性信息都是存储在ID字段中...
	@Expose
	public String ID;

	/** 将长字符串描述信息转坏为键值对信息 */
	public LinkedHashMap<String, String> attrStrToMap() {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

		if (ID == null || ID.trim().length() == 0) {
			return map;
		}

		if (ID.contains(",")) {// 若是有多个属性， 每一个属性之间都是,分割
			String[] attrs = ID.split(",");

			for (String attr : attrs) {
				// 每个属性的描述与值都是按:分割
				if (attr.contains(":")) {
					String[] attrArrays = attr.split(":");
					// 存在有描述但是没有值的情况，没有值的时候存入空字符串
					map.put(attrArrays[0], attrArrays.length > 1 ? attrArrays[1] : "");
				}
			}
		} else if (ID.contains(":")) {// 若只有一个属性
			String[] attrArrays = ID.split(":");
			// 存在有描述但是没有值的情况，没有值的时候存入空字符串
			map.put(attrArrays[0], attrArrays.length > 1 ? attrArrays[1] : "");
		}

		return map;
	}
}
