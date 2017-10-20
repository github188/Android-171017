package com.mapgis.mmt.module.gis.toolbar.accident.entity;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class AccidentResult{
	@Expose
	public AccidentIdentify identify;
	@Expose
	public AccidentIdentify valve;// 需关阀门
	@Expose
	public AccidentIdentify user;// 用户
	@Expose
	public AccidentIdentify line;// 影响管段
	// public AccidentArea[] area;//影响区域
	@Expose
	public AccidentIdentify source;// 水表
	@Expose
	public AccidentIdentify center;// 接水点
	@Expose
	public String errorMsg;
	@Expose
	public String guid;
	@Expose
	public AccidentIdentify unvalve;// 失效设备

	public final static String[] resultTypes = new String[] { "需关阀门", "影响管段", "水表", "接水点", "用户" };

	public final AccidentIdentify[] identifies = new AccidentIdentify[] { valve, line, source, center, user };

	/** 根据索引获取对应的标识信息 */
	public AccidentIdentify getAccidentIdentifyByIndex(int index) {
		AccidentIdentify returnIdentify = null;

		switch (index) {
		case 0:
			returnIdentify = valve;
			break;
		case 1:
			returnIdentify = line;
			break;
		case 2:
			returnIdentify = source;
			break;
		case 3:
			returnIdentify = center;
			break;
		case 4:
			returnIdentify = user;
			break;
		}

		return returnIdentify;
	}

	/**
	 * 获取所有的标注的个数
	 */
	public int getAllAnnotationCount(){
		int count = 0;
        for (int i = 0; i < identifies.length; i++) {
            AccidentIdentify identify = getAccidentIdentifyByIndex(i);
            if (identify != null){
                count += identify.totalRcdNum;
            }
        }
		return count;
	}

	/** 需要二次关阀的设备 */
	public final String twiceCloseValve = "需关阀门";

	/** 获取含有计数的设备类型信息集合 */
	public List<String> getResultTypesWithCount() {
		List<String> result = new ArrayList<>();
		result.add(resultTypes[0] + ((valve == null || valve.totalRcdNum == 0) ? "" : "(" + valve.totalRcdNum + ")"));
		result.add(resultTypes[1] + ((line == null || line.totalRcdNum == 0) ? "" : "(" + line.totalRcdNum + ")"));
		result.add(resultTypes[2] + ((source == null || source.totalRcdNum == 0) ? "" : "(" + source.totalRcdNum + ")"));
		result.add(resultTypes[3] + ((center == null || center.totalRcdNum == 0) ? "" : "(" + center.totalRcdNum + ")"));
		result.add(resultTypes[4] + ((user == null || user.totalRcdNum == 0) ? "" : "(" + user.totalRcdNum + ")"));
		return result;
	}

	/**
	 * 根据类型获取对应的设备属性信息，用字符串的形式存储
	 * 
	 * @param type
	 *            类型
	 * @return 包含该类型设备信息的集合，采用长字符串的方式
	 */
	public List<String> valveEquipeStrInfos(String type) {
		List<String> list = new ArrayList<>();

		if (type == null) {
			return list;
		}

		switch (Arrays.asList(resultTypes).indexOf(type)) {
		case 0:
			list.addAll(getAttrStrFromAccidentIdentify(valve));
			break;
		case 1:
			list.addAll(getAttrStrFromAccidentIdentify(line));
			break;
		case 2:
			list.addAll(getAttrStrFromAccidentIdentify(source));
			break;
		case 3:
			list.addAll(getAttrStrFromAccidentIdentify(center));
			break;
		case 4:
			list.addAll(getAttrStrFromAccidentIdentify(user));
			break;
		default:
			break;
		}

		return list;
	}

	/**
	 * 根据类型获取对应的设备属性信息，用键值对的形式存储
	 * 
	 * @param type
	 *            类型
	 * @return 包含该类型设备信息的集合，采用键值对的方式
	 */
	public List<LinkedHashMap<String, String>> valveEquipeMapInfos(String type) {
		List<LinkedHashMap<String, String>> attrsMap = new ArrayList<>();

		if (type == null) {
			return attrsMap;
		}

		switch (Arrays.asList(resultTypes).indexOf(type)) {
		case 0:
			attrsMap.addAll(getAttrMapFromAccidentIdentify(valve));
			break;
		case 1:
			attrsMap.addAll(getAttrMapFromAccidentIdentify(line));
			break;
		case 2:
			attrsMap.addAll(getAttrMapFromAccidentIdentify(source));
			break;
		case 3:
			attrsMap.addAll(getAttrMapFromAccidentIdentify(center));
			break;
		case 4:
			attrsMap.addAll(getAttrMapFromAccidentIdentify(user));
			break;
		default:
			break;
		}

		return attrsMap;

	}

	/** 从AccidentIdentify中获取设备信息 */
	private List<String> getAttrStrFromAccidentIdentify(AccidentIdentify identify) {
		List<String> attrs = new ArrayList<>();

		if (identify != null) {
			attrs.addAll(identify.getAttrStr());
		}

		return attrs;
	}

	/** 从AccidentIdentify中获取设备信息 */
	private List<LinkedHashMap<String, String>> getAttrMapFromAccidentIdentify(AccidentIdentify identify) {
		List<LinkedHashMap<String, String>> attrs = new ArrayList<>();

		if (identify != null) {
			attrs.addAll(identify.getAttrMap());
		}

		return attrs;
	}
}
