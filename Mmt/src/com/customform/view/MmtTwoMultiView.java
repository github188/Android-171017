package com.customform.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 联动框
 * Created by zoro at 2017/9/1.
 */
class MmtTwoMultiView extends MmtBaseView {

    MmtTwoMultiView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_cascade_selector;
    }

    /**
     * 创建是联动框视图
     */
    public View build() {
        final List<String> values = new ArrayList<>();

        String showName = control.DisplayName;
        String defValues = control.DefaultValues;

        if (control.DisplayName.contains(";")) {// 子联动框
            showName = showName.split(";")[1];
            if (control.DefaultValues.contains(";")) {
                defValues = BaseClassUtil.StringToList(control.DefaultValues, ";").get(0);
            } else {
                defValues = control.DefaultValues;
            }
        }

        if (defValues.contains(",")) {
            values.addAll(Arrays.asList(defValues.split(",")));
        } else {
            values.add(defValues);
        }

        final ImageButtonView view = new ImageButtonView(context);

        view.setTag(control);
        view.setKey(showName);
        view.setImage(getIconRes());
        view.setValue(values.size() > 0 ? values.get(0) : "");

        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        if (control.Value.length() != 0) {
            view.setValue(control.Value);
        }

        return view;
    }

    @Override
    protected View buildReadonlyView() {
        ImageTextView view = new ImageTextView(context);
        view.setTag(control);

        String displayName = control.DisplayName;
        if (control.DisplayName.contains(";")) {
            String[] keys = control.DisplayName.split(";");
            if (keys.length > 1) {
                displayName = keys[1];
            }
        }
        view.setKey(displayName);

        view.setImage(getIconRes());
        if (!TextUtils.isEmpty(control.Unit)) {
            view.setModifier(control.Unit);
        }
        view.setValue(getDefaultValue());
        return view;
    }
}
