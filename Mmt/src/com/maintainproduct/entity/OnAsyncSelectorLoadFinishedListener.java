package com.maintainproduct.entity;

import java.util.List;

/**
 * 选择器异步加载数据的回调接口(选择器，二级选择器)
 */
public interface OnAsyncSelectorLoadFinishedListener {

    void onSingleSelectorLoaded(String fieldName, List<String> fieldValues);
    void onAllSelectorLoaded();
}
