package com.customform.view;

import android.content.Context;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.ImageEditButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 可编辑值选择器
 * Created by zoro at 2017/9/1.
 */
class MmtEditableMultiView extends MmtBaseView {
    MmtEditableMultiView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_editable_dropdown_radiobutton;
    }

    public View build() {
        final List<String> values = new ArrayList<>();

        final ImageEditButtonView view = new ImageEditButtonView(context);

        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        if (control.ValidateRule.contains("number")) {
            view.setFloat();
        }
        if (control.Validate.equals("1")) {
            view.isMustDo(true);
        }

        if (!BaseClassUtil.isNullOrEmptyString(control.ConfigInfo)) {
            if (control.ConfigInfo.contains(",")) {
                values.addAll(BaseClassUtil.StringToList(control.ConfigInfo, ","));
            } else {
                values.add(control.ConfigInfo);
            }
        }
        view.setValue(values.size() > 0 ? values.get(0) : "");

        if (control.Value.length() != 0) {
            view.setValue(control.Value);
        }

        view.setOnButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialogFragment fragment = new ListDialogFragment(control.DisplayName, values);
                fragment.show(getActivity().getSupportFragmentManager(), "");
                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        view.setValue(value);
                        if (control.onSelectedChangedListener != null) {
                            control.onSelectedChangedListener.onSelectedChanged(control, value);
                        }
                    }
                });
            }
        });

        view.getImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.button.performClick();
            }
        });

        return view;
    }
}
