package com.mapgis.mmt.module.gis.onliemap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 在线图层详情信息，主要记录指定图层的包含字段信息及类型 */
public class OnlineLayerInfo {
	public String id;
	public String name;
	public int parentLayerId;
	public boolean defaultVisibility;
    public String geometryType;
	public OnlineLayerAttribute[] fields;
    public String Legend;//图例base64编码
    public List<String> subLayerIds;

	/** 获取图层信息属性字段名称 */
	public List<String> getFieldsNames() {
		String[] hidenFields = ("LAYER,class,OID,AuxFlag,FID1,ID,MediaFlag,LayerID,mpLayer,"
				+ "NGCLS,OCLS,TCLS,ElemID,PID,AREARANGE,id2,id1,id4,id3,PATHRANGE,PATHNAME,"
				+ "AREAID,PATHID,ID5,$ReturnResultString$,$ClickPoint$,emapgisoid,My_ID,emapgisid,"
				+ "mpLength,mpLayer,Task_ID,GUID,FCLS,SYSLAYERNO").split(",");

		List<String> fieldsNames = new ArrayList<String>();

		if (fields != null) {
			for (OnlineLayerAttribute attribute : fields) {
				if (!Arrays.asList(hidenFields).contains(name)) {
					fieldsNames.add(attribute.name);
				}
			}
		}
		return fieldsNames;
	}

	public class OnlineLayerAttribute {
		public String name;
		public String type;
		public String alias;
		public String DefVal;
		public String Shape;
		public String ShapeVal;
		public String Length;
		public String PointLen;
		public boolean AllowNull;
	}
}
