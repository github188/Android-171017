package com.mapgis.mmt.common.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

import com.mapgis.mmt.R;

public class CameraVideoActivtiy extends Activity {

	private MovieRecorderView mRecorderView;//视频录制控件
	private Button mShootBtn;//视频开始录制按钮
	private View dismissView;
	private boolean isFinish = true;
	private boolean success = false;//防止录制完成后出现多次跳转事件

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);

		mRecorderView = (MovieRecorderView) findViewById(R.id.movieRecorderView);

		if (this.getIntent() != null){
			mRecorderView.setRecordDir(this.getIntent().getStringExtra(MediaStore.EXTRA_OUTPUT));
		}

		mShootBtn = (Button) findViewById(R.id.shoot_button);
		dismissView = findViewById(R.id.dimissActivity);

		//用户长按事件监听
		mShootBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {//用户按下拍摄按钮
					mShootBtn.setBackgroundResource(R.drawable.bg_movie_add_shoot_select);
					mRecorderView.record(new MovieRecorderView.OnRecordFinishListener() {

						@Override
						public void onRecordFinish() {
							if(!success&&mRecorderView.getTimeCount()<10){//判断用户按下时间是否大于10秒
								success = true;
								handler.sendEmptyMessage(1);
							}
						}
					});
				} else if (event.getAction() == MotionEvent.ACTION_UP) {//用户抬起拍摄按钮
					mShootBtn.setBackgroundResource(R.drawable.bg_movie_add_shoot);
					if (mRecorderView.getTimeCount() > 3 && mRecorderView.getmVecordFile().length() > 0){//判断用户按下时间是否大于3秒以及是否录制成功
						if(!success){
							success = true;
							handler.sendEmptyMessage(1);
						}
					} else {
						success = false;
						if (mRecorderView.getmVecordFile() != null)
							mRecorderView.getmVecordFile().delete();//删除录制的过短视频
						mRecorderView.stop();//停止录制
						Toast.makeText(CameraVideoActivtiy.this, "视频录制时间太短", Toast.LENGTH_SHORT).show();
					}
				}
				return true;
			}
		});

		dismissView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
                CameraVideoActivtiy.this.onBackPressed();
				return false;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		isFinish = true;
//		if (mRecorderView.getmVecordFile() != null)
//			mRecorderView.getmVecordFile().delete();//视频使用后删除
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		isFinish = false;
		success = false;
		mRecorderView.stop();//停止录制
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(success){
				finishActivity();
			}
		}
	};

	//视频录制结束后，跳转的函数
	private void finishActivity() {
		Intent intent = new Intent();
		// 初始化为无效返回码
		int resultCode;
		if (isFinish) {
			mRecorderView.stop();
			// 将录制的视频路径返回
			intent.setData(Uri.fromFile(mRecorderView.getmVecordFile()));
			resultCode = Activity.RESULT_OK;

		}else{
			success = false;
			resultCode = Activity.RESULT_CANCELED;
		}
		setResult(resultCode,intent);

		dismissActivity();
//
	}

	private void dismissActivity(){
		this.finish();
		this.overridePendingTransition(0, R.anim.bottom_out);
	}

	/**
	 * 录制完成回调
	 */
	 public interface OnShootCompletionListener {
		 void OnShootSuccess(String path, int second);
		 void OnShootFailure();
	 }

	@Override
	public void onBackPressed() {
		dismissActivity();
//		this.overridePendingTransition(0, R.anim.bottom_out);
//		super.onBackPressed();
	}
}
