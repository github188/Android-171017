package com.repair.zhoushan.module.devicecare.stationaccount.stationdeviceselect;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;

/**
 * 场站设备选择器，选择场站设备
 */
public class StationDeviceListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StationDeviceListFragment fragment = new StationDeviceListFragment();
        addFragment(fragment);
    }

}
