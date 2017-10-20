package com.repair.shaoxin.water.valveinstruction;

import android.content.Intent;

import com.mapgis.mmt.BaseActivity;
import com.repair.zhoushan.common.ShowAllLocatableCallback;

import java.util.ArrayList;

public class ShowAllValveCallback extends ShowAllLocatableCallback<ValveModel> {

    public ShowAllValveCallback(BaseActivity activity, ArrayList<ValveModel> dataList) {
        super(activity, dataList);
    }

    @Override
    protected void onAnnotationViewClick(ValveModel dataItem) {
        Intent intent = new Intent(activity, ValveDevicePropertyActivity.class);
        intent.putExtra("ListItemEntity", dataItem);
        activity.startActivity(intent);
    }
}
