package com.cameralibary;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.thirdpartylibraries.R;

import java.io.FileOutputStream;

public class CameraActivity extends Activity {

	private CameraPreview mPreview;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private FrameLayout preview;
	private ImageButton captureButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.camera_preview);

		mPreview = new CameraPreview(this);
		mPreview.mPicture = mPicture;

		preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

		captureButton = (ImageButton) findViewById(R.id.button_capture);

		captureButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mPreview.takePicture();
			}
		});

		captureButton.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					captureButton.setImageResource(R.drawable.btn_shutter_pressed);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					captureButton.setImageResource(R.drawable.btn_shutter_default);
				}

				return false;
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private PictureCallback mPicture = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {

			new AsyncTask<byte[], Integer, Boolean>() {
				@Override
				protected Boolean doInBackground(byte[]... params) {
					try {
						Uri uri = CameraActivity.this.getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);

						FileOutputStream fos = new FileOutputStream(uri.getPath());

						fos.write(params[0]);

						fos.flush();
						fos.close();

						return true;
					} catch (Exception ex) {
						ex.printStackTrace();

						return false;
					}
				}

				@Override
				protected void onPostExecute(Boolean isOk) {
					if (isOk) {
						CameraActivity.this.setResult(-1);
						CameraActivity.this.finish();
					} else {
						Toast.makeText(CameraActivity.this, "拍照保存失败，可能存储卡未就绪", Toast.LENGTH_SHORT).show();

						mPreview.startPreview();// 重新开始预览
					}
				}
			}.execute(data);
		}
	};
}
