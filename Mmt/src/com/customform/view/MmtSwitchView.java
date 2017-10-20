package com.customform.view;

import android.content.Context;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageSwitchView;

/**
 * 是否
 * Created by zoro on 2017/9/1.
 */
class MmtSwitchView extends MmtBaseView {
    MmtSwitchView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_radiobutton;
    }

    /**
     * 创建是否类型视图
     */
    @Override
    public View build() {
        ImageSwitchView imageSwitchView = new ImageSwitchView(context);
        imageSwitchView.setTag(control);
        imageSwitchView.setKey(control.DisplayName);
        imageSwitchView.setImage(getIconRes());
        imageSwitchView.setValue(control.Value);
        return imageSwitchView;
    }
}
