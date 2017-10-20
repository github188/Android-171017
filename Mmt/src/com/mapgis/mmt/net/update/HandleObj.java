package com.mapgis.mmt.net.update;

import android.widget.LinearLayout;

import com.mapgis.mmt.module.systemsetting.DownloadMap;

import java.io.File;

public class HandleObj {
	public DownloadMap downloadMap;
	public LinearLayout layout;

	public long startTick;

	public int preSize;
	public double current;
	public double total;

	public File file;
}