package com.mapgis.mmt.common.util;

import android.content.Context;

/**
 * 单位转换工具
 * 
 * @author Administrator
 * 
 */
public class DimenTool {

	private static float scale = 0.0f;

	public static float getScale(Context context) {
		if (scale == 0) {
			scale = context.getResources().getDisplayMetrics().density;
		}
		return scale;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = getScale(context);
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = getScale(context);
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 获取手机的宽(像素)
	 */
	public static int getWidthPx(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * 获取手机的高(像素)
	 */
	public static int getHeightPx(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

}
