package com.mapgis.mmt.common.widget;

import android.app.ProgressDialog;
import android.content.Context;

public class MmtProgressDialog {
	public static ProgressDialog getLoadingProgressDialog(Context context, String message) {
		ProgressDialog loadingDialog = new ProgressDialog(context);

		loadingDialog.setMessage(message);
		loadingDialog.setIndeterminate(true);
		loadingDialog.setCancelable(true);

		return loadingDialog;
	}
}
