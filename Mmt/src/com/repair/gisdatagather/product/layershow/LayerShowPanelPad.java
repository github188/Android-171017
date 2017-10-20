package com.repair.gisdatagather.product.layershow;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.mapgis.mmt.common.widget.customview.FlowRadioGroup;
import com.mapgis.mmt.R;
import com.repair.gisdatagather.product.gisgather.GisGather;

import java.util.List;

/**
 * Created by liuyunfan on 2016/5/6.
 */
public class LayerShowPanelPad extends LayerShowPanel {
    public LayerShowPanelPad(GisGather gisGather, View view) {
        super(gisGather, view);
    }

    @Override
    public void createRadioGroup(Context context, List<RadioButton> radioButtons) {
        ViewGroup vg = (ViewGroup) view.findViewById(R.id.radioGroup);
        vg.removeAllViews();
        gisGather.rg = new FlowRadioGroup(context, radioButtons, 2);
        gisGather.rg.setOnCheckedChangeListener(LayerShowPanelPad.this);
        vg.addView(gisGather.rg);


    }
}
