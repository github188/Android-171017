package com.mapgis.mmt.entity;

import com.mapgis.mmt.net.BaseTaskListener;

public class EmptyBaseServiceListener implements BaseTaskListener<String> {

	@Override
	public void onCompletion(short completFlg, String localObject1) {

	}

	@Override
	public void onError(Throwable paramThrowable) {

	}
}
