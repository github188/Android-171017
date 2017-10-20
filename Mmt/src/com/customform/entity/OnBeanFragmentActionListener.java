package com.customform.entity;

import android.content.Intent;

import java.util.Map;

/**
 * Created by zoro at 2017/9/4.
 */
public interface OnBeanFragmentActionListener {
    void onViewCreated(Map<String, Integer> controlIds);

    boolean onActivityResult(int requestCode, int resultCode, Intent data);

    void onDestroy();

    boolean onStart(Intent intent);

    boolean onEventBusCallback(Object tag);
}