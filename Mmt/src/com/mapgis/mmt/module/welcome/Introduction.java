package com.mapgis.mmt.module.welcome;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.constant.ActivityClassRegistry;

public class Introduction extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.introduction);

		findViewById(R.id.introductionButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences preferences = MyApplication.getInstance().getSystemSharedPreferences();
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("isSeeIntroduction", false);
				editor.commit();

				Intent intent = new Intent(Introduction.this, ActivityClassRegistry.getInstance().getActivityClass("登录界面"));
				startActivity(intent);
				finish();
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}
}
