package com.mapgis.mmt.common.widget.popupwindow;

import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.map.MapLayer;

import java.util.LinkedList;
import java.util.List;

/**
 * 地图选择模式界面
 * 
 */
public class MapModePopupWindow implements OnClickListener {
	private final MapGISFrame activity;
	private final View anchorView;

	private PopupWindow popupWindow;

	public MapModePopupWindow(MapGISFrame activity, View anchorView) {
		this.activity = activity;
		this.anchorView = anchorView;
	}

	/** 显示该界面 */
	public void show() {
		List<MapMode> mapModes = initMapMode();

		PopupWindow popupWindow = new PopupWindow(createView(mapModes));
		popupWindow.setWindowLayoutMode(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		popupWindow.setFocusable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable(activity.getResources(), ""));
		popupWindow.setAnimationStyle(R.style.PopupAnimation);
		popupWindow.showAsDropDown(anchorView, 0, 0);
	}

	/** 初始化所包含的地图模式的数据模型 */
	private List<MapMode> initMapMode() {
		List<MapMode> mapModes = new LinkedList<MapMode>();

		mapModes.add(createMapMode("谷歌街道", new LinkedList<MapLayer>()));
		mapModes.add(createMapMode("谷歌卫星", new LinkedList<MapLayer>()));
		mapModes.add(createMapMode("地形图", new LinkedList<MapLayer>()));

		return mapModes;
	}

	/** 创建地图模式的数据模型 */
	private MapMode createMapMode(String mapModeName, List<MapLayer> maplayers) {
		MapMode mapMode = new MapMode();
		mapMode.mapModeName = mapModeName;
		mapMode.isVisible = true;
		mapMode.mapLayers = maplayers;
		return mapMode;
	}

	/** 创建界面 */
	private View createView(List<MapMode> mapModes) {
		LinearLayout layout = new LinearLayout(activity);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setPadding(0, DimenTool.dip2px(activity, 20), 0, DimenTool.dip2px(activity, 10));
		layout.setBackgroundResource(R.drawable.pop_layers);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setMinimumHeight(DimenTool.dip2px(activity, 120));

		for (MapMode mode : mapModes) {
			layout.addView(createItemView(mode));
		}

		return layout;
	}

	/** 创建一个控制视图 */
	private View createItemView(MapMode mapMode) {
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		layoutParams.weight = 1;
		layoutParams.gravity = Gravity.CENTER;
		layoutParams.setMargins(DimenTool.dip2px(activity, 4), DimenTool.dip2px(activity, 8), DimenTool.dip2px(activity, 4), 0);

		LinearLayout layout = new LinearLayout(activity);
		layout.setLayoutParams(layoutParams);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.CENTER);

		ImageView imageView = new ImageView(activity);
		imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		imageView.setImageResource(R.drawable.main_map_mode_plain_normal);
		imageView.setAlpha(mapMode.isVisible ? 1f : 0.24f);
		imageView.setTag(mapMode);
		imageView.setOnClickListener(this);

		TextView textView = new TextView(activity);
		textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		textView.setTextAppearance(activity, R.style.default_text_medium_1);
		textView.setText(mapMode.mapModeName);
		textView.setGravity(Gravity.CENTER);

		layout.addView(imageView);
		layout.addView(textView);

		return layout;
	}

	@Override
	public void onClick(View v) {
		MapMode mapMode = (MapMode) v.getTag();

		if (mapMode.isVisible) {
			v.setAlpha(0.24f);
			mapMode.setLayersVisiblity(false);
		} else {
			v.setAlpha(1f);
			mapMode.setLayersVisiblity(true);
		}

		activity.getMapView().refresh();
	}

	/** 当前PopupWindow是否可见 */
	public boolean isVisible() {
        return popupWindow != null && popupWindow.isShowing();
    }

	class MapMode {
		public String mapModeName;
		public boolean isVisible;
		public List<MapLayer> mapLayers;

		/**
		 * 设置图层的可见性
		 * 
		 * @param isVisiblity
		 *            true设置为可见
		 */
		public void setLayersVisiblity(boolean isVisiblity) {
			this.isVisible = isVisiblity;

			if (mapLayers == null) {
				return;
			}

			for (MapLayer mapLayer : mapLayers) {
				mapLayer.setVisible(isVisiblity);
			}
		}
	}
}
