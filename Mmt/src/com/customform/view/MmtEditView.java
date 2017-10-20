package com.customform.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.ImageEditView;

/**
 * 短文本、长文本
 * Created by zoro at 2017/9/1.
 */
class MmtEditView extends MmtBaseView {
    MmtEditView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        if (control.isReadOnly()) {
            return R.drawable.form_short_text;
        } else {
            return R.drawable.form_editable_short_text;
        }
    }

    /**
     * 创建编辑类型视图
     */
    @Override
    public ImageEditView build() {

        ImageEditView view = new ImageEditView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        view.setImage(getIconRes());

        view.setMaxLength(control.MaxLength);

        // 多行文本的特殊处理，目前仅用于本地界面的生成
        if (control.DisplayColSpan == 100) {
            view.setLines(5);
            view.setEditTextGravity(Gravity.TOP | Gravity.LEFT);
        }

        if (!BaseClassUtil.isNullOrEmptyString(this.control.ConfigInfo)) {
            view.setHint(this.control.ConfigInfo);
        }

        if (control.ValidateRule.contains("number")) {
            view.setInputType(ImageEditView.MmtInputType.DECIMAL);
        }
        if (!BaseClassUtil.isNullOrEmptyString(this.control.Unit)) {
            view.setModifier(this.control.Unit);
        }

        view.setValue(control.Value.length() == 0 ? control.DefaultValues : control.Value);

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
}
