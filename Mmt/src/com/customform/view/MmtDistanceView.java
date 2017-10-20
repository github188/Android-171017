package com.customform.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.widget.customview.FeedBackView;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageDotView;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;

import java.util.Map;

/**
 * 距离
 * Created by zoro at 2017/9/1.
 */

public class MmtDistanceView extends MmtBaseView {

    public MmtDistanceView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_short_text;
    }

    /**
     * 距离，计算当前位置坐标与给定坐标间的距离(逻辑使用时处理)
     */
    public View build() {

        ImageEditView view = new ImageEditView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setEditable(false);
        view.setImage(getIconRes());

        if (control.ValidateRule.contains("number")) {
            view.setInputType(ImageEditView.MmtInputType.DECIMAL);
        }
        if (!BaseClassUtil.isNullOrEmptyString(control.Unit)) {
            view.setModifier(control.Unit);
        }
        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        view.setValue(control.Value.length() == 0 ? control.DefaultValues : control.Value);

        if (control.isReadOnly()) {
            View isDeleteView = view.getIvDelete();
            if (isDeleteView != null) {
                isDeleteView.setVisibility(View.GONE);
                isDeleteView.setOnClickListener(null);
            }
        }

        return view;
    }

    // 距离控件
    private View[] disRefControls = new View[3]; // size()=3; [1]:Coord1; [2]:Coord2; [3]:Distance

    // "距离"控件
    private String[] resolveDistanceControl() {

        String disControlName = control.Name;
        String configInfo = control.ConfigInfo;

        if (TextUtils.isEmpty(configInfo)) {
            Toast.makeText(context, "距离控件配置有误", Toast.LENGTH_SHORT).show();
            return null;
        }

        String[] disControlNames = configInfo.split("=");
        if (disControlNames.length != 2) {
            Toast.makeText(context, "距离控件配置有误", Toast.LENGTH_SHORT).show();
            return null;
        }
        disControlNames = disControlNames[1].split(",");

        if (disControlNames.length != 2) {
            Toast.makeText(context, "距离控件配置有误", Toast.LENGTH_SHORT).show();
            return null;
        }

        return new String[]{disControlNames[0], disControlNames[1], disControlName};
    }

    @Override
    public void onViewCreated(Map<String, Integer> controlIds) {
        try {
            super.onViewCreated(controlIds);

            String[] names = resolveDistanceControl();

            if (names == null)
                return;

            for (int i = 0; i < names.length; i++)
                disRefControls[i] = getActivity().findViewById(controlIds.get(names[i]));

            // "距离"控件
            for (int i = 0; i < 2; i++) {
                View formView = disRefControls[i];

                // "距离"控件相关的坐标控件，加入到 disRefControls最前
                View posView = null;

                if (formView instanceof ImageTextView) {
                    posView = ((ImageTextView) formView).getValueTextView();
                } else if (formView instanceof ImageButtonView) {
                    posView = ((ImageButtonView) formView).getValueTextView();
                } else if (formView instanceof ImageDotView) {
                    posView = ((ImageDotView) formView).getValueEditView();
                }

                if (posView == null)
                    continue;

                ((TextView) posView).addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateDistance();
                    }
                });
            }

            updateDistance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // "距离"控件
    private void updateDistance() {
        try {
            Dots dots = new Dots();

            for (int i = 0; i < 2; i++) {
                View view = disRefControls[i];

                if (view == null)
                    break;

                // 若不是需要反馈的视图
                if (!(view instanceof FeedBackView)) {
                    break;
                }

                String value = ((FeedBackView) view).getValue();

                if (TextUtils.isEmpty(value) || !value.contains(",") || value.equalsIgnoreCase("0,0")) {
                    break;
                }

                Dot dot = GisUtil.convertDot(value);

                if (dot == null) {
                    break;
                }

                dots.append(dot);
            }

            double distance = 0;

            if (dots.size() == 2) {
                distance = GisUtil.calcDistance(dots.get(0), dots.get(1));
            }

            final View disView = disRefControls[2];

            if (disView instanceof ImageEditView) {
                ((ImageEditView) disView).setValue(String.valueOf((int) distance));
                ((ImageEditView) disView).setModifier("米");
            } else {
                ((ImageTextView) disView).setValue(String.valueOf((int) distance));
                ((ImageTextView) disView).setModifier("米");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
