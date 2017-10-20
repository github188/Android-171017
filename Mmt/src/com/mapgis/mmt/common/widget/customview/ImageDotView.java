package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

public class ImageDotView extends RelativeLayout implements FeedBackView {

	private ImageView imageView;
	private TextView keyTextView;
	private EditText valueEditView;
	private ImageButton button;

	private TextView mustView;// 必填项

	public ImageDotView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initView(context);
	}

	public ImageDotView(Context context) {
		this(context, null);
	}

	private void initView(Context context) {
		View v = LayoutInflater.from(context).inflate(R.layout.image_dot_view, this, true);

		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				button.performClick();
			}
		});

		imageView = (ImageView) findViewById(R.id.imageDotViewImage);
		imageView.setImageDrawable(getResources().getDrawable(R.drawable.flex_flow_address));
		imageView.setMinimumWidth(DimenTool.dip2px(context, 20));

		keyTextView = (TextView) findViewById(R.id.imageDotViewKey);
		keyTextView.setTextAppearance(context, R.style.default_form_key);
		keyTextView.setText("键");
		keyTextView.setMinWidth(DimenTool.dip2px(context, 40));

		valueEditView = (EditText) findViewById(R.id.imageDotViewValue);
		valueEditView.setTextAppearance(context, R.style.default_text_medium_1);
		valueEditView.setMinWidth(DimenTool.dip2px(context, 40));

		mustView = (TextView) findViewById(R.id.imageDotViewMust);
		mustView.setVisibility(View.GONE);

		button = (ImageButton) findViewById(R.id.imageDotViewButton);
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
		valueEditView.setText(str);
	}

	public ImageButton getButton() {
		return button;
	}

	public TextView getValueEditView() {
		return valueEditView;
	}

	@Override
	public String getValue() {
		return valueEditView.getText().toString();
	}

	@Override
	public String getKey() {
		return keyTextView.getText().toString();
	}

	public void isMustDo(boolean isMust) {
		mustView.setVisibility(isMust ? View.VISIBLE : View.GONE);
	}

}
