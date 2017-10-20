package com.mapgis.mmt.common.widget.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.customview.ImageLineView;

public class ThreeButtonPopupWindow {
	private final LinearLayout parentLayout;

	private TextView firstView;
	private TextView secondView;
	private TextView thirdView;

	private PopupWindow popupWindow;
	private PopupWindow showdowWindow;

	public ThreeButtonPopupWindow(Activity activity) {
		parentLayout = initView(activity);
		ListenTouchEvent();
	}

	/** 显示该界面 */
	public void show(Activity activity) {
		showdowWindow = new PopupWindow();
		RelativeLayout relativeLayout = new RelativeLayout(activity);
		relativeLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		showdowWindow.setContentView(relativeLayout);
		showdowWindow.setWidth(LayoutParams.MATCH_PARENT);
		showdowWindow.setHeight(LayoutParams.MATCH_PARENT);
		showdowWindow.setBackgroundDrawable(new ColorDrawable(0xb0000000));
		showdowWindow.showAtLocation(activity.getWindow().getDecorView(),
				Gravity.NO_GRAVITY, 0, 0);

		popupWindow = new PopupWindow();
		popupWindow.setContentView(parentLayout);
		// popupWindow.setWindowLayoutMode(LayoutParams.MATCH_PARENT,
		// LayoutParams.MATCH_PARENT);
		popupWindow.setWidth(LayoutParams.MATCH_PARENT);
		popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
		popupWindow.setFocusable(true);
		popupWindow.setAnimationStyle(R.style.PopupBottomAnimation);
		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				showdowWindow.dismiss();
			}
		});
		popupWindow.showAtLocation(activity.getWindow().getDecorView(),
				Gravity.BOTTOM, 0, 0);
	}

	public void dismiss() {
		if (popupWindow != null)
			popupWindow.dismiss();

		if (showdowWindow != null)
			showdowWindow.dismiss();
	}

	/**
	 * 初始化界面
	 * 
	 * @param context
	 */
	private LinearLayout initView(Context context) {
		LinearLayout parentLayout = new LinearLayout(context);
		parentLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		parentLayout.setOrientation(LinearLayout.VERTICAL);
		parentLayout.setBackgroundColor(Color.WHITE);

		parentLayout.addView(new ImageLineView(context));

		firstView = creatTextView(context);
		parentLayout.addView(firstView);

		parentLayout.addView(new ImageLineView(context));

		secondView = creatTextView(context);
		parentLayout.addView(secondView);

		View view = new View(context);
		view.setBackgroundDrawable(new ColorDrawable(0xbb888888));
		view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, DimenTool.dip2px(context, 6)));
		parentLayout.addView(view);

		thirdView = creatTextView(context);
		parentLayout.addView(thirdView);

		return parentLayout;
	}

	/**
	 * 创建文本空间
	 * 
	 * @param context
	 */
	private TextView creatTextView(Context context) {
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.weight = 1;

		TextView textView = new TextView(context);
		textView.setLayoutParams(layoutParams);
		textView.setBackgroundResource(R.drawable.layout_focus_bg);
		textView.setMinHeight(DimenTool.dip2px(context, 48));
		textView.setGravity(Gravity.CENTER);
		textView.setTextAppearance(context, R.style.default_text_medium_1);
		// textView.setTextColor(Color.parseColor("#009CDD"));

		return textView;
	}

	public void setFirstView(String text, OnClickListener onClickListener) {
		firstView.setText(text);
		firstView.setOnClickListener(onClickListener);
	}

	public void setSecondView(String text, OnClickListener onClickListener) {
		secondView.setText(text);
		secondView.setOnClickListener(onClickListener);
	}

	public void setThirdView(String text, OnClickListener onClickListener) {
		thirdView.setText(text);
		thirdView.setOnClickListener(onClickListener);
	}

	/**
	 * touch界面和点击返回按钮
	 */
	public void ListenTouchEvent() {
		
		parentLayout.setFocusable(true);
		parentLayout.setFocusableInTouchMode(true);
		parentLayout.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_BACK)
					dismiss();

				return false;
			}
		});
		parentLayout.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				int height = parentLayout.getTop();
				int y = (int) event.getY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						dismiss();
					}
				}
				return true;
			}
		});
	}
}
