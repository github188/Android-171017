package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

public class ImageButtonView extends RelativeLayout implements FeedBackView {
    private ImageView imageView;
	private TextView keyTextView;
    private TextView valueTextView;
    private ImageButton button;

    private TextView mustView;// 必填项

    public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}
    public ImageButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView(context);
    }

    public ImageButtonView(Context context) {
        this(context, null);
    }

    private void initView(Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.image_button_view, this, true);

        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                button.performClick();
            }
        });

        if (isInEditMode())
            return;

        imageView = (ImageView) findViewById(R.id.imageButtonViewImage);
        imageView.setImageResource(R.drawable.flex_flow_address);
        imageView.setMinimumWidth(DimenTool.dip2px(context, 20));

        keyTextView = (TextView) findViewById(R.id.imageButtonViewKey);
        keyTextView.setTextAppearance(context, R.style.default_form_key);
        keyTextView.setText("键");
        keyTextView.setMinWidth(DimenTool.dip2px(context, 40));

        valueTextView = (TextView) findViewById(R.id.imageButtonViewValue);
        valueTextView.setTextAppearance(context, R.style.default_text_medium_1);
        valueTextView.setMinWidth(DimenTool.dip2px(context, 40));

        mustView = (TextView) findViewById(R.id.imageButtonViewMust);
        mustView.setVisibility(View.INVISIBLE);

        button = (ImageButton) findViewById(R.id.imageButtonViewButton);
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

    public ImageButton getButton() {
        return button;
    }

    public TextView getValueTextView() {
        return valueTextView;
    }

    @Override
    public String getValue() {
        return valueTextView.getText().toString();
    }

    @Override
    public String getKey() {
        return keyTextView.getText().toString();
    }

    public void setRequired(boolean required) {
        mustView.setVisibility(required ? View.VISIBLE : View.INVISIBLE);
    }

}
