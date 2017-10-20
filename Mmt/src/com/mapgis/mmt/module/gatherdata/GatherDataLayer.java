package com.mapgis.mmt.module.gatherdata;

import com.mapgis.mmt.common.util.BaseClassUtil;

import java.util.List;

public class GatherDataLayer {
	/**
	 * ID
	 */
	public int NodeID;

	/**
	 * 图层名称
	 */
	public String NodeName;

	/**
	 * 需要反馈的字段
	 */
	public String NodeValue;

	/**
	 * 点类型，线类型
	 */
	public String Type;

	public boolean isPoint() {
		return Type.equals("点");
	}

	public List<String> getFeedbackAttr() {
		return BaseClassUtil.StringToList(NodeValue, ",");
	}
}
