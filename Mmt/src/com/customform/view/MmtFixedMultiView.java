package com.customform.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.GDGroup;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 值选择器、动态值选择器
 * Created by zoro at 2017/9/1.
 */
class MmtFixedMultiView extends MmtBaseView {
    MmtFixedMultiView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_dropdown_radiobutton;
    }

    public View build() {
        final ImageButtonView view = new ImageButtonView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        final List<String> values = new ArrayList<>();
        if (!BaseClassUtil.isNullOrEmptyString(control.ConfigInfo)) {
            values.addAll(BaseClassUtil.StringToList(control.ConfigInfo, ","));
        }
        view.getValueTextView().setTag(values);

        String defaultValue;
        if (!TextUtils.isEmpty(control.Value)) {
            defaultValue = control.Value;
        } else if (!TextUtils.isEmpty(control.DefaultValues)) {
            defaultValue = control.DefaultValues;
        } else if (values.size() > 0) {
            defaultValue = values.get(0);
        } else {
            defaultValue = "";
        }
        view.setValue(defaultValue);

        view.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                if (values.size() == 0) {
                    Toast.makeText(context, "字段配置错误：没有可选值", Toast.LENGTH_SHORT).show();
                    return;
                }
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

        if (control.Type.equals("动态值选择器")) {
            initAction(view);
        }

        return view;
    }

    // 设定"动态值选择器"的行为
    private void initAction(final ImageButtonView buttonView) {

        final FlowBeanFragment beanFragment = getBeanFragment();

        if (!beanFragment.getFilterFields().containsKey(control.Name))
            return;

        @SuppressWarnings("unchecked")
        final List<String> values = (ArrayList<String>) buttonView.getValueTextView().getTag();

        buttonView.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ListDialogFragment fragment = new ListDialogFragment(control.DisplayName, values);
                fragment.show(getActivity().getSupportFragmentManager(), "");
                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        String oldValue = buttonView.getValue();
                        if (TextUtils.isEmpty(oldValue) || !oldValue.equals(value)) {

                            // 刷新界面之前记录所有控件的当前值
                            List<FeedItem> feedbackItems = beanFragment.getFeedbackItems(ReportInBackEntity.SAVING);

                            for (GDGroup gdGroup : beanFragment.data.Groups) {
                                for (GDControl gdControl : gdGroup.Controls) {
                                    for (FeedItem feedItem : feedbackItems) {
                                        if (gdControl.Name.equals(feedItem.Name)) {
                                            gdControl.Value = feedItem.Value;
                                            break;
                                        }
                                    }
                                }
                            }

                            control.Value = value;
                            beanFragment.getFilterFields().put(control.Name, value);
                            beanFragment.refreshView();
                        }
                    }
                });
            }
        });
    }
}
