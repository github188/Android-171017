package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;

public class ImageEditButtonView extends RelativeLayout implements FeedBackView {
	private ImageView imageView;
	private TextView keyTextView;
	private EditText editText;
	public ImageButton button;

	private TextView mustView;// 必填项

	public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	public EditText getEditText() {
		return editText;
	}
	
	public ImageEditButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initView(context);
	}

	public ImageEditButtonView(Context context) {
		this(context, null);
	}

	private void initView(Context context) {
		LayoutInflater.from(context).inflate(R.layout.image_edit_button_view, this, true);

		imageView = (ImageView) findViewById(R.id.imageButtonViewImage);
		imageView.setImageDrawable(getResources().getDrawable(R.drawable.flex_flow_address));
		imageView.setMinimumWidth(DimenTool.dip2px(context, 20));

		keyTextView = (TextView) findViewById(R.id.imageButtonViewKey);
		keyTextView.setTextAppearance(context, R.style.default_form_key);
		keyTextView.setText("键");
		keyTextView.setMinWidth(DimenTool.dip2px(context, 40));

		editText = (EditText) findViewById(R.id.imageEditViewEdit);
		editText.setTextAppearance(context, R.style.default_text_medium_1);
		editText.setHint("请填写信息");
		editText.setHintTextColor(getResources().getColor(R.color.default_text_light_black2));
		editText.setMinWidth(DimenTool.dip2px(context, 180));
		editText.setId(BaseClassUtil.generateViewId());

		mustView = (TextView) findViewById(R.id.imageButtonViewMust);
		mustView.setVisibility(View.GONE);

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
		editText.setText(str);
	}

	@Override
	public String getValue() {
		return editText.getText().toString();
	}

	@Override
	public String getKey() {
		return keyTextView.getText().toString();
	}

	public void setLines(int lines) {
		editText.setLines(lines);
	}

	public void setFloat() {
		editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
	}

	public void setNum() {
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
	}

	public void isMustDo(boolean isMust) {
		mustView.setVisibility(isMust ? View.VISIBLE : View.GONE);
	}

	public void setOnButtonClickListener(OnClickListener onClickListener) {
		if (onClickListener != null)
			button.setOnClickListener(onClickListener);
	}

}
