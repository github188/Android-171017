package com.mapgis.mmt.module.gatherdata.operate.bar;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
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

/**
 * 片区采集功能条(地图底部功能条)
 * 
 */
public class GatherDataForPointBar extends GatherDataOperateBar implements
		OnClickListener {
	private TextView drawView;

	private RectQueryTask rectQueryTask;

	public GatherDataForPointBar(MapGISFrame mapGISFrame,
			GatherProjectBean projectBean) {
		super(mapGISFrame, projectBean);

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
		addView(GatherDataUtils.createTextView(mapGISFrame, "重置", this));
		addView(GatherDataUtils.createDivider(mapGISFrame));
		addView(GatherDataUtils.createTextView(mapGISFrame, "提交", this));
	}

	@Override
	public void onMoveButtonClick(GatherElementBean elementBean) {
		drawView.setVisibility(View.VISIBLE);
		super.onMoveButtonClick(elementBean);
	}

	@Override
	public void onClick(View v) {

		if (!(v instanceof TextView))
			return;

		String text = ((TextView) v).getText().toString();

		if (text.equals("描点")) {

			if (rectQueryTask != null
					&& rectQueryTask.getStatus() == Status.RUNNING) {
				mapGISFrame.showToast("正在进行查询，请稍等");
				return;
			}

			rectQueryTask = new RectQueryTask();
			rectQueryTask.executeOnExecutor(MyApplication.executorService);

		} else if (text.equals("提交")) {
			reportGatherResult();

		} else if (text.equals("重置")) {
			resetProject();
		}
	}

	/** 查询该描点附近是否存在线设备，若查询到指定点，则将描点移动到与该线的垂足点上 */
	class RectQueryTask extends AsyncTask<Void, Void, GraphicPolylin> {
		private GatherElementBean taskBean;
		private boolean isAdd = false;

		@Override
		protected GraphicPolylin doInBackground(Void... arg0) {
			taskBean = null;
			if (projectBean == null)
				return null;
			// 没有点，点击描点时，加一个点
			if (projectBean.elementBeans.size() == 0) {// 加点点击事件，增加点
				isAdd = true;
				taskBean = addElementBean();
			}
			// 有点时，点击描点，弹出编辑框
			else if (projectBean.elementBeans.size() == 1) {

				taskBean = movePoint(projectBean.elementBeans.get(0));

				// //如果该点需要移动，则移动后弹出编辑框
				// if (projectBean.elementBeans.get(0).canMove) {// 移动点
				// //移动到新点后要判断是否打断
				// taskBean = movePoint(projectBean.elementBeans.get(0));
				// }
				// else{
				// taskBean =projectBean.elementBeans.get(0);
				// return null;
				// }

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
				if (result == null || result.getPointCount() < 2) {// 查找到描绘点，但周围并未存在线设备
					// 新加点，则直接弹出属性编辑框
					// 移动点，相当于改属性，还是要弹出编辑框

					if (projectBean.dot == null) {
						projectBean.dot = taskBean.mapDot;
					} else {
						// 移动后
					
						if (!GisUtil.equals(projectBean.dot, taskBean.mapDot)) {
							projectBean.dot = taskBean.mapDot;
							projectBean.addressChanged = true;
						}
					}
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
						// TODO Auto-generated method stub

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

						if (!GisUtil.equals(projectBean.dot, taskBean.mapDot)) {
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
