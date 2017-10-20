package com.mapgis.mmt.common.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.mapgis.mmt.R;

public class MyProgressbarView extends View {
	private Paint paint;

	private int left, top, bottom;

	private int progress;// 设置的当前进度长度
	private int maxProgress = 100;// 设置的最大进度
	private float drawProgress;// 计算出来应填充的进度

	private int inputColor;

	private float barWidth;

	public MyProgressbarView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MyProgressbarView);

		inputColor = array.getColor(R.styleable.MyProgressbarView_progressbarColor, 0XFF00FF00); // 提供默认值，放置未指定

		// barWidth =
		// array.getDimension(R.styleable.MyProgressbarView_progressbarWidth,
		// getWidth());
		// barWidth = DimenTool.dip2px(context, barWidth);

		paint = new Paint();
		paint.setAntiAlias(true);

		array.recycle();
	}

	public MyProgressbarView(Context context) {
		this(context, null);
	}

	@Override
	public void layout(int l, int t, int r, int b) {
		super.layout(l, t, r, b);
		this.left = 0;
		this.top = getMeasuredHeight() / 2 - 10;
		this.bottom = top + 20;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// paint.setStyle(Style.STROKE);
		// canvas.drawRoundRect(new RectF(left, top, right, bottom), 10, 10,
		// paint);
		paint.setStyle(Style.FILL);

		barWidth = getMeasuredWidth();
		this.drawProgress = (float) progress / maxProgress * barWidth;

		paint.setColor(getResources().getColor(R.color.progressbar_bg));
		canvas.drawRoundRect(new RectF(left, top, left + barWidth, bottom), 20, 20, paint);

		paint.setColor(inputColor);
		canvas.drawRoundRect(new RectF(left, top, left + drawProgress, bottom), 20, 20, paint);
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
		postInvalidate();
	}

	public int getMaxProgress() {
		return maxProgress;
	}

	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}

}