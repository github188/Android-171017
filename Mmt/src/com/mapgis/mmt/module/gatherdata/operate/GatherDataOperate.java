package com.mapgis.mmt.module.gatherdata.operate;

import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gatherdata.GatherDataUtils;
import com.mapgis.mmt.module.gatherdata.GatherProjectBean;
import com.mapgis.mmt.module.gatherdata.operate.bar.GatherDataForProjectBar;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.android.mapview.MapView;

public class GatherDataOperate {

	protected final MapGISFrame mapGISFrame;

	/** 数据模型 */
	protected GatherProjectBean projectBean;

	/** 底部操作框 */
	protected GatherDataOperateBar operateBar;

	/** 地图初始下方的装载四个菜单栏的View */
	protected final LinearLayout layoutMapToolbar;

	/** 地图/列表 按钮 */
	// private TextView functionView;

	public GatherDataOperate(final MapGISFrame mapGISFrame,
			GatherProjectBean projectBean) {
		this(mapGISFrame, projectBean, false);
	}

	public GatherDataOperate(final MapGISFrame mapGISFrame,
			GatherProjectBean projectBean, boolean isView) {
		this.mapGISFrame = mapGISFrame;
		this.projectBean = projectBean;

		// this.mapGISFrame.setCustomView(initTitleView());

		// 清空地图上会绘制的其他图形
		final MapView mapView = mapGISFrame.getMapView();
		mapView.getGraphicLayer().removeAllGraphics();
		mapView.getAnnotationLayer().removeAllAnnotations();
		mapView.refresh();

		// 将原始工具条功能隐藏
		layoutMapToolbar = (LinearLayout) mapGISFrame
				.findViewById(R.id.layoutMapToolbar);
		layoutMapToolbar.getChildAt(0).setVisibility(View.GONE);

		// 清空除了原始工具功能以外的其他功能
		for (int i = 1; i < layoutMapToolbar.getChildCount(); i++) {
			layoutMapToolbar.removeViewAt(i);
		}

		if (isView) {
			operateBar = new GatherDataForProjectBar(mapGISFrame, projectBean);
		} else {
			operateBar = GatherDataOperateBar.getOperateBar(mapGISFrame,
					projectBean);
		}

		createToolSubView(operateBar);
	}

	/** 底部操作不可操作 */
	public void setToolBarNoneOperate() {
		operateBar.removeAllViews();
		TextView textView = GatherDataUtils.createTextView(mapGISFrame,
				"已完成任务，不可操作", null);
		textView.setTextColor(Color.RED);
		operateBar.addView(textView);
	}

	/** 启动功能 */
	public void showGatherDataMap() {
		mapGISFrame.showMainFragment(true);
	}

	/** 在底部工具栏添加工具 */
	protected void createToolSubView(View value) {
		layoutMapToolbar.addView(value);
	}

	/** 判断两个点是否相同*/

}
