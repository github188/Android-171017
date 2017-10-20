package com.customform.view;

import android.content.Context;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageRadioButtonView;

/**
 * 平铺值选择器
 * Created by zoro at 2017/9/1.
 */
class MmtRadioButtonView extends MmtBaseView {
    MmtRadioButtonView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_radiobutton;
    }

    public View build() {
        ImageRadioButtonView view = new ImageRadioButtonView(control.ConfigInfo, context);

        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        if (control.Validate.equals("1")) {
            view.isMustDo(true);
        }

        view.setValue(control.Value.length() == 0 ? control.DefaultValues : control.Value);

        if (control.onSelectedChangedListener != null) {
            view.setOnCheckedChangedListener(new ImageRadioButtonView.OnCheckedChangedListener() {
                @Override
                public void onCheckedChanged(String checkedValue) {
                    control.onSelectedChangedListener.onSelectedChanged(control, checkedValue);
                }
            });
        }

        return view;
    }
}
