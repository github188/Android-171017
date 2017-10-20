package com.mapgis.mmt.entity;

import java.util.ArrayList;

/**
 * 带业务数据的返回结果
 * 
 * @author Administrator
 * 
 * @param <T>
 */
public class ResultData<T> extends ResultWithoutData {

	/**
	 * 业务数据集合，泛型支持
	 */
	public ArrayList<T> DataList = new ArrayList<T>();

	/**
	 * 适应于只返回一个结果的情况
	 * 
	 * @return
	 */
	public T getSingleData() {
		return DataList.get(0);
	}
}
