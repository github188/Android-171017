package com.mapgis.mmt.net.multhreaddownloader;

import java.io.File;

public interface DownloadProgressListener {
	void onStart();

	void onLoading(double current, double total);

	void onSuccess(File f);
}