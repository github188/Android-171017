package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

public class ImageSwitchView extends LinearLayout implements FeedBackView {

	private ImageView imageView;
	private TextView keyTextView;
	private ImageButton valueImageButton;

	private boolean isOn;

	public ImageSwitchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public ImageSwitchView(Context context) {
		this(context, null);
	}

	private void initView(Context context) {

		View v = LayoutInflater.from(context).inflate(R.layout.image_button_view, this, true);

		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				valueImageButton.performClick();
			}
		});

		imageView = (ImageView) findViewById(R.id.imageButtonViewImage);
		imageView.setImageDrawable(getResources().getDrawable(R.drawable.flex_flow_address));
		imageView.setMinimumWidth(DimenTool.dip2px(context, 20));

		keyTextView = (TextView) findViewById(R.id.imageButtonViewKey);
		keyTextView.setTextAppearance(context, R.style.default_form_key);
		keyTextView.setText("键");
		keyTextView.setMinWidth(DimenTool.dip2px(context, 40));

		valueImageButton = (ImageButton) findViewById(R.id.imageButtonViewButton);
		valueImageButton.setImageResource(isOn ? R.drawable.switch_on : R.drawable.switch_off);
		valueImageButton.setBackgroundResource(R.color.default_no_bg);
		valueImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isOn = !isOn;
				((ImageView) v).setImageResource(isOn ? R.drawable.switch_on : R.drawable.switch_off);
			}
		});

		findViewById(R.id.imageButtonViewMust).setVisibility(GONE);

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
		isOn = str.equals("是");
	}

	@Override
	public String getValue() {
		return isOn ? "是" : "否";
	}

	@Override
	public String getKey() {
		return keyTextView.getText().toString();
	}

}
