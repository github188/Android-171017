package com.mapgis.mmt.module.gatherdata;

import android.graphics.Color;
import android.graphics.PointF;

import com.google.gson.annotations.Expose;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicText;
import com.zondy.mapgis.geometry.Dot;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 数据采集GIS元素（点、线、面等）实体类
 * 
 * @author Zoro
 * 
 */
public class GatherElementBean {

	/**
	 * 唯一编码
	 */
	@Expose
	public int ID;

	/**
	 * 所属工程的唯一编码
	 */
	@Expose
	public int ProjectID;

	/**
	 * 点：索引编号，线：点索引编号-点索引编号
	 */
	@Expose
	public String ElemSN;

	/**
	 * 操作类型：添加、删除、修改、捕捉、打断
	 */
	@Expose
	public String Oper;

	/**
	 * 几何类型：点、线、区
	 */
	@Expose
	public String GeomType;

	/**
	 * 原有的空间几何信息：点对应的是(x,y);线对应的是(起点guid，终点guid)；原有的属性信息,键值对的字典
	 */
	@Expose
	public String OldAttr;

	/**
	 * 点：坐标点，线：点GUID,点GUID
	 */
	@Expose
	public String OldGeo;

	/**
	 * 新的空间几何信息：点对应的是(x,y);线对应的是(起点guid，终点guid)；原有的属性信息,键值对的字典
	 */
	@Expose
	public String NewAttr;

	/**
	 * 所属的图层名称
	 */
	@Expose
	public String LayerName;

	/**
	 * 点：坐标点，线：点GUID,点GUID
	 */
	@Expose
	public String NewGeo;

	/**
	 * 如果是基于原有的管网进行的属性编辑，此属性对应管件的某字段
	 */
	@Expose
	public String FieldName;

	/**
	 * 如果是基于原有的管网进行的属性编辑，此属性对应管件的某字段的值
	 */
	@Expose
	public String FieldValue;

	/**
	 * 照片的相对路径
	 */
	@Expose
	public String Photo;

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** 所在地图点 */
	public Dot mapDot;
    
	public String address;
	/** 是否是捕获点 */
	public boolean isCaptureDot;

	/** 是否可以移动 */
	public boolean canMove;

	/** 照片存放的绝对路径 */
	public String photosPaths;

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public GatherElementBean() {
	}

	/** 点的创建方式 */
	public GatherElementBean(String index, Dot mapDot, int ProjectGUID) {
		this.mapDot = mapDot;
		this.ElemSN = index;

		this.ProjectID = ProjectGUID;
		this.Oper = "新增";

		this.GeomType = "点";

		NewGeo = Convert.FormatDouble(mapDot.x) + "," + Convert.FormatDouble(mapDot.y);
	}

	/** 线的创建方式 */
	public GatherElementBean(String index, String GUIDs, int ProjectGUID) {
		this.ProjectID = ProjectGUID;
		this.Oper = "新增";
		this.ElemSN = index;

		this.GeomType = "线";
		this.NewGeo = GUIDs;
	}

	/**
	 * 创建文本图形
	 * 
	 * @return GraphicText
	 */
	public GraphicText createGraphicText() {
		GraphicText graphicText = new GraphicText(mapDot, ElemSN + "");
		graphicText.setColor(Color.WHITE);
		graphicText.setFontSize(30);
		graphicText.setAttributeValue("SN", ElemSN);
		graphicText.setAnchorPoint(new PointF(0.5f, 0.55f));
		return graphicText;
	}

	public GraphicPoint createGraphicPoint() {
		// 创建点图形
		GraphicPoint graphicPoint = new GraphicPoint(mapDot, 20);
		graphicPoint.setAttributeValue("SN", ElemSN);
		graphicPoint.setColor(Color.BLUE);
		return graphicPoint;
	}

	public LinkedHashMap<String, String> getNewAttrMap() {
		LinkedHashMap<String, String> attrMap = new LinkedHashMap<String, String>();

		// 初始化填写信息
		if (!BaseClassUtil.isNullOrEmptyString(NewAttr)) {

			List<String> strs = BaseClassUtil.StringToList(NewAttr, ",");

			for (String str : strs) {
				String[] keyValue = str.split(":");

				String key = "";
				String value = "";

				if (keyValue.length != 0) {
					key = keyValue[0];
				}

				if (keyValue.length >= 2) {
					value = keyValue[1];
				}

				attrMap.put(key, value);
			}
		}

		return attrMap;
	}

	public LinkedHashMap<String, String> getOldAttrMap() {
		LinkedHashMap<String, String> attrMap = new LinkedHashMap<String, String>();

		// 初始化填写信息
		if (!BaseClassUtil.isNullOrEmptyString(OldAttr)) {

			List<String> strs = BaseClassUtil.StringToList(OldAttr, ",");

			for (String str : strs) {
				String[] keyValue = str.split(":");

				String key = "";
				String value = "";

				if (keyValue.length != 0) {
					key = keyValue[0];
				}

				if (keyValue.length >= 2) {
					value = keyValue[1];
				}

				attrMap.put(key, value);
			}
		}

		return attrMap;
	}

	@Override
	public GatherElementBean clone() throws CloneNotSupportedException {
		GatherElementBean cloneBean = new GatherElementBean();
		cloneBean.canMove = this.canMove;
		cloneBean.GeomType = this.GeomType;
		cloneBean.ID = this.ID;
		cloneBean.LayerName = this.LayerName;
		cloneBean.mapDot = this.mapDot;
		cloneBean.NewAttr = this.NewAttr;
		cloneBean.NewGeo = this.NewGeo;
		cloneBean.OldAttr = this.OldAttr;
		cloneBean.Oper = this.Oper;
		cloneBean.FieldName = this.FieldName;
		cloneBean.FieldValue = this.FieldValue;
		cloneBean.ProjectID = this.ProjectID;
		cloneBean.ElemSN = this.ElemSN;
		cloneBean.isCaptureDot = this.isCaptureDot;
		cloneBean.Photo = this.Photo;
		cloneBean.photosPaths = this.photosPaths;
		return cloneBean;
	}

}
