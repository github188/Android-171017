package com.customform.view;

import android.content.Context;
import android.text.TextUtils;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageTextView;

/**
 * 保留字、只读
 * Created by zoro at 2017/9/1.
 */
class MmtTextView extends MmtBaseView {


    MmtTextView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_short_text;
    }

    /**
     * 创建文本类型视图
     */
    @Override
    public ImageTextView build() {
        ImageTextView view = new ImageTextView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        if (!TextUtils.isEmpty(control.Unit)) {
            view.setModifier(control.Unit);
        }

        String defaultValue = getDefaultValue();
        view.setValue(defaultValue);

        return view;
    }
}