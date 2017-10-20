package com.mapgis.mmt.module.relevance;

public class DeviceMedia {
	/**
	 * GIS服务器名称
	 */
	public String mapServerName;
	/**
	 * 图层名称
	 */
	public String layerName;
	/**
	 * GUID
	 */
	public String elemGuid;
	/**
	 * 服务IP
	 */
	public String cityServerIp;
	/**
	 * 照片路径
	 */
	public String path;

	/**
	 * 上传人员
	 */
	public String uploader;

	/**
	 * 关联属性：GUID or 编号
	 */
	public String fldName;

	/**
	 * 删除web缓存
	 */
	public boolean isDelete = true;
}
