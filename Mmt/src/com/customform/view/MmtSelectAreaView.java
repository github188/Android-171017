package com.customform.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageDotView;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.areaopr.AreaOprUtil;
import com.mapgis.mmt.module.gis.toolbar.areaopr.SelectAreaMapCallback;
import com.mapgis.mmt.module.gis.toolbar.areaopr.ShowAreaMapCallback;

/**
 * 区域控件
 * Created by zoro at 2017/9/1.
 */
class MmtSelectAreaView extends MmtBaseView implements MmtBaseView.ReadonlyHandleable {

    private ImageDotView imageDotView;

    MmtSelectAreaView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_address;
    }

    public View build() {
        final ImageDotView view = new ImageDotView(context);
        this.imageDotView = view;
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        EditText editText = (EditText) view.getValueEditView();
        editText.setFocusable(false);
        editText.setSingleLine(true);
        if (control.Validate.equals("1")) {
            view.isMustDo(true);
        }
//        if ("true".equalsIgnoreCase(IsRead)) {
//            view.setValue(AreaOprUtil.showPainTxt);
//        } else {
//            boolean hasPain = false;
//            if (!TextUtils.isEmpty(Value) || !TextUtils.isEmpty(DefaultValues)) {
//                hasPain = true;
//            }
//            view.setValue(hasPain ? AreaOprUtil.hasPainTxt : AreaOprUtil.startPainTxt);
//        }
        // view.setValue(!TextUtils.isEmpty(Value) ? Value : "");
        if (control.isReadOnly()) {
            view.setValue(AreaOprUtil.showPainTxt);
            //区域展示
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseMapCallback callback = new ShowAreaMapCallback(context, control.Value);

                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
            view.getValueEditView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseMapCallback callback = new ShowAreaMapCallback(context, control.Value);

                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
            view.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseMapCallback callback = new ShowAreaMapCallback(context, control.Value);

                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
        } else {
            //区域绘制
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getActivity().getIntent();
                    intent.putExtra("controlName", control.Name);
                    getActivity().setIntent(intent);
                    control.Value = view.getValue();
                    BaseMapCallback callback = new SelectAreaMapCallback(context, control.Value);

                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
            view.getValueEditView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getActivity().getIntent();
                    intent.putExtra("controlName", control.Name);
                    getActivity().setIntent(intent);
                    control.Value = view.getValue();
                    BaseMapCallback callback = new SelectAreaMapCallback(context, control.Value);

                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
            view.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getActivity().getIntent();
                    intent.putExtra("controlName", control.Name);
                    getActivity().setIntent(intent);
                    control.Value = view.getValue();
                    BaseMapCallback callback = new SelectAreaMapCallback(context, control.Value);

                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });

        }
        return view;
    }

    @Override
    public boolean onStart(Intent intent) {
        String controlName = intent.getStringExtra("controlName");

        if (TextUtils.isEmpty(controlName) || !controlName.equals(this.control.Name))
            return false;

        //区域控件处理
        String area;

        if (!TextUtils.isEmpty(area = intent.getStringExtra("area"))) {
            GDControl control = (GDControl) imageDotView.getTag();
            control.Value = area;
            imageDotView.setTag(control);
            imageDotView.setValue(area);

            intent.removeExtra("area");
        }

        return true;
    }
}
