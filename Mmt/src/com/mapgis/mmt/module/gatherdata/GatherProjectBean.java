package com.mapgis.mmt.module.gatherdata;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据采集工程实体类
 * 
 * @author Zoro
 * 
 */
public class GatherProjectBean implements ISQLiteOper {
	/**
	 * 工程唯一标识
	 */
	@Expose
	public int ID;
	/**
	 * 案件编号
	 */
	@Expose
	public String EventCode;
	/**
	 * 案件编号
	 */
	@Expose
	public String CaseNo;

	/**
	 * 工程类型：点补漏，线补漏，片区采集
	 */
	@Expose
	public String Type;

	/**
	 * 工程名称
	 */
	@Expose
	public String Name;

	/**
	 * 上报人姓名
	 */
	@Expose
	public String Repoter;

	/**
	 * 上报时间
	 */
	@Expose
	public String ReportTime;

	/**
	 * 工程范围
	 */
	@Expose
	public String Range;

	/**
	 * GIS数据更新状态：未处理，已处理，已完工
	 */
	@Expose
	public String Status;

	/**
	 * 当前环节
	 */
	public String ActivityName;

	/**
	 * 承办意见
	 */
	public String Opinion;
	/**
	 * 上报描述
	 */
	public String Describte;

	@Expose
	public List<GatherElementBean> elementBeans = new ArrayList<GatherElementBean>();

	public int LastSN = 0;

	private GatherProjectBean saveProjectBean;

	/**
	 * 工程地址坐标，存储前一次的坐标
	 */
	@Expose
	public Dot dot;
	
	/**
	 * 工程地址,定位时可下拉选择精确的地址（第一个点的地址名）
	 */
	@Expose
	public List<String> addressList = new ArrayList<String>();
	
	/**
	 * 工程地址，从下拉框中选择（用于填充线普查的后两个元素坐标）
	 * 或从服务获取（用于填充列表）
	 */
	public String address;
	
	@Expose
	public boolean addressChanged=false;
	/**
	 * 将工程绘制在地图上
	 * 
	 * @param mapView
	 */
	public void showOnMap(MapView mapView) {
		if (elementBeans == null)
			return;

		GraphicLayer graphicLayer = mapView.getGraphicLayer();
		graphicLayer.removeAllGraphics();

		List<Graphic> pointGraphics = new ArrayList<Graphic>();

		List<GatherElementBean> LineBeans = new ArrayList<GatherElementBean>();

		for (GatherElementBean bean : elementBeans) {
			if (bean.GeomType.equals("点")) {
				pointGraphics.add(bean.createGraphicPoint());
				pointGraphics.add(bean.createGraphicText());
			} else if (bean.GeomType.equals("线"))
				LineBeans.add(bean);
		}

		for (GatherElementBean lineBean : LineBeans) {
			GraphicPolylin graphicPolylin = new GraphicPolylin();
			graphicPolylin.setLineWidth(5);
			graphicPolylin.setColor(Color.BLUE);

			String[] sns = lineBean.ElemSN.split("-");
			Dot sDot = findBySN(sns[0]).mapDot;
			Dot eDot = findBySN(sns[1]).mapDot;

			graphicPolylin.appendPoint(sDot);
			graphicPolylin.appendPoint(eDot);
			graphicPolylin.setAttributeValue("SN", lineBean.ElemSN);

			graphicLayer.addGraphic(graphicPolylin);
		}

		graphicLayer.addGraphics(pointGraphics);

		mapView.refresh();
	}

	public void caluteRange() {
		if (elementBeans == null || elementBeans.size() == 0) {
			return;
		}

		double minX = 0, minY = 0, maxX = 0, maxY = 0;

		for (GatherElementBean bean : elementBeans) {

			if (!bean.GeomType.equals("点"))
				continue;

			String geo = BaseClassUtil.isNullOrEmptyString(bean.NewGeo) ? bean.OldGeo
					: bean.NewGeo;

			if (BaseClassUtil.isNullOrEmptyString(geo) || !geo.contains(","))
				continue;

			String[] location = geo.split(",");

			double x = Double.valueOf(location[0]);
			double y = Double.valueOf(location[1]);

			if (x <= minX || minX == 0)
				minX = x;

			if (x >= maxX || maxX == 0)
				maxX = x;

			if (y <= minY || minY == 0)
				minY = y;

			if (y >= maxY || maxY == 0)
				maxY = y;
		}

		Range = minX + "," + minY + "," + maxX + "," + maxY;
	}

	/**
	 * 用NewGeo或OldGeo初始化地图坐标和project最后的SN信息，<br>
	 * 并且，若PipeGUID不为空，则判断为捕捉到的设备
	 */
	public void initGatherDataElementDot() {

		if (elementBeans == null || elementBeans.size() == 0) {
			return;
		}

		// 获取最后的编号
		for (int i = elementBeans.size() - 1; i >= 0; i--) {
			GatherElementBean bean = elementBeans.get(i);

			if (bean != null && bean.GeomType.equals("点")) {
				LastSN = Integer.valueOf(bean.ElemSN);
				break;
			}
		}

		for (GatherElementBean bean : elementBeans) {

			if (!bean.GeomType.equals("点")) {
				continue;
			}

			if (!BaseClassUtil.isNullOrEmptyString(bean.OldGeo))
				bean.isCaptureDot = true;

			String dotStr = bean.NewGeo;

			if (BaseClassUtil.isNullOrEmptyString(dotStr)) {
				dotStr = bean.OldGeo;
			}

			if (BaseClassUtil.isNullOrEmptyString(dotStr))
				continue;

			String[] dotStrArr = dotStr.split(",");

			bean.mapDot = new Dot(Float.valueOf(dotStrArr[0]),
					Float.valueOf(dotStrArr[1]));
		}
	}

	/**
	 * 通过SN号，找到对应的GatherElementBean
	 * 
	 * @param SN
	 *            序号
	 * @return 找到返回GatherElementBean，否则返回null
	 */
	private GatherElementBean findBySN(String SN) {
		for (GatherElementBean bean : elementBeans) {
			if (bean.ElemSN.equals(SN))
				return bean;
		}
		return null;
	}

	/** 缓存当前状态 */
	public void save() {
		try {
			saveProjectBean = clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	/** 取出缓存状态，注意：取出后，取出的是前一个的状态，不保证前一个一定有缓存状态，即再调用该方法，极可能返回的是空 */
	public GatherProjectBean restore() {
		return saveProjectBean;
	}

	@Override
	public GatherProjectBean clone() throws CloneNotSupportedException {
		GatherProjectBean cloneBean = new GatherProjectBean();
		cloneBean.CaseNo = this.CaseNo;
		cloneBean.ID = this.ID;
		cloneBean.LastSN = this.LastSN;
		cloneBean.Name = this.Name;
		cloneBean.Range = this.Range;
		cloneBean.ReportTime = this.ReportTime;
		cloneBean.Repoter = this.Repoter;
		cloneBean.Status = this.Status;
		cloneBean.Type = this.Type;

		if (this.elementBeans != null) {
			cloneBean.elementBeans = new ArrayList<GatherElementBean>();
			for (GatherElementBean elementBean : this.elementBeans)
				cloneBean.elementBeans.add(elementBean.clone());
		}

		return cloneBean;
	}

	/**
	 * 插入或更新数据。<br>
	 * 当存在相同Name的数据时，则更新数据，否则插入数据
	 * 
	 * @return 影响行数
	 */
	public long insertOrUpdate() {
		long i = -1;

		List<GatherProjectBean> queryBeans = DatabaseHelper.getInstance()
				.query(GatherProjectBean.class, "Name = '" + Name + "'");

		if (queryBeans == null || queryBeans.size() == 0)
			i = DatabaseHelper.getInstance().insert(this);
		else
			i = DatabaseHelper.getInstance().update(GatherProjectBean.class,
					generateContentValues(), "Name = '" + Name + "'");

		return i;
	}

	/** 删除本地数据 */
	public void delete() {
		DatabaseHelper.getInstance().delete(GatherProjectBean.class,
				"Name = '" + Name + "'");
	}

	@Override
	public String getTableName() {
		return "GatherProject";
	}

	@Override
	public String getCreateTableSQL() {
		return "(CaseNo,ID,LastSN,Name,Range,ReportTime,Repoter,Status,Type,ElementBeansStr)";
	}

	@Override
	public SQLiteQueryParameters getSqLiteQueryParameters() {
		return null;
	}

	@Override
	public ContentValues generateContentValues() {
		ContentValues contentValues = new ContentValues();
		contentValues.put("CaseNo", CaseNo);
		contentValues.put("ID", ID);
		contentValues.put("LastSN", LastSN);
		contentValues.put("Name", Name);
		contentValues.put("Range", Range);
		contentValues.put("ReportTime", ReportTime);
		contentValues.put("Repoter", Repoter);
		contentValues.put("Status", Status);
		contentValues.put("Type", Type);

		String ElementBeansStr = new Gson().toJson(elementBeans,
				new TypeToken<List<GatherElementBean>>() {
				}.getType());

		contentValues.put("ElementBeansStr", ElementBeansStr);

		return contentValues;
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
		this.CaseNo = cursor.getString(0);
		this.ID = cursor.getInt(1);
		this.LastSN = cursor.getInt(2);
		this.Name = cursor.getString(3);
		this.Range = cursor.getString(4);
		this.ReportTime = cursor.getString(5);
		this.Repoter = cursor.getString(6);
		this.Status = cursor.getString(7);
		this.Type = cursor.getString(8);

		List<GatherElementBean> beans = new Gson().fromJson(
				cursor.getString(9), new TypeToken<List<GatherElementBean>>() {
				}.getType());

		if (beans != null) {
			this.elementBeans = beans;
		}
	}
}
