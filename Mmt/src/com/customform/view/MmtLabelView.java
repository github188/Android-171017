package com.customform.view;

import android.content.Context;

import com.maintainproduct.entity.BuildInField;
import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageTextView;

/**
 * 标签
 * Created by zoro on 2017/9/1.
 */
class MmtLabelView extends MmtBaseView {
    MmtLabelView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_tag;
    }

    /**
     * 创建标签类型视图
     */
    @Override
    public ImageTextView build() {
        ImageTextView view = new ImageTextView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        if (control.DefaultValues.startsWith("$") && control.DefaultValues.endsWith("$")) {
            control.DefaultValues = BuildInField.getValue(view, control.DefaultValues);
        }

        view.setValue(control.Value.length() == 0 ? control.DefaultValues : control.Value);
        return view;
    }
}
