package com.repair.gisdatagather.product.layershow;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.FlowRadioGroup;
import com.repair.gisdatagather.enn.bean.GISDeviceSetBean;
import com.repair.gisdatagather.product.gisgather.GisGather;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/5/6.
 */
public class LayerShowPanelMobile extends LayerShowPanel {
    public LayerShowPanelMobile(GisGather gisGather, View view) {
        super(gisGather, view);
    }

    @Override
    public void createRadioGroup(Context context, List<RadioButton> radioButtons) {
        ViewGroup vg = (ViewGroup) view.findViewById(R.id.radioGroup);
        vg.removeAllViews();
        gisGather.rg = new FlowRadioGroup(context, radioButtons, 1);
        gisGather.rg.setOnCheckedChangeListener(LayerShowPanelMobile.this);
        vg.addView(gisGather.rg);
    }

    @Override
    public void createRadioButtons(List<GISDeviceSetBean> gisDeviceSetBeans) {
        List<RadioButton> radioButtons = new ArrayList<RadioButton>();
        for (GISDeviceSetBean gisDeviceSetBean : gisDeviceSetBeans) {
            gisGather.gisDeviceSetBeans.add(gisDeviceSetBean);

            RadioButton rb = new RadioButton(gisGather.mapGISFrame);
            rb.setText(gisDeviceSetBean.alias);
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            radioButtons.add(rb);
        }

        createRadioGroup(gisGather.mapGISFrame, radioButtons);
    }
}
