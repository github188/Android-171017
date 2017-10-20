package com.mapgis.mmt.module.gis.toolbar.online.query.nearby;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.common.widget.fragment.SplitListViewFragment;
import com.mapgis.mmt.common.widget.fragment.SplitListViewFragment.SplitListViewPositiveClick;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryResult;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineSpatialQueryTask;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.GraphicCircle;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.List;

public class OnlineNearbyQueryMenu extends BaseMapMenu {

	/** 当前查询的图层名称 */
	private String layerName;
	/** 当前查询的范围 */
	private int range;

	private final SharedPreferences preferences;

	/** 文本数据存储，附近查询的图层信息的Key值 */
	public static final String NEARBY_QUERY_LAYER_NAME = "NEARBY_QUERY_LAYER_NAME";
	/** 文本数据存储，附近查询的范围的Key值 */
	public static final String NEARBY_QUERY_RANGE = "NEARBY_QUERY_RANGE";

	protected OnlineFeature[] onlineFeature;

	// 给最新查询出来的数据编号
	int[] icons = { R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc, R.drawable.icon_markd,
			R.drawable.icon_marke, R.drawable.icon_markf, R.drawable.icon_markg, R.drawable.icon_markh, R.drawable.icon_marki,
			R.drawable.icon_markj };

	public OnlineNearbyQueryMenu(MapGISFrame mapGISFrame) {
		super(mapGISFrame);

		preferences = MyApplication.getInstance().getSystemSharedPreferences();
		layerName = preferences.getString(NEARBY_QUERY_LAYER_NAME, "阀门");
		range = preferences.getInt(NEARBY_QUERY_RANGE, 100);
	}

	@Override
	public boolean onActivityResult(int resultCode, Intent intent) {
		switch (resultCode) {
		case ResultCode.RESULT_WHERE_SELECTED:
			showLayerAndRangeFragment();
			break;
		case ResultCode.RESULT_PIPE_LOCATE:
			String uuid = intent.getStringExtra("uuid");

			if (uuid == null) {
				Toast.makeText(mapGISFrame, "uuid = null", Toast.LENGTH_SHORT).show();
			}

			for (Annotation annotation : mapView.getAnnotationLayer().getAllAnnotations()) {

				if (annotation.getUid() == null || annotation.getUid().length() == 0) {
					continue;
				}

				if (!(annotation instanceof MmtAnnotation)) {
					continue;
				}

				MmtAnnotation mmtAnnotation = (MmtAnnotation) annotation;

				if (uuid.equals(mmtAnnotation.uuid)) {
					annotation.showAnnotationView();
					break;
				}

			}
			break;
		}

		return super.onActivityResult(resultCode, intent);
	}

	@Override
	public boolean onOptionsItemSelected() {
		if (mapView == null || mapView.getMap() == null) {
			mapGISFrame.stopMenuFunction();
			return false;
		}

		setLayerNameAndRange();

		OnlineLayerInfo onlineLayerInfo = MapServiceInfo.getInstance().getLayerByName(layerName);

		if (onlineLayerInfo == null) {
			mapGISFrame.showToast("未查询到指定图层信息");
			return false;
		}

		GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

		// 以当前位置点为原点，指定范围为半径，绘制圆形
		Dot centerDot = null;

		if (xyz == null || !xyz.isUsefull()) {
			Toast.makeText(mapGISFrame, "未获取到当前坐标位置,取当前范围中心点!", Toast.LENGTH_SHORT).show();

			centerDot = mapView.getCenterPoint();

		} else {
			centerDot = new Dot(xyz.getX(), xyz.getY());
		}

		mapView.getGraphicLayer().removeAllGraphics();
		mapView.getAnnotationLayer().removeAllAnnotations();

		Dot dot1 = new Dot(centerDot.getX() - range, centerDot.getY() - range);
		Dot dot2 = new Dot(centerDot.getX() + range, centerDot.getY() + range);

		int color = Color.parseColor("#330591c5");

		GraphicCircle circle = new GraphicCircle();
		circle.setCenterPoint(centerDot);
		circle.setRadius(range);
		circle.setColor(color);

		mapView.getGraphicLayer().addGraphic(circle);

		// 缩放至矩形区域，刷新地图
		Rect rect = new Rect(dot1.getX(), dot1.getY(), dot2.getX(), dot2.getY());
		// mapView.zoomToRange(new Rect(rect.xMin - 100, rect.yMin - 100,
		// rect.xMax + 100, rect.yMax + 100), true);
		mapView.refresh();

		new OnlineNearbyQueryTask(mapGISFrame, rect, onlineLayerInfo.id).executeOnExecutor(MyApplication.executorService);

		return true;
	}

	@Override
	public View initTitleView() {
		View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.header_bar_plan_name, null);

		view.findViewById(R.id.tvPlanBack).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mapGISFrame.resetMenuFunction();
			}
		});

		((TextView) view.findViewById(R.id.tvPlanName)).setText("附近查询");
		view.findViewById(R.id.ivPlanDetail).setVisibility(View.VISIBLE);

		view.findViewById(R.id.ivPlanDetail).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mapGISFrame, OnlineNearbyQueryResultActivity.class);

				intent.putExtra("data", onlineFeature);
				intent.putExtra("layerName", layerName);
				intent.putExtra("whereInfo", ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).getText()
						.toString());
				mapGISFrame.startActivityForResult(intent, 0);
			}
		});

		return view;
	}

	/** 设置标题栏查询条件信息 */
	private void setLayerNameAndRange() {
		StringBuilder builder = new StringBuilder();
		builder.append("图层名称: ");
		builder.append(layerName);
		builder.append("   范围: ");
		builder.append(range);
		builder.append("米");
		((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).setText(builder.toString());
	}

	/**
	 * 显示查询条件选择界面的Fragment
	 */
	private void showLayerAndRangeFragment() {
		// 获取图层列表信息
		List<String> layerNames = new ArrayList<String>();
		for (int i = 0; i < MapServiceInfo.getInstance().getLayers().length; i++) {
			OnlineLayerInfo layer = MapServiceInfo.getInstance().getLayers()[i];
			layerNames.add(layer.name);
		}

		ArrayList<String> ranges = new ArrayList<String>();
		ranges.add(100 + "米");
		ranges.add(200 + "米");
		ranges.add(500 + "米");

		// 默认只有100m,200m,500m
		List<List<String>> rightDatas = new ArrayList<List<String>>();
		for (int i = 0; i < ranges.size(); i++) {
			rightDatas.add(layerNames);
		}

		SplitListViewFragment fragment = new SplitListViewFragment("选择条件查询", "范围", "图层信息", ranges, rightDatas);

		fragment.setSplitListViewPositiveClick(new SplitListViewPositiveClick() {
			@Override
			public void onSplitListViewPositiveClick(String leftListValue, String rightListValue, int leftPos, int rightPos) {
				layerName = rightListValue;
				range = Integer.valueOf(leftListValue.replace("米", ""));

				Editor editor = preferences.edit();
				editor.putString(NEARBY_QUERY_LAYER_NAME, layerName);
				editor.putInt(NEARBY_QUERY_RANGE, range);
				editor.apply();

				setLayerNameAndRange();

				onOptionsItemSelected();
			}
		});

		// fragment.show() 会出现异常
		// illegalStateException
		// Can not perform this action after onSaveInstanceState
		Message message = handler.obtainMessage();
		message.obj = fragment;
		handler.sendMessage(message);
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			SplitListViewFragment fragment = (SplitListViewFragment) msg.obj;
			fragment.setCancelable(false);
			fragment.setLeftLayoutWeight(3);
			fragment.setRightLayoutWeight(7);
			fragment.show(mapGISFrame.getSupportFragmentManager().beginTransaction(), "");
		}
	};

	/**
	 * @deprecated 显示附近查询条件界面，原始界面
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void showLayerAndRangeSelectView() {
		View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.nearby_query_select, null);

		final TextView nearbyQueryLayerTxt = (TextView) view.findViewById(R.id.nearbyQueryLayerTxt);
		nearbyQueryLayerTxt.setText(layerName);
		final TextView nearbyQueryRangeTxt = (TextView) view.findViewById(R.id.nearbyQueryRangeTxt);
		nearbyQueryRangeTxt.setText(range + "");

		// 点击选择查询范围事件
		view.findViewById(R.id.nearbyQueryRange).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ListDialogFragment listDialogFragment = new ListDialogFragment("选择范围", new String[] { "100", "200", "500" });
				listDialogFragment.show(mapGISFrame.getSupportFragmentManager(), "");
				listDialogFragment.setListItemClickListener(new OnListItemClickListener() {
					@Override
					public void onListItemClick(int arg2, String value) {
						nearbyQueryRangeTxt.setText(value);
					}
				});
			}
		});

		// 点击选择查询图层事件
		view.findViewById(R.id.nearbyQueryLayer).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				List<String> layerNames = new ArrayList<String>();

				LayerEnum layerEnum = mapView.getMap().getLayerEnum();
				layerEnum.moveToFirst();
				MapLayer mapLayer;
				while ((mapLayer = layerEnum.next()) != null) {
					if (!(mapLayer instanceof VectorLayer))
						continue;
					layerNames.add(mapLayer.getName());
				}

				// for (int i = 0; i < mapView.getMap().getLayerCount(); i++) {
				// MapLayer layer = mapView.getMap().getLayer(i);
				// layerNames.add(layer.getName());
				// }

				ListDialogFragment listDialogFragment = new ListDialogFragment("选择图层", layerNames);
				listDialogFragment.show(mapGISFrame.getSupportFragmentManager(), "");
				listDialogFragment.setListItemClickListener(new OnListItemClickListener() {
					@Override
					public void onListItemClick(int arg2, String value) {
						nearbyQueryLayerTxt.setText(value);
					}
				});
			}
		});

		// 显示条件选择界面
		OkCancelDialogFragment fragment = new OkCancelDialogFragment("选择条件", view);
		fragment.show(mapGISFrame.getSupportFragmentManager(), "");

		fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
			@Override
			public void onRightButtonClick(View view) {
				layerName = nearbyQueryLayerTxt.getText().toString();
				range = Integer.valueOf(nearbyQueryRangeTxt.getText().toString());

				Editor editor = preferences.edit();
				editor.putString(NEARBY_QUERY_LAYER_NAME, layerName);
				editor.putInt(NEARBY_QUERY_RANGE, range);
				editor.apply();

				setLayerNameAndRange();

				onOptionsItemSelected();
			}
		});

	}

	/** 附近查询线程类 */
	class OnlineNearbyQueryTask extends OnlineSpatialQueryTask {

		public OnlineNearbyQueryTask(MapGISFrame mapGISFrame, Rect rect, String objectIds) {
			super(mapGISFrame, rect, objectIds);
		}

		@Override
		protected void onTaskDone(OnlineQueryResult data) {

			mapView.setAnnotationListener(new MmtAnnotationListener());

			onlineFeature = data.features;

			for (int i = 0; i < onlineFeature.length; i++) {

				OnlineFeature featureResult = onlineFeature[i];

				String field = LayerConfig.getInstance().getConfigInfo(layerName).HighlightField;

				String highlight = BaseClassUtil.isNullOrEmptyString(field) ? layerName : featureResult.attributes.get(field);

				MmtAnnotation mmtAnnotation = featureResult.showAnnotationOnMap(mapView, highlight, String.valueOf(i),
						BitmapFactory.decodeResource(mapGISFrame.getResources(), icons[i]));
				mmtAnnotation.attrMap.putAll(featureResult.attributes);
			}

			mapView.refresh();
		}
	}

}
