package com.mapgis.mmt.module.navigation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;

public class NavigationMainView extends HorizontalScrollView {
	private ViewGroup menu;
	private ViewGroup content;

	/**
	 * 屏幕宽度
	 */
	private int mScreenWidth;
	/**
	 * 菜单的宽度
	 */
	private int mMenuWidth;

	private boolean isOpen;

	private boolean once;

	public NavigationMainView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public NavigationMainView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NavigationMainView(Context context) {
		this(context, null, 0);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		this.scrollTo(mMenuWidth, 0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		initView();
	}

	private void initView() {
		if (!once) {
			mScreenWidth = getMeasuredWidth();

			LinearLayout wrapper = (LinearLayout) getChildAt(0);

			menu = (ViewGroup) wrapper.getChildAt(0);
			content = (ViewGroup) wrapper.getChildAt(1);

			mMenuWidth = mScreenWidth - mScreenWidth / 3;
			menu.getLayoutParams().width = mMenuWidth;
			content.getLayoutParams().width = mScreenWidth;
			// content.setMinimumWidth(mScreenWidth);
		}
		once = true;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		float scale = l * 1.0f / mMenuWidth;
		float leftScale = 1 - 0.3f * scale;
		float rightScale = 0.8f + scale * 0.2f;

		ViewHelper.setScaleX(menu, leftScale);
		ViewHelper.setScaleY(menu, leftScale);
		// ViewHelper.setAlpha(menu, 0.6f + 0.4f * (1 - scale));
		ViewHelper.setAlpha(menu, 1 - scale);
		ViewHelper.setTranslationX(menu, mMenuWidth * scale * 0.6f);

		ViewHelper.setPivotX(content, 0);
		ViewHelper.setPivotY(content, content.getHeight() / 2);
		ViewHelper.setScaleX(content, rightScale);
		ViewHelper.setScaleY(content, rightScale);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();

		switch (action) {

		case MotionEvent.ACTION_UP:

			// Up时，进行判断，如果显示区域大于菜单宽度一半则完全显示，否则隐藏
			int scrollX = getScrollX();

			if (scrollX > mMenuWidth / 2) {
				this.smoothScrollTo(mMenuWidth, 0);
				isOpen = false;
			} else {
				this.smoothScrollTo(0, 0);
				isOpen = true;
			}

			return true;
		}

		return true;
	}

	/**
	 * 打开菜单
	 */
	public void openMenu() {
		if (isOpen)
			return;
		this.smoothScrollTo(0, 0);
		isOpen = true;
	}

	/**
	 * 关闭菜单
	 */
	public void closeMenu() {
		if (isOpen) {
			this.smoothScrollTo(mMenuWidth, 0);
			isOpen = false;
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		return isOpen ? super.dispatchTouchEvent(ev) : content.dispatchTouchEvent(ev);
	}

	/**
	 * 切换菜单状态
	 */
	public void toggle() {
		if (isOpen) {
			closeMenu();
		} else {
			openMenu();
		}
	}

}
