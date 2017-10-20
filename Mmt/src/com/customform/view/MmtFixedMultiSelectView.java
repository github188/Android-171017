package com.customform.view;

import android.content.Context;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.ListCheckBoxDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 值复选器
 * Created by zoro at 2017/9/1.
 */
class MmtFixedMultiSelectView extends MmtBaseView {
    MmtFixedMultiSelectView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_dropdown_checkbox;
    }

    public View build(){
        final ArrayList<String> values = new ArrayList<>();
        final ImageButtonView view = new ImageButtonView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        if (!BaseClassUtil.isNullOrEmptyString(control.ConfigInfo)) {
            if (control.ConfigInfo.contains(",")) {
                values.addAll(BaseClassUtil.StringToList(control.ConfigInfo, ","));
            } else {
                values.add(control.ConfigInfo);
            }
        }
        // CheckBox不提供默认值
        // view.setValue(values.size() > 0 ? values.get(0) : "");

        if (control.Value.length() != 0) {
            view.setValue(control.Value);
        }

        view.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String> defValueList = new ArrayList<String>();
                String val = view.getValue();
                if (!BaseClassUtil.isNullOrEmptyString(val)) {
                    defValueList.addAll(BaseClassUtil.StringToList(val, ","));
                }

                ListCheckBoxDialogFragment fragment
                        = ListCheckBoxDialogFragment.newInstance(control.DisplayName, values, defValueList);
                fragment.show(getActivity().getSupportFragmentManager(), "");
                fragment.setOnRightButtonClickListener(new ListCheckBoxDialogFragment.OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View v, List<String> selectedItems) {
                        String selectedStr = BaseClassUtil.listToString(selectedItems);
                        view.setValue(selectedStr);
                    }
                });
            }
        });

        return view;
    }
}
