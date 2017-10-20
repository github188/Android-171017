package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

public class ImageTextView extends LinearLayout implements FeedBackView {

    private ImageView imageView;
    private TextView keyTextView;
    private TextView valueTextView;
    private TextView modifierView;

    private static int baseMargin;

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public ImageTextView(Context context) {
        this(context, null);
    }

    public ImageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        baseMargin = DimenTool.dip2px(context, 16);

        initView(context);
    }

    private void initView(Context context) {

        this.setMinimumHeight(baseMargin * 3);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = baseMargin;
        params.gravity = Gravity.CENTER_VERTICAL;

        // Icon
        imageView = new ImageView(context);
        imageView.setLayoutParams(params);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.flex_flow_address));
        imageView.setMinimumWidth(DimenTool.dip2px(context, 20));

        // Key
        keyTextView = new TextView(context);
        keyTextView.setLayoutParams(params);
        keyTextView.setTextAppearance(context, R.style.default_form_key);
        keyTextView.setText("键");
        keyTextView.setMinWidth(DimenTool.dip2px(context, 40));
        keyTextView.setMaxWidth(DimenTool.dip2px(context, 250));

        // Modifier
        modifierView = new TextView(context);
        modifierView.setLayoutParams(params);

        // Value
        valueTextView = new TextView(context);
        // valueTextView.setTextIsSelectable(true);
        valueTextView.setTextAppearance(context, R.style.default_text_medium_1);

        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
        valueParams.leftMargin = baseMargin;
        valueParams.gravity = Gravity.CENTER_VERTICAL;
        valueParams.weight = 1;
        valueTextView.setLayoutParams(valueParams);

        addView(imageView);
        addView(keyTextView);
        addView(valueTextView);
        addView(modifierView);
    }

    @Override
    public void setImage(int id) {
        imageView.setImageDrawable(getResources().getDrawable(id));
    }

    @Override
    public void setKey(String str) {
        keyTextView.setText(str);
    }

    @Override
    public void setValue(String str) {
        valueTextView.setText(str);
    }

    @Override
    public String getValue() {
        return valueTextView.getText().toString();
    }

    @Override
    public String getKey() {
        return keyTextView.getText().toString();
    }

    public TextView getValueTextView() {
        return this.valueTextView;
    }

    public void setModifier(String modifier) {

        modifier = modifier == null ? "" : modifier.trim();
        String oldModifier = modifierView.getText().toString();

        if (!modifier.equals(oldModifier)) {

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = baseMargin;
            params.rightMargin = baseMargin;
            params.gravity = Gravity.CENTER_VERTICAL;
            modifierView.setLayoutParams(params);

            modifierView.setText(modifier);
        }
    }

}
