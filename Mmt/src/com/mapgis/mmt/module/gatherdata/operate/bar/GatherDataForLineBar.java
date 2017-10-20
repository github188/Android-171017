package com.mapgis.mmt.module.gatherdata.operate.bar;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnLeftButtonClickListener;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.module.gatherdata.GatherDataUtils;
import com.mapgis.mmt.module.gatherdata.GatherElementBean;
import com.mapgis.mmt.module.gatherdata.GatherProjectBean;
import com.mapgis.mmt.module.gatherdata.operate.GatherDataOperateBar;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.GeomType;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * 底部工具条。<br>
 * 
 * 包含加点，加线，修改，更多功能。
 * 
 */
public class GatherDataForLineBar extends GatherDataOperateBar implements
		OnClickListener {
	private RectQueryTask rectQueryTask;

	private TextView drawView;

	private TextView captureView;

	private PointQueryTask pointQueryTask;

	public GatherDataForLineBar(MapGISFrame mapGISFrame,
			GatherProjectBean projectBean) {
		super(mapGISFrame, projectBean);

		this.mapView = mapGISFrame.getMapView();

		initToolBar();

		mapGISFrame.showToast("点击<描点>按钮进行设备采集");

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

		drawView = GatherDataUtils.createTextView(mapGISFrame, "描点", this);
		addView(drawView);

		addView(GatherDataUtils.createDivider(mapGISFrame));

		captureView = GatherDataUtils.createTextView(mapGISFrame, "捕捉", this);
		addView(captureView);

		addView(GatherDataUtils.createDivider(mapGISFrame));
		addView(GatherDataUtils.createTextView(mapGISFrame, "重置", this));

		addView(GatherDataUtils.createDivider(mapGISFrame));
		addView(GatherDataUtils.createTextView(mapGISFrame, "提交", this));

	}

	private GatherElementBean moveBean() {
		for (GatherElementBean elementBean : projectBean.elementBeans) {
			if (elementBean.canMove)
				return elementBean;
		}
		return null;
	}

	@Override
	public void onMoveButtonClick(GatherElementBean elementBean) {
		drawView.setVisibility(View.VISIBLE);
		super.onMoveButtonClick(elementBean);
	}

	@Override
	protected void onCaptureFinished(LinkedHashMap<String, String> attrMap) {
		if (attrMap == null || attrMap.size() == 0) {
			mapGISFrame.showToast("捕捉失败,未查询到设备信息");
			return;
		}

		mapGISFrame.showToast("捕捉成功");

		GatherElementBean b = addElementBean();

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
		if (b.GeomType.equals("点")) {

			if (!BaseClassUtil.isNullOrEmptyString(attrMap.get("编号"))) {
				b.FieldName = "编号";
				b.FieldValue = attrMap.get("编号");
			} else if (BaseClassUtil.isNullOrEmptyString(attrMap.get("GUID"))) {
				b.FieldName = "GUID";
				b.FieldValue = attrMap.get("GUID");
			}

			b.NewGeo = "";
			b.isCaptureDot = true;
			b.OldAttr = attrStr;
			b.Oper = "捕捉";
			b.OldGeo = attrMap.get("$坐标$");
			b.LayerName = attrMap.get("$图层名称$");
		}
		executeAttr(b, true);

		projectBean.showOnMap(mapView);
	}

	@Override
	public void onClick(View v) {
		if (!(v instanceof TextView))
			return;

		String text = ((TextView) v).getText().toString();

		if (text.equals("描点")) {// 加线点击事件，增加线

			if (rectQueryTask != null
					&& rectQueryTask.getStatus() == Status.RUNNING) {
				mapGISFrame.showToast("正在进行查询，请稍等");
				return;
			}

			rectQueryTask = new RectQueryTask();
			rectQueryTask.executeOnExecutor(MyApplication.executorService);

			// // 是否存在需要移动的点
			// GatherElementBean moveBean = moveBean();
			//
			// // 成功采集3个设备，并且不存在需要移动的点，则此次采集已经完成
			// if (projectBean.elementBeans.size() == 3 && moveBean == null) {
			// mapGISFrame.showToast("设备已采集成功");
			// return;
			// }
			//
			// // 若有需要移动的点，则执行移动操作
			// if (moveBean != null) {
			// GatherElementBean bean =movePoint(moveBean);
			// executeAttr(bean, false);
			// } else {// 否则执行新增操作
			// executeAttr(addElementBean(), true);
			// }
			//
			// projectBean.showOnMap(mapView);

		} else if (text.equals("提交")) {
			reportGatherResult();
		} else if (text.equals("重置")) {
			resetProject();
		} else if (text.equals("捕捉")) {

			if (projectBean.elementBeans.size() == 3) {
				mapGISFrame.showToast("不能再捕捉更多的点");
				return;
			}

			if (pointQueryTask != null
					&& pointQueryTask.getStatus() == Status.RUNNING) {
				mapGISFrame.showToast("正在进行查询，请稍等");
				return;
			}

			if (moveBean() != null) {
				mapGISFrame.showToast("你正在执行移图操作，请完成后再执行此操作");
				return;
			}

			pointQueryTask = new PointQueryTask();
			pointQueryTask.executeOnExecutor(MyApplication.executorService);
		}
	}

	/** 查询该描点附近是否存在线设备，若查询到指定点，则将描点移动到与该线的垂足点上 */
	class RectQueryTask extends AsyncTask<Void, Void, GraphicPolylin> {
		private GatherElementBean taskBean;
		private boolean isAdd = false;
		GatherElementBean moveBean;

		@Override
		protected GraphicPolylin doInBackground(Void... arg0) {

			// 是否存在需要移动的点
			moveBean = moveBean();

			// 成功采集3个设备，并且不存在需要移动的点，则此次采集已经完成
			if (projectBean.elementBeans.size() == 3 && moveBean == null) {
				mapGISFrame.showToast("设备已采集成功");
				return null;
			}

			// 若有需要移动的点，则执行移动操作
			if (moveBean != null) {
				taskBean = movePoint(moveBean);
			} else {// 否则执行新增操作
				isAdd = true;
				taskBean = addElementBean();
			}
			if (taskBean == null)
				return null;
			Graphic graphic = GatherDataUtils.searchTargetGeomLayer(mapView,
					taskBean.mapDot, GeomType.GeomLin);

			if (graphic == null || !(graphic instanceof GraphicPolylin)) {
				return null;
			}

			GraphicPolylin polylin = (GraphicPolylin) graphic;

			return polylin;
		}

		@Override
		protected void onPostExecute(final GraphicPolylin result) {
			try {
				if (taskBean == null) {// 未查到到描绘点
					return;
				}
				projectBean.addressChanged = false;

				projectBean.showOnMap(mapView);

				// 只关注对第一个点
				if (taskBean.ElemSN.equals("1"))
					if (!GisUtil.equals(projectBean.dot, taskBean.mapDot)) {
						projectBean.dot = taskBean.mapDot;
						projectBean.addressChanged = true;
					}

				if (result == null || result.getPointCount() < 2) {// 查找到描绘点，但周围并未存在线设备
					// 新加点，则直接弹出属性编辑框
					// 移动点，相当于改属性，还是要弹出编辑框
					executeAttr(taskBean, isAdd);
					return;
				}

				// 周围存在设备线
				OkCancelDialogFragment fragment = new OkCancelDialogFragment(
						"是否打断");
				fragment.setLeftBottonText("否");
				fragment.setRightBottonText("是");

				// 否也要弹出编辑框
				fragment.setOnLeftButtonClickListener(new OnLeftButtonClickListener() {

					@Override
					public void onLeftButtonClick(View view) {
						executeAttr(taskBean, isAdd);
					}

				});
				fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
					@Override
					public void onRightButtonClick(View view) {
						Dot startDot = result.getPoint(0);
						Dot endDot = result.getPoint(1);

						Dot footDot = GisUtil.GetFootOfPerpendicular(
								taskBean.mapDot, startDot, endDot);

						moveToPoint(taskBean, footDot);
						taskBean.Oper = "打断";
						projectBean.showOnMap(mapView);

						// 只关注对第一个点
						if (projectBean.elementBeans.size() == 1)
							if (!GisUtil.equals(projectBean.dot,
									taskBean.mapDot)) {
								projectBean.dot = taskBean.mapDot;
								projectBean.addressChanged = true;
							}

						executeAttr(taskBean, isAdd);
					}

				});

				fragment.show(mapGISFrame.getSupportFragmentManager(), "");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
