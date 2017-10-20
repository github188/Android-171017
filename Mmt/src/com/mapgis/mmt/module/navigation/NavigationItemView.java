package com.mapgis.mmt.module.navigation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.ResourceUtil;
import com.mapgis.mmt.constant.NavigationMenuRegistry;

public class NavigationItemView extends RelativeLayout {

	public NavigationActivity activity;
	private NavigationItem item;

	public NavigationItem getItem() {
		return item;
	}

	public void setItem(NavigationItem item) {
		this.item = item;
		textView.setText(item.Function.Alias);
		textView.setTextSize(18);
		textView.setTextColor(Color.WHITE);

		Drawable topDrawable = getResources().getDrawable(
				ResourceUtil.getDrawableResourceId(item.Function.Icon, R.drawable.main_menu_case_list));

		topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
		textView.setCompoundDrawables(null, topDrawable, null, null);
		this.setVisibility(View.VISIBLE);
	}

	private final TextView textView;

	private final int normalColor;
	private final int pressedColor;

	public NavigationItemView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NavigationView);
		normalColor = a.getColor(R.styleable.NavigationView_normalColor, R.color.nav_blue);
		pressedColor = a.getColor(R.styleable.NavigationView_pressedColor, R.color.nav_blue);
		a.recycle();

		textView = new TextView(context);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);

		this.addView(textView, lp);

		LayerDrawable normal = createLayerDrawable(context, normalColor, 0);
		LayerDrawable pressed = createLayerDrawablePressed(context, pressedColor, 0);

		StateListDrawable stateListDrawable = createStateListDrawable(context, normal, pressed);

		// 2014.12.1 by WL 三星平板测试，此处有问题 NoSuchMethodError
		// 2014.12.3 by WL 查看SKD文档发现 setBackground() 此方法在API16版本以上才可以使用
		// this.setBackground(stateListDrawable);
		this.setBackgroundDrawable(stateListDrawable);
		this.setClickable(true);
		this.setVisibility(View.GONE);
		this.setOnClickListener(navigationItemClickListener);
	}

	private StateListDrawable createStateListDrawable(Context context, Drawable unPressed, Drawable pressed) {
		StateListDrawable sld = new StateListDrawable();

		sld.addState(new int[] { -android.R.attr.state_pressed }, unPressed);
		sld.addState(new int[] { android.R.attr.state_pressed }, pressed);

		return sld;
	}

	private LayerDrawable createLayerDrawable(Context context, int startColor, int endColor) {
		RectShape shape = new RectShape();

		ShapeDrawable[] layers = new ShapeDrawable[1];
		layers[0] = new ShapeDrawable(shape);
		layers[0].getPaint().setColor(startColor);
		layers[0].getPaint().setStyle(Paint.Style.FILL_AND_STROKE);

		LayerDrawable layerDrawable = new LayerDrawable(layers);

		return layerDrawable;
	}

	private LayerDrawable createLayerDrawablePressed(Context context, int startColor, int endColor) {
		int radius = 0;
		float[] outerR = new float[] { radius, radius, radius, radius, radius, radius, radius, radius };
		RoundRectShape shape = new RoundRectShape(outerR, null, null);

		ShapeDrawable[] layers = new ShapeDrawable[1];
		layers[0] = new ShapeDrawable(shape);
		layers[0].getPaint().setColor(startColor);
		layers[0].getPaint().setStyle(Paint.Style.FILL_AND_STROKE);

		LayerDrawable layerDrawable = new LayerDrawable(layers);
		layerDrawable.setLayerInset(0, 10, 10, 10, 10);

		return layerDrawable;
	}

	private final View.OnClickListener navigationItemClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			activity.menu = NavigationMenuRegistry.getInstance().getMenuInstance(activity, item);
			activity.menu.onItemSelected();

			if (activity.menu.item.Function.Name.equals("地图浏览")) {
				activity.overridePendingTransition(0, 0);
			} else {
				MyApplication.getInstance().startActivityAnimation(activity);
			}
		}
	};

}
