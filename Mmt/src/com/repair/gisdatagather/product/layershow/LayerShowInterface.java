package com.repair.gisdatagather.product.layershow;

import android.content.Context;
import android.widget.RadioButton;

import java.util.List;

/**
 * Created by liuyunfan on 2016/5/5.
 */
public interface LayerShowInterface {
    void getCanEditLayer();
    void createRadioGroup(Context context, List<RadioButton> radioButtons);
    void clickRadioButton(String layerName);
    void editDefaultAttr();
}
