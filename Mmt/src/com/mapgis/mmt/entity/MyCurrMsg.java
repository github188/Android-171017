package com.mapgis.mmt.entity;

/**
 * 
 * 消息条数接口数据模型<br>
 * 
 * {"rnt":{"IsSuccess":true,"Msg":""}, "newShortMessageCount":0,
 * "newMyPlanCount":3, "newOwnTaskCount":0, "newOwnValveCount":0,
 * "newOwnJobCount":0}
 * 
 * @author Liangjun
 * 
 */
public class MyCurrMsg {
	public class RNT {
		public boolean IsSuccess;
		public String Msg;
	}

	public RNT rnt;
	public int newShortMessageCount;
	public int newMyPlanCount;
	public int newOwnTaskCount;
	public int newMyRepairTaskCount;
}
