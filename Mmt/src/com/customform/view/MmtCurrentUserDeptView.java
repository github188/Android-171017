package com.customform.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.module.login.UserBean;

/**
 * 本人部门
 * Created by zoro at 2017/9/1.
 */
class MmtCurrentUserDeptView extends MmtBaseView {
    MmtCurrentUserDeptView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_self_dept;
    }

    /**
     * 当前用户部门控件
     */
    public ImageEditView build() {
        ImageEditView view = new ImageEditView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        if (control.Value.length() != 0) {
            view.setValue(control.Value);
        } else {
            view.setValue(control.DefaultValues.length() == 0 ? MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).Department : control.DefaultValues);
        }

        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }
        if (control.isReadOnly()) {
            View isDeleteView;
            if ((isDeleteView = view.getIvDelete()) != null) {
                isDeleteView.setVisibility(View.GONE);
                isDeleteView.setOnClickListener(null);
            }
        }
        return view;
    }

    @Override
    protected View buildReadonlyView() {
        ImageTextView view = (ImageTextView) super.buildReadonlyView();

        String defaultValue = getDefaultValue();
        if (TextUtils.isEmpty(defaultValue)) {
            defaultValue = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).Department;
        }
        view.setValue(defaultValue);
        return view;
    }
}
