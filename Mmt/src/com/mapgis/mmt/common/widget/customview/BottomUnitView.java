package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

/**
 * 底部单元格视图
 *
 * @author Administrator
 */
public class BottomUnitView extends LinearLayout {
    public ImageView imageView;
    public TextView textView;

    public BottomUnitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public BottomUnitView(Context context) {
        this(context, null);
    }


    public BottomUnitView(Context context, String text, OnClickListener onClickListener) {
        this(context);

        this.setContent(text);
        this.setOnClickListener(onClickListener);
    }

    public BottomUnitView(Context context, String text, OnClickListener onClickListener, int imageBackColor, int textViewColor) {
        this(context);
        this.setContent(text);
        this.setOnClickListener(onClickListener);
        imageView.setBackgroundColor(imageBackColor);
        this.setBackgroundColor(imageBackColor);
        textView.setTextColor(textViewColor);
    }

    protected void initView(Context context) {
        this.setClickable(true);
        //this.setBackgroundResource(R.drawable.layout_focus_bg);
        this.setBackgroundResource(AppStyle.getBtnBackgroundStyleResource());
        this.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        this.setGravity(Gravity.CENTER);
        this.setLayoutParams(layoutParams);

        imageView = new ImageView(context);
        LinearLayout.LayoutParams iParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        iParams.rightMargin = DimenTool.dip2px(context, 4);
        imageView.setLayoutParams(iParams);
        this.addView(imageView);

        textView = new TextView(context);
        textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        textView.setTextAppearance(context, R.style.default_text_medium_1);
        textView.setGravity(Gravity.CENTER);
        this.addView(textView);
    }

    public BottomUnitView setContent(String text) {
        textView.setText(text);
        return this;
    }

    public String getContent() {
        return textView.getText().toString();
    }

    public BottomUnitView setImageResource(int resId) {
        // imageView.setImageResource(resId);
        Drawable leftDrawable = getResources().getDrawable(resId);
        leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
        textView.setCompoundDrawablePadding(0);
        textView.setCompoundDrawables(leftDrawable, null, null, null);
        return this;
    }

    public enum EditMode {
        Edit,       // 编辑
        Delete,     // 删除
        Report,     // 上报
        Broswer     // 浏览
    }

    public static BottomUnitView create(Context context) {
        return new BottomUnitView(context);
    }

    public static BottomUnitView create(Context context, EditMode editMode) {
        BottomUnitView bottomUnitView = create(context);
        bottomUnitView.setImageResource(R.drawable.handoverform_report);
        switch (editMode) {
            case Edit:
                bottomUnitView.setContent("编辑");
                break;
            case Delete:
                bottomUnitView.setContent("删除");
                break;
            case Report:
                bottomUnitView.setContent("上报");
                break;
            case Broswer:
                bottomUnitView.setContent("浏览");
                break;
        }
        return bottomUnitView;
    }
}
