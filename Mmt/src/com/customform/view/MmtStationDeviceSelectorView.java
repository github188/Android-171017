package com.customform.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import com.customform.entity.StationDeviceEvent;
import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.repair.zhoushan.module.devicecare.stationaccount.stationdeviceselect.StationDeviceListActivity;

import java.util.List;

/**
 * 场站设备选择器
 * Created by zoro at 2017/9/1.
 */
class MmtStationDeviceSelectorView extends MmtBaseView {
    MmtStationDeviceSelectorView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_dropdown_checkbox;
    }

    /**
     * 场站设备选择器
     */
    public View build() {

        final ImageButtonView view = new ImageButtonView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        view.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, StationDeviceListActivity.class));
            }
        });

        return view;
    }

    @Override
    public boolean onEventBusCallback(Object tag) {
        try {
            if (tag == null || !(tag instanceof StationDeviceEvent))
                return false;

            StationDeviceEvent stationDeviceEvent = (StationDeviceEvent) tag;

            // 业务类型，设备名称，设备ID
            ImageButtonView bizTypeView = (ImageButtonView) findViewByName("业务名称");
            ImageButtonView deviceNameView = (ImageButtonView) findViewByName("设备类型");
            ImageButtonView deviceIdView = (ImageButtonView) findViewByName("设备ID");

            bizTypeView.setValue(stationDeviceEvent.bizType);
            deviceNameView.setValue(stationDeviceEvent.deviceName);
            deviceIdView.setValue(stationDeviceEvent.deviceID);

            ImageButtonView eventTypeView = (ImageButtonView) findViewByName("事件类型");
            ImageButton button = eventTypeView.getButton();
            List<String> selectableList = (List<String>) button.getTag();
            boolean isMatched = false;
            for (String str : selectableList) {
                if (str.equals(stationDeviceEvent.deviceName)) {
                    eventTypeView.setValue(str);
                    isMatched = true;
                    break;
                }
            }
            button.setEnabled(!isMatched);
            eventTypeView.setEnabled(!isMatched);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
