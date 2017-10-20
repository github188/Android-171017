package com.mapgis.mmt.module.gatherdata.operate.bar;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.module.gatherdata.GatherDataUtils;
import com.mapgis.mmt.module.gatherdata.GatherElementBean;
import com.mapgis.mmt.module.gatherdata.GatherProjectBean;
import com.mapgis.mmt.module.gatherdata.operate.GatherDataOperateBar;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.android.mapview.MapView.MapViewTapListener;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * 底部工具条。<br>
 * 
 * 
 */
public class GatherDataForAreaBar extends GatherDataOperateBar implements OnClickListener {

	public GatherDataForAreaBar(MapGISFrame mapGISFrame, GatherProjectBean projectBean) {
		super(mapGISFrame, projectBean);

		this.mapView = mapGISFrame.getMapView();

		initToolBar();

		projectBean.showOnMap(mapView);

		zoomToProject();
	}

	/**
	 * 初始化操作工具条
	 * 
	 * @return 工具条View
	 */
	@Override
	protected void initToolBar() {
		removeAllViews();

		addView(GatherDataUtils.createTextView(mapGISFrame, "描点", this));
		addView(GatherDataUtils.createDivider(mapGISFrame));
		addView(GatherDataUtils.createTextView(mapGISFrame, "捕捉", this));
		addView(GatherDataUtils.createDivider(mapGISFrame));
		addView(GatherDataUtils.createTextView(mapGISFrame, "连线", this));
		addView(GatherDataUtils.createDivider(mapGISFrame));
		addView(GatherDataUtils.createTextView(mapGISFrame, "重置", this));
		addView(GatherDataUtils.createDivider(mapGISFrame));
		addView(GatherDataUtils.createTextView(mapGISFrame, "提交", this));
	}

	@Override
	protected void onCaptureFinished(LinkedHashMap<String, String> attrMap) {
		if (attrMap == null || attrMap.size() == 0) {
			mapGISFrame.showToast("捕捉失败,未查询到设备信息");
			return;
		}

		mapGISFrame.showToast("捕捉成功");

		// 将键值对属性信息借此成字符串
		String attrStr = "";

		Iterator<String> iterator = attrMap.keySet().iterator();

		while (iterator.hasNext()) {
			String key = iterator.next();

			if (key.startsWith("$")) {
				continue;
			}

			String value = attrMap.get(key);

			attrStr = attrStr + key + ":" + value + ",";
		}

		attrStr = attrStr.substring(0, attrStr.length() - 2);

		GatherElementBean bean = addElementBean();

		if (BaseClassUtil.isNullOrEmptyString(attrMap.get("编号"))) {
			bean.FieldName = "编号";
			bean.FieldValue = attrMap.get("编号");
		} else if (BaseClassUtil.isNullOrEmptyString(attrMap.get("GUID"))) {
			bean.FieldName = "GUID";
			bean.FieldValue = attrMap.get("GUID");
		}

		bean.NewGeo = "";
		bean.isCaptureDot = true;
		bean.OldAttr = attrStr;
		bean.Oper = "捕捉";
		bean.OldGeo = attrMap.get("$坐标$");
		bean.LayerName = attrMap.get("$图层名称$");

		executeAttr(bean, true);

		projectBean.showOnMap(mapView);
	}

	@Override
	public void onClick(View v) {
		if (!(v instanceof TextView))
			return;

		String text = ((TextView) v).getText().toString();

		if (text.equals("描点")) {// 加点点击事件，增加点
			GatherElementBean holdBean = null;

			for (GatherElementBean elementBean : projectBean.elementBeans) {
				if (elementBean.canMove) {
					holdBean = elementBean;
					break;
				}
			}

			if (holdBean != null) {
				holdBean = movePoint(holdBean);
				holdBean = null;
				mapGISFrame.showToast("移图完成");
			} else {
				holdBean = addElementBean();
			}

			projectBean.showOnMap(mapView);

			mapGISFrame.showToast("采集成功，请点击采集点进行属性编辑");

			executeAttr(holdBean, true);

		} else if (text.equals("提交")) {
			reportGatherResult();
		} else if (text.equals("连线")) {// 加线点击事件，增加线

			if (isOnMoveOpera()) {
				mapGISFrame.showToast("你正在执行移图操作，请先点击<描点>完成此操作");
				return;
			}

			mapView.setTapListener(new LinePointListener());

		} else if (text.equals("重置")) {
			resetProject();
		} else if (text.equals("捕捉")) {

			if (isOnMoveOpera()) {
				mapGISFrame.showToast("你正在执行移图操作，请先点击<描点>完成此操作");
				return;
			}

			new PointQueryTask().execute();
		}
	}

	private boolean isOnMoveOpera() {
		for (GatherElementBean elementBean : projectBean.elementBeans) {
			if (elementBean.canMove) {
				return true;
			}
		}
		return false;
	}

	private class LinePointListener implements MapViewTapListener {
		private GatherElementBean startBean;
		private GatherElementBean endBean;

		@Override
		public void mapViewTap(PointF arg0) {

			MapView mapView = mapGISFrame.getMapView();

			for (int i = mapView.getGraphicLayer().getGraphicCount() - 1; i >= 0; i--) {

				Graphic graphic = mapView.getGraphicLayer().getGraphic(i);

				if (graphic instanceof GraphicPoint && mapView.graphicHitTest(graphic, arg0.x, arg0.y)) {

					String SN = graphic.getAttributeValue("SN");

					if (BaseClassUtil.isNullOrEmptyString(SN))
						continue;

					// 点选起点和终点
					for (GatherElementBean bean : projectBean.elementBeans) {
						if (bean.ElemSN.equals(SN)) {
							if (startBean == null) {
								startBean = bean;
								mapGISFrame.showToast("起点选择成功");
								graphic.setColor(Color.GREEN);
								mapView.refresh();
							} else {
								endBean = bean;
							}
							break;
						}
					}

					// 判断两次点击是否为同一点
					if (endBean != null && endBean.ElemSN.equals(startBean.ElemSN)) {

						mapGISFrame.showToast("该点已经作为起始点，请选择其他点进行连线");

						projectBean.showOnMap(mapView);

						startBean = null;
						endBean = null;

						return;
					}

					// 起点和终点都选择结束后
					if (startBean != null && endBean != null) {

						// 判断该线是否已经添加过
						String lineSN1 = startBean.ElemSN + "-" + endBean.ElemSN;
						String lineSN2 = endBean.ElemSN + "-" + startBean.ElemSN;

						for (GatherElementBean elementBean : projectBean.elementBeans) {
							if (lineSN1.equals(elementBean.ElemSN) || lineSN2.equals(elementBean.ElemSN)) {

								mapGISFrame.showToast("该线已经添加，请重新选择");

								projectBean.showOnMap(mapView);

								startBean = null;
								endBean = null;

								return;
							}
						}

						// 创建线设备
						String lineBeanSN = startBean.ElemSN + "-" + endBean.ElemSN;

						if (Integer.valueOf(startBean.ElemSN) > Integer.valueOf(endBean.ElemSN))
							lineBeanSN = endBean.ElemSN + "-" + startBean.ElemSN;

						GatherElementBean lineBean = new GatherElementBean(lineBeanSN, startBean.ID + "," + endBean.ID,
								endBean.ProjectID);

						projectBean.elementBeans.add(lineBean);

						mapGISFrame.showToast("管线添加成功成功");

						// 重置地图点击事件
						registTagListener();

						projectBean.showOnMap(mapView);

						executeAttr(lineBean, true);
					}

					break;
				}
			}
		}
	}
}
