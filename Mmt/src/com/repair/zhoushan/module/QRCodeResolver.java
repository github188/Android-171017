package com.repair.zhoushan.module;

import com.mapgis.mmt.global.OnResultListener;

public interface QRCodeResolver<T> {
    /**
     * Sync call
     * @param codeData The raw data need to be resolved
     */
    T resolve(String codeData);

    /**
     * Async call
     * @param codeData The raw data need to be resolved
     * @param listener The callback interface to notify the resolved result
     */
    void resolve(String codeData, OnResultListener<T> listener);
}
