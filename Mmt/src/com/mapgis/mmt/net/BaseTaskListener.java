package com.mapgis.mmt.net;

//Task监听接口
public interface BaseTaskListener<T> {
	  short ON_OK = 1;
	  short ON_CANCEL = 0;
	  short ON_ERROR = -1;

	  void onCompletion(short completFlg, T localObject1);

	  void onError(Throwable paramThrowable);
}
