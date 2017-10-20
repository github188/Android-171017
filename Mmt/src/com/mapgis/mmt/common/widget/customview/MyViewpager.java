package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * 对系统的ViewPager 重写<br>
 * 修改了图片放大后左右滑动和ViewPager的左右滑动的冲突
 * 
 */
public class MyViewpager extends ViewPager {

	public MyViewpager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyViewpager(Context context) {
		this(context, null);
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof ViewGroup) {
			final ViewGroup group = (ViewGroup) v;
			final int scrollX = v.getScrollX();
			final int scrollY = v.getScrollY();
			final int count = group.getChildCount();

			for (int i = count - 1; i >= 0; i--) {

				final View child = group.getChildAt(i);
				if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() && y + scrollY >= child.getTop()
						&& y + scrollY < child.getBottom()
						&& canScroll(child, true, dx, x + scrollX - child.getLeft(), y + scrollY - child.getTop())) {
					return true;
				}
			}
		}

		if (v instanceof ImageViewTouch) {
			ImageViewTouch imageViewTouch = (ImageViewTouch) v;

			if (imageViewTouch.canScroll(dx)) {
				return true;
			}
		}

		return checkV && ViewCompat.canScrollHorizontally(v, -dx);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

}
