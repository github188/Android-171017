package com.mapgis.mmt.module.gis.investigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mapgis.mmt.R;

public class ProtertyEditDialog extends Activity {

	private Button button1;
	private Button button2;
	private EditText editText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.system_settings_dialog);

		boolean isFromProtertyEditActivity = getIntent().getBooleanExtra("isFromProtertyEditActivity", false);
		if (isFromProtertyEditActivity) {
			findViewById(R.id.dialog_textView).setVisibility(View.GONE);
			((EditText) findViewById(R.id.dialog_editText)).setInputType(InputType.TYPE_CLASS_TEXT);
		}

		final String key = getIntent().getStringExtra("key");
		final String value = getIntent().getStringExtra("value");
		final int position = getIntent().getIntExtra("position", -1);
		((TextView) findViewById(R.id.dialogTitle)).setText(key);

		editText = (EditText) findViewById(R.id.dialog_editText);
		editText.setText(value);

		button1 = (Button) findViewById(R.id.btn_cancel);
		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		button2 = (Button) findViewById(R.id.btn_ok);
		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String str = editText.getText().toString();
				Intent intent = new Intent();
				intent.putExtra("editValue", str);
				intent.putExtra("position", position);
				intent.putExtra("key", key);
				intent.putExtra("value", value);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
}
