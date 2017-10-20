package com.repair.eventreport;

public class EventReportEntity {
	
	/**
	 * 事件类型
	 */
	public String EventType;

	/**
	 * 事件内容
	 */
	public String EventClass;

	/**
	 * 地址
	 */
	public String Address;

	/**
	 * 描述
	 */
	public String Description;

	/**
	 * 图片路径
	 */
	public String ImageUrl;

	/**
	 * 录音路径
	 */
	public String AudiosUrl;

	/**
	 * 上报人ID
	 */
	public String ReportID;

	/**
	 * 上报人
	 */
	public String ReportName;

	/**
	 * 坐标
	 */
	public String Position;

	/**
	 * 事件关联的设备的图层名称
	 */
	public String LayerName;

	/**
	 * 事件关联的设备的字段名称
	 */
	public String FieldName;

	/**
	 * 事件关联的设备的字段值
	 */
	public String FieldValue;

	public void setIdentityField(String[] args) {
		if (args == null) {
			return;
		}

		this.LayerName = args[0];
		this.FieldName = args[1];
		this.FieldValue = args[2];
	}
}
