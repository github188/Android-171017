package com.mapgis.mmt.global;

public interface OnResultListener<T> {
    void onSuccess(T t);
    void onFailed(String errMsg);
}
