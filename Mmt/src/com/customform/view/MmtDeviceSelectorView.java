package com.customform.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageEditButtonView;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.eventbustype.DeviceSelectEvent;
import com.mapgis.mmt.module.gis.SelectDeviceCallback;
import com.mapgis.mmt.module.gis.ShowDeviceCallback;

/**
 * 设备选择
 * Created by zoro at 2017/9/1.
 */
class MmtDeviceSelectorView extends MmtDotBaseView implements MmtBaseView.ReadonlyHandleable {

    MmtDeviceSelectorView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_dropdown_checkbox;
    }

    /**
     * 设备选择
     */
    public View build() {
        final ImageButtonView view = new ImageButtonView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        view.setValue(!TextUtils.isEmpty(control.Value) ? control.Value : "");
        view.getButton().setOnClickListener(controlClickListener);
        view.getValueTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.getButton().performClick();
            }
        });

        return view;
    }

    private final View.OnClickListener controlClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (control.isReadOnly()) {
                if (TextUtils.isEmpty(control.Value)) {
                    MyApplication.getInstance().showMessageWithHandle("无效位置");
                    return;
                }
                BaseMapCallback callback = new ShowDeviceCallback(context, control.Value, "", "", -1);
                MyApplication.getInstance().sendToBaseMapHandle(callback);

            } else {
                BaseMapCallback callback = new SelectDeviceCallback(context, control.Value);
                MyApplication.getInstance().sendToBaseMapHandle(callback);
            }
        }
    };

    public boolean onEventBusCallback(Object tag) {
        if (tag == null || !(tag instanceof DeviceSelectEvent))
            return false;

        DeviceSelectEvent deviceSelectEvent = (DeviceSelectEvent) tag;

        View deviceView = findViewByType("设备选择");

        if (!(deviceView instanceof ImageButtonView)) {
            MyApplication.getInstance().showMessageWithHandle("未找到设备选择控件");
            return true;
        }
        ImageButtonView iv = (ImageButtonView) deviceView;
        iv.setValue(!TextUtils.isEmpty(deviceSelectEvent.loc) ? deviceSelectEvent.loc : "");

        View addview = findViewByType("百度地址");
        if (addview instanceof ImageEditButtonView) {
            ImageEditButtonView imageEditButtonViewtemp = (ImageEditButtonView) addview;
            positionAddressLink(imageEditButtonViewtemp, deviceSelectEvent.addr, deviceSelectEvent.names);
        }

        View layerNameview = findViewByName("GIS图层");
        if (layerNameview instanceof ImageEditView) {
            ImageEditView layerImageEditView = (ImageEditView) layerNameview;
            String layerName = !TextUtils.isEmpty(deviceSelectEvent.layerName) ? deviceSelectEvent.layerName : "";
            layerImageEditView.setValue(layerName);
            GDControl control = (GDControl) layerImageEditView.getTag();
            control.setValue(layerName);
        }
        if (layerNameview instanceof ImageTextView) {
            ImageTextView layerImageEditView = (ImageTextView) layerNameview;
            String layerName = !TextUtils.isEmpty(deviceSelectEvent.layerName) ? deviceSelectEvent.layerName : "";
            layerImageEditView.setValue(layerName);
            GDControl control = (GDControl) layerImageEditView.getTag();
            control.setValue(layerName);
        }

        View filedValview = findViewByName("GIS编号");
        if (filedValview instanceof ImageEditView) {
            String gisNo = !TextUtils.isEmpty(deviceSelectEvent.filedVal) ? deviceSelectEvent.filedVal : "";
            ImageEditView numImageEditView = (ImageEditView) filedValview;
            numImageEditView.setValue(gisNo);
            GDControl control = (GDControl) numImageEditView.getTag();
            control.setValue(gisNo);
        }
        if (filedValview instanceof ImageTextView) {
            String gisNo = !TextUtils.isEmpty(deviceSelectEvent.filedVal) ? deviceSelectEvent.filedVal : "";
            ImageTextView numImageTextView = (ImageTextView) filedValview;
            numImageTextView.setValue(gisNo);
            GDControl control = (GDControl) numImageTextView.getTag();
            control.setValue(gisNo);
        }

        return true;
    }
}
