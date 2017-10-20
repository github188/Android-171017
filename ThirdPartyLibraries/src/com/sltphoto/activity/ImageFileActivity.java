package com.sltphoto.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.sltphoto.adapter.FolderAdapter;
import com.sltphoto.util.AlbumHelper;
import com.sltphoto.util.Bimp;
import com.sltphoto.util.ImageBucket;
import com.sltphoto.util.ImageItem;
import com.thirdpartylibraries.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个类主要是用来进行显示包含图片的文件夹
 */
public class ImageFileActivity extends Activity {

	private FolderAdapter folderAdapter;
	private GridView gridView;
	private Button bt_cancel;
	private Button bt_ok;

	private AlbumHelper helper;
	public static List<ImageBucket> contentList;
	public static Bitmap bitmap;

	private ProgressDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plugin_camera_image_file);

		bt_cancel = (Button) findViewById(R.id.cancel);
		bt_cancel.setOnClickListener(new CancelListener());

		bt_ok = (Button) findViewById(R.id.ok);
		bt_ok.setOnClickListener(new OKListener());

		gridView = (GridView) findViewById(R.id.fileGridView);

		TextView textView = (TextView) findViewById(R.id.headerTitle);
		textView.setText(getResources().getString(R.string.photo));

		init();
	}

	private void init() {
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plugin_camera_no_pictures);

		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());

		loadingDialog = new ProgressDialog(this);

		loadingDialog.setMessage("正在扫描存储卡...");
		loadingDialog.setIndeterminate(true);
		loadingDialog.setCancelable(true);
		loadingDialog.show();

		connection.connect();
	}

	private final MediaScannerConnection connection = new MediaScannerConnection(this, new MediaScannerConnectionClient() {

		public void onScanCompleted(String path, Uri uri) {
			connection.disconnect();
		}

		public void onMediaScannerConnected() {
			MediaScannerConnection.scanFile(ImageFileActivity.this, new String[] { Environment.getExternalStorageDirectory()
					.getAbsolutePath() }, new String[] { "image/jpeg", "image/jpg", "image/png", "image/bmp" },
					new MediaScannerConnection.OnScanCompletedListener() {
						public void onScanCompleted(String path, Uri uri) {
							ScannerCompleted();
						}
					});
		}
	});

	private void ScannerCompleted() {
		ImageFileActivity.this.runOnUiThread(new Runnable() {

			public void run() {
				if (loadingDialog != null)
					loadingDialog.dismiss();

				contentList = helper.getImagesBucketList();

				folderAdapter = new FolderAdapter(ImageFileActivity.this);

				gridView.setAdapter(folderAdapter);
			}
		});
	}

	private class CancelListener implements OnClickListener {// 取消按钮的监听
		public void onClick(View v) {
			// 清空选择的图片
			Bimp.tempSelectBitmap.clear();
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	}

	private class OKListener implements OnClickListener {// 完成按钮的监听
		public void onClick(View v) {
			onActivityResult(1201, Activity.RESULT_OK, null);
		}
	}

	@Override
	public void onBackPressed() {
		// 清空选择的图片
		Bimp.tempSelectBitmap.clear();
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_CANCELED) {
			return;
		}

		if (requestCode == 1201) {
			ArrayList<String> list = new ArrayList<String>();

			for (ImageItem item : Bimp.tempSelectBitmap) {
				list.add(item.imagePath);
			}

			Bimp.tempSelectBitmap.clear();

			Intent intent = new Intent();
			intent.putStringArrayListExtra("ImageFilePaths", list);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	}
}
