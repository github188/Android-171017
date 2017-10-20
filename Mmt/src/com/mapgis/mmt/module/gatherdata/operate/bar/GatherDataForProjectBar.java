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

/**
 * 底部工具条。<br>
 * 
 * 
 */
public class GatherDataForProjectBar extends GatherDataOperateBar implements OnClickListener {

	public GatherDataForProjectBar(MapGISFrame mapGISFrame, GatherProjectBean projectBean) {
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

		// 点补漏，线补漏在该功能下，不能做加点等操作
		if (projectBean.Type.equals("片区采集")) {
			addView(GatherDataUtils.createTextView(mapGISFrame, "描点", this));
			addView(GatherDataUtils.createDivider(mapGISFrame));
			addView(GatherDataUtils.createTextView(mapGISFrame, "连线", this));
			addView(GatherDataUtils.createDivider(mapGISFrame));
		}
		addView(GatherDataUtils.createDivider(mapGISFrame));
		addView(GatherDataUtils.createTextView(mapGISFrame, "重置", this));
		addView(GatherDataUtils.createTextView(mapGISFrame, "提交", this));

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
			mapView.setTapListener(new LinePointListener());
		} else if (text.equals("重置")) {
			resetProject();
		}
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
						GatherElementBean lineBean = new GatherElementBean(startBean.ElemSN + "-" + endBean.ElemSN, startBean.ID
								+ "," + endBean.ID, endBean.ProjectID);

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
