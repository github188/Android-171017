package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

/** 常用分割线 */
public class ImageLineView extends ImageView {

	public ImageLineView(Context context) {
		this(context, null);
	}

	public ImageLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, DimenTool.dip2px(context, 1)));
		this.setBackgroundColor(getResources().getColor(R.color.default_line_bg));
	}
}
