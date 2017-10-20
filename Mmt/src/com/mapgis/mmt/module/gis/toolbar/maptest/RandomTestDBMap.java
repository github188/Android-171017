package com.mapgis.mmt.module.gis.toolbar.maptest;

import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.zondy.mapgis.geometry.Rect;

import java.util.Date;
import java.util.Random;

public class RandomTestDBMap extends BaseMapMenu {

	public RandomTestDBMap(MapGISFrame mapGISFrame) {
		super(mapGISFrame);
	}

	@Override
	public boolean onOptionsItemSelected() {
		isRun = true;
		count = 0;
		doWorkThread.start();

		return true;
	}

	Rect target;

	boolean isRun = false;
	int count = 0;

	Thread doWorkThread = new Thread(new Runnable() {

		@Override
		public void run() {

			Thread.currentThread().setName(this.getClass().getName());

			while (isRun) {
				try {
					Rect range = mapView.getMap().getEntireRange();

					Random random = new Random(new Date().getTime());

					double rangeX = range.xMax - range.xMin;
					double rangeY = range.yMax - range.yMin;

					double x1 = range.xMin + random.nextDouble() * rangeX;
					double x2 = range.xMin + random.nextDouble() * rangeX;

					double y1 = range.yMin + random.nextDouble() * rangeY;
					double y2 = range.yMin + random.nextDouble() * rangeY;

					target = new Rect(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2));

					count++;

					MyApplication.getInstance().sendToBaseMapHandle(baseMapCallback);

					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	});

	BaseMapCallback baseMapCallback = new BaseMapCallback() {

		@Override
		public boolean handleMessage(Message msg) {
			mapView.zoomToRange(target, true);

			Log.v(RandomTestDBMap.this.getClass().getSimpleName(), "开始第  " + count + " 次缩放: " + target);

			return true;
		}
	};

	@Override
	public View initTitleView() {
		View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);

		((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("随机地图测试");

		view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isRun = false;

				mapGISFrame.resetMenuFunction();
			}
		});

		return view;
	}
}
